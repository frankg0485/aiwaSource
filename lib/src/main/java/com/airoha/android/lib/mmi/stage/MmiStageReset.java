package com.airoha.android.lib.mmi.stage;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.mmi.AirohaMmiMgr;

public class MmiStageReset extends MmiStage {
    public MmiStageReset(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_SOFTWARE_RESET;
        mRaceRespType = RaceType.RESPONSE;
        mIsRelay = false;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = new RacePacket(RaceType.CMD_NO_RESP, RaceId.RACE_SOFTWARE_RESET);
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {

    }
}
