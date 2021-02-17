package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.mmi.RaceCmdSwitchPowerMode;
import com.airoha.android.lib.fota.powerMode.ModeEnum;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

public class FotaStage_SwitchPowerMode extends FotaStage {

    protected byte mPowerMode = ModeEnum.UNKNOWN;

    public FotaStage_SwitchPowerMode(AirohaRaceOtaMgr mgr, byte modeEnum) {
        super(mgr);

        mRaceId = RaceId.RACE_SWITCH_POWER_MODE;

        mPowerMode = modeEnum;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = new RaceCmdSwitchPowerMode(new byte[]{mPowerMode});
        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }


    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        //05 5B 03 00 0D 02 00

        mAirohaLink.logToFile(TAG, "RACE_SWITCH_POWER_MODE resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
