package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdFotaIntegrityCheck;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.fotaError.FotaErrorMsg;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayOutputStream;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_04_CheckIntegrityStorage extends FotaStage {

    public FotaStage_04_CheckIntegrityStorage(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_FOTA_INTEGRITY_CHECK;
        mRaceRespType = RaceType.INDICATION;
    }

    @Override
    public void genRacePackets() {

        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte)
        //} [%RecipientCount%]


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        assert gRespQueryPartitionInfos.length == 1;
        byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

        for (int i =0; i< gRespQueryPartitionInfos.length; i++){
            byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
            byteArrayOutputStream.write(gRespQueryPartitionInfos[i].StorageType);
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        //StorageType (1 Byte)
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_INTEGRITY_CHECK, payload);
        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_INTEGRITY_CHECK resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            mIsErrorOccurred = true;
            mOtaMgr.notifyAppListenerError(FotaErrorMsg.CheckIntegrityFail);
            return;
        }

        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte)
        //} [%RecipientCount%]
    }
}
