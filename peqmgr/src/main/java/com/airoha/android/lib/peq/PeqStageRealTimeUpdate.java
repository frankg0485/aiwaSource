package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Airoha internal use
 */
public class PeqStageRealTimeUpdate extends PeqStage{
    private static final String TAG = "PeqStageRealTimeUpdate";

    Map<Rate, CoefParamStruct> mRateCoefParamStruct;

    public PeqStageRealTimeUpdate(AirohaPeqMgr mgr, Map<Rate, CoefParamStruct> rateCoefParamStructMap) {
        super(mgr);

        mRateCoefParamStruct = rateCoefParamStructMap;

        mRaceId = RaceId.DSP_REALTIME_PEQ;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        RacePacket racePacket = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.DSP_REALTIME_PEQ);

        List<Byte> byteList = new ArrayList<>();

        byte phase = 0x00;

        byteList.add(phase);

        byte[] numberOfSampleRate = Converter.shortToBytes((short) mRateCoefParamStruct.size());
        byte[] peqAlgoVersion = new byte[]{0x00, 0x00};


        for (byte b : numberOfSampleRate) {
            byteList.add(b);
        }

        for (byte b : peqAlgoVersion) {
            byteList.add(b);
        }

        for (CoefParamStruct coefParamStruct : mRateCoefParamStruct.values()) {
            byte[] raw = coefParamStruct.getRaw();

            for (byte b : raw) {
                byteList.add(b);
            }
        }

        byte[] payload = new byte[byteList.size()];

        for (int i = 0; i < byteList.size(); i++) {
            payload[i] = byteList.get(i);
        }

        racePacket.setPayload(payload);

        return racePacket;
    }
}
