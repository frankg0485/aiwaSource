package com.airoha.android.lib.fota.stage;

import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.RaceCmdWriteNv;
import com.airoha.android.lib.fota.StatusCode;

public class FotaStage_WriteNV extends FotaStage implements IAirohaFotaStage {

    private int mNvKey;
    private byte[] mNvValue;

    public FotaStage_WriteNV(AirohaRaceOtaMgr mgr, int nvKey, byte[] nvValue) {
        super(mgr);
        mRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRaceRespType = RaceType.RESPONSE;

        mNvKey = nvKey;
        mNvValue = nvValue;
    }

    @Override
    public void genRacePackets() {
//        byte[] payload = {
//                // nvkey 0x3A00
//                (byte) 0x00, (byte) 0x3A,
//                // payload
//                (byte) 0x00
//        };

        RaceCmdWriteNv cmd = new RaceCmdWriteNv(mNvKey, mNvValue);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_NVKEY_WRITEFULLKEY resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
