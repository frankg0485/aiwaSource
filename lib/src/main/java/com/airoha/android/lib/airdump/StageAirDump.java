package com.airoha.android.lib.airdump;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.stage.MmiStage;

public class StageAirDump extends MmiStage {

    public byte[] payload = new byte[1];
    private byte[] _raw;
    public StageAirDump(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_AIRDUMP_ONOFF;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, mRaceId, payload);

        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp

        _raw = cmd.getRaw();
    }

    public byte[] getRaw() { return _raw; }

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
