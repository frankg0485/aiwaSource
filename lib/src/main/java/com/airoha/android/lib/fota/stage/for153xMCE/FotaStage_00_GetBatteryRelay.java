package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetBattery;

public class FotaStage_00_GetBatteryRelay extends FotaStage_00_GetBattery {
//    private byte mAgentOrClient = AgentClientEnum.AGENT;

//    public FotaStage_00_GetBatteryRelay(AirohaRaceOtaMgr mgr, byte agentOrClient) {
//        super(mgr, agentOrClient);
//
//        mAgentOrClient = agentOrClient;
//
//        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
//        mRaceRespType = RaceType.INDICATION;
//
//        mRelayRaceId = RaceId.RACE_BLUETOOTH_GET_BATTERY;
//        mRelayRaceRespType = RaceType.INDICATION;
//
//        mIsRelay = true;
//    }

    public FotaStage_00_GetBatteryRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

//        mAgentOrClient = agentOrClient;

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_BLUETOOTH_GET_BATTERY;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        super.parsePayloadAndCheckCompeted(raceId, packet, status, raceType);
    }
}
