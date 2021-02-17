package com.airoha.android.lib.simulator;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.fota.RaceIndActiveFotaPreparation;
import com.airoha.android.lib.RaceCommand.packet.fota.RaceRespActiveFotaPreparation;
import com.airoha.android.lib.RaceCommand.packet.fota.fotTws.RaceIndFotaGetVersion;
import com.airoha.android.lib.RaceCommand.packet.fota.fotTws.RaceRespFotaGetVersion;
import com.airoha.android.lib.RaceCommand.packet.mmi.RaceIndRoleSwitch;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;

public class AirohaSimFwServer {
    public static final String TAG = "AirohaSimFwServer";

    private AirohaLink mAirohaLink;


    /**
     * @see AirohaLink#doEngineeringCmd(String, Object)
     * @param airohaLink
     */
    public AirohaSimFwServer(AirohaLink airohaLink){
        mAirohaLink = airohaLink;

        mAirohaLink.registerOnRacePacketListener(TAG, mRacePacketListener);
    }

    private OnRacePacketListener mRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            Log.d(TAG, "received raceId: " + String.format("%04X", raceId)
                    + ", raceType: " + String.format("%02X", raceType) );

            handle_RACE_FOTA_ACTIVE_FOTA_PREPARATION(raceId, packet, raceType);

            handle_RACE_FOTA_GET_VERSION(raceId, packet, raceType);
        }
    };

    public void sendRoleSwitchInd(){
        RaceIndRoleSwitch raceIndRoleSwitch = new RaceIndRoleSwitch();

        mAirohaLink.sendCommand(raceIndRoleSwitch.getRaw());
    }

    private void handle_RACE_FOTA_ACTIVE_FOTA_PREPARATION(int raceId, byte[] packet, int raceType){
        if(raceId != RaceId.RACE_FOTA_ACTIVE_FOTA_PREPARATION)
            return;

        byte agentOrCliet = packet[6];

        RaceRespActiveFotaPreparation resp = new RaceRespActiveFotaPreparation();
        RaceIndActiveFotaPreparation ind = new RaceIndActiveFotaPreparation(agentOrCliet);

        mAirohaLink.sendCommand(resp.getRaw());
        mAirohaLink.sendCommand(ind.getRaw());
    }

    private void handle_RACE_FOTA_GET_VERSION(int raceId, byte[] packet, int raceType) {
        if(raceId != RaceId.RACE_FOTA_GET_VERSION)
            return;

        byte agentOrClient = packet[6];

        RaceRespFotaGetVersion resp = new RaceRespFotaGetVersion();
        RaceIndFotaGetVersion ind = new RaceIndFotaGetVersion(agentOrClient);

        mAirohaLink.sendCommand(resp.getRaw());
        mAirohaLink.sendCommand(ind.getRaw());
    }
}
