package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.mmi.RaceCmdSuspendDsp;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

public class FotaStage_SuspendDsp extends FotaStage {

    public FotaStage_SuspendDsp(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_SUSPEND_DSP;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = new RaceCmdSuspendDsp();

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_SUSPEND_DSP resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
