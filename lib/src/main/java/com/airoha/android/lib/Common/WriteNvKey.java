package com.airoha.android.lib.Common;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

public class WriteNvKey extends FotaStage {
    private short mNvkeyid;
    private byte[] mNvkeyValue;
    public WriteNvKey(AirohaRaceOtaMgr mgr, short nvkey_id, byte[]nvkey_value) {
        super(mgr);
        mNvkeyid = nvkey_id;
        mNvkeyValue = nvkey_value;
        mRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        byte[] payload = new byte[2 + mNvkeyValue.length];
        payload[0] = (byte) (mNvkeyid& 0xFF);
        payload[1] = ((byte) ((mNvkeyid >> 8) & 0xFF));
        System.arraycopy(mNvkeyValue, 0, payload, 2, mNvkeyValue.length);

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_WRITEFULLKEY, payload);
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