package com.airoha.android.lib.offlinedump;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.stage.MmiStage;

public class StageAssert extends MmiStage {

    String TAG = "StageAssert";

    public StageAssert(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_TOOL_ASSERT;
        mRaceRespType = RaceType.CMD_NO_RESP;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = new RacePacket(RaceType.CMD_NO_RESP, mRaceId);

        placeCmd(cmd);
    }

    public void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {

    }
}
