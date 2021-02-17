package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayOutputStream;

public class FotaStage_05_DetachReset extends FotaStage {

    public FotaStage_05_DetachReset(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_SOFTWARE_RESET;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_SOFTWARE_RESET);
        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
    }
}
