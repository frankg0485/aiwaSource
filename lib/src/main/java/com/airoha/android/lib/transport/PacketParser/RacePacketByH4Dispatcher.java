package com.airoha.android.lib.transport.PacketParser;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MTK60279 on 2018/2/6.
 */

public class RacePacketByH4Dispatcher {
    public static final byte RACE_BY_H4_START = 0x05;
    private static String TAG = "RacePacketByH4Dispatcher";

    private ConcurrentHashMap<String, OnRacePacketListener> mRacePacketListeners;
    private ConcurrentHashMap<String, OnRaceMmiPacketListener> mMmiPacketListeners;
    private ConcurrentHashMap<String, OnRaceMmiRoleSwitchIndListener> mRolSwitchIndListeners;

    private AirohaLink mAirohaLink;

    public RacePacketByH4Dispatcher(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;

        mRacePacketListeners = new ConcurrentHashMap<>();

        mMmiPacketListeners = new ConcurrentHashMap<>();

        mRolSwitchIndListeners = new ConcurrentHashMap<>();
    }

    public static boolean isRackeByH4Collected(byte[] packet) {
        return packet[0] == RACE_BY_H4_START;
    }

    public void registerRacePacketListener(String subscriber, OnRacePacketListener listener) {
        mRacePacketListeners.put(subscriber, listener);
    }

    public void registerRaceMmiPacketListener(String subscriber, OnRaceMmiPacketListener listener) {
        mMmiPacketListeners.put(subscriber, listener);
    }

    public void registerRaceMmiRoleSwitchIndLisener(String subsriber, OnRaceMmiRoleSwitchIndListener listener) {
        mRolSwitchIndListeners.put(subsriber, listener);
    }

    public static boolean isRaceResponse(byte[] packet) {
        return packet[1] == RaceType.RESPONSE;
    }

    private static boolean isRaceIndication(byte[] packet) {
        return packet[1] == RaceType.INDICATION;
    }

    public void parseSend(byte[] packet) {

        int raceId = Converter.BytesToU16(packet[5], packet[4]);

        int raceType = packet[1];

        int payloadLength = Converter.BytesToU16(packet[3], packet[2]) - 2;

        int payloadStartIdx = 6;

        for (OnRacePacketListener respListener : mRacePacketListeners.values()) {
            if (respListener != null) {
                respListener.handleRespOrInd(raceId, packet, raceType);
            }
        }


        if (raceType == RaceType.RESPONSE) {
            byte respStatus = packet[payloadStartIdx];

            if (raceId == RaceId.RACE_ANC_ON) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncSetOnResp(respStatus);
                    }
                }
            }

            if (raceId == RaceId.RACE_ANC_OFF) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncSetOffResp(respStatus);
                    }
                }
            }

            if (raceId == RaceId.RACE_ANC_GET_STATUS) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncGetStatusResp(respStatus);
                    }
                }
            }

            if (raceId == RaceId.RACE_ANC_SET_GAIN) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncSetGainResp(respStatus);
                    }
                }
            }

            if (raceId == RaceId.RACE_ANC_READ_PARAM_FROM_NVKEY) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncReadParamFromNvKeyResp(respStatus);
                    }
                }
            }

            if (raceId == RaceId.RACE_ANC_WRITE_GAIN_TO_NVKEY) {
                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncWriteGainToNvKeyResp(respStatus);
                    }
                }
            }
        }

        if(raceType == RaceType.INDICATION){
            if(raceId == RaceId.RACE_ANC_READ_PARAM_FROM_NVKEY){
                byte[] payload = new byte[payloadLength];

                System.arraycopy(packet, payloadStartIdx, payload, 0, payloadLength);

                for (OnRaceMmiPacketListener listener : mMmiPacketListeners.values()){
                    if(listener != null){
                        listener.OnAncReadParamFromNvKeyInd(payload);
                    }
                }
            }

            if(raceId == RaceId.RACE_BLUETOOTH_ROLE_SWITCH) {
                byte status = packet[payloadStartIdx];

                for(OnRaceMmiRoleSwitchIndListener listener : mRolSwitchIndListeners.values()) {
                    if(listener != null) {
                        listener.OnRoleSwitched(status);
                    }
                }
            }

        }
    }
}
