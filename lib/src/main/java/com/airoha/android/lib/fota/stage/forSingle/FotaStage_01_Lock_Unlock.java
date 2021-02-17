package com.airoha.android.lib.fota.stage.forSingle;

import androidx.annotation.NonNull;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_01_Lock_Unlock extends FotaStage {

    private byte mLockUnlock;

    public FotaStage_01_Lock_Unlock(AirohaRaceOtaMgr mgr, boolean isLock) {
        super(mgr);
        mRaceId = RaceId.RACE_STORAGE_LOCK_UNLOCK;
        mRaceRespType = RaceType.INDICATION;

        mLockUnlock = (byte) (isLock ? 0x01 : 0x00);
    }

    @NonNull
    private RacePacket createRacePacket() throws IOException {
        //"RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    LockOrUnlock (1 byte)
        //} [%RecipientCount%]"

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

        for (int i = 0; i < gRespQueryPartitionInfos.length; i++) {
            byte[] recipientInfo = new byte[]{
                    gRespQueryPartitionInfos[i].Recipient,
                    gRespQueryPartitionInfos[i].StorageType,
                    mLockUnlock
            };

            byteArrayOutputStream.write(recipientInfo);
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_LOCK_UNLOCK);
        cmd.setPayload(payload);

        return cmd;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = null;
        try {
            cmd = createRacePacket();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        placeCmd(cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_STORAGE_LOCK_UNLOCK resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }

        // Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    LockOrUnlock (1 byte)
        //} [%RecipientCount%]
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }
}
