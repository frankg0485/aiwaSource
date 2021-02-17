package com.airoha.android.lib.Common;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

public class ReloadNvkeyToRam extends FotaStage {
    private short mNvkeyid;
    public ReloadNvkeyToRam(AirohaRaceOtaMgr mgr, short nvkey_id) {
        super(mgr);
        mNvkeyid = nvkey_id;
        mRaceId = RaceId.RACE_RELOAD_NVKEY_TO_RAM;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        byte[] payload = Converter.ShortToBytes(mNvkeyid);
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_RELOAD_NVKEY_TO_RAM, payload);
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
