package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.RaceCmdGetStoragePartitionSHA256;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.SHA256;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FotaStage_14_CompareFileSystemPartition extends FotaStage {

    private byte[] mFileSystemBinSha256;

    protected File mFile;

    public FotaStage_14_CompareFileSystemPartition(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRaceRespType = RaceType.INDICATION;
    }

    public FotaStage_14_CompareFileSystemPartition(AirohaRaceOtaMgr mgr, File file) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRaceRespType = RaceType.INDICATION;

        mFile = file;
    }
    @Override
    public void genRacePackets() {

        try {
            mFileSystemBinSha256 = SHA256.calculate(Files.toByteArray(mFile));

            mAirohaLink.logToFile(TAG, "FileSystem Bin SHA256"
                    + Converter.byte2HexStr(mFileSystemBinSha256));
        } catch (IOException e) {
            e.printStackTrace();
        }


        assert gRespQueryPartitionInfos.length == 1;
        RacePacket racePacket = new RaceCmdGetStoragePartitionSHA256(
                gRespQueryPartitionInfos[0].Recipient, gRespQueryPartitionInfos[0].StorageType,
                gRespQueryPartitionInfos[0].Address, Converter.intToByteArray(mOtaMgr.getFotaFileSystemInputStreamSize()));

        placeCmd(racePacket);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        RacePacket cmd = mCmdPacketMap.get(TAG);
        if(cmd!=null){
            if (status == StatusCode.FOTA_ERRCODE_SUCESS){
                cmd.setIsRespStatusSuccess();
            } else {
                //cmd.increaseRetryCounter();
                return;
            }
        }


        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes),
        //    SHA256 (32 bytes)
        //} [%RecipientCount%]

        int idx = RacePacket.IDX_PAYLOAD_START + 1;
        byte recipientCount = packet[idx];
        idx = idx + 1;
        assert recipientCount == 1; // 2018.10.31 testing

        byte recipient = packet[idx];
        idx = idx + 1;

        byte storageType = packet[idx];
        idx = idx + 1;

        byte[] partitionAddress = new byte[4];
        System.arraycopy(packet, idx, partitionAddress, 0, 4);
        idx = idx + 4;

        byte[] partitionLength = new byte[4];
        System.arraycopy(packet, idx, partitionLength, 0, 4);
        idx = idx + 4;

        byte[] sha256 = new byte[32];
        System.arraycopy(packet, idx, sha256, 0, 32);
        idx = idx + 32;

        mAirohaLink.logToFile(TAG, "resp storageType "
                + Converter.byte2HexStr(storageType));

        mAirohaLink.logToFile(TAG, "resp role: "
                + Converter.byte2HexStr(recipient));

        mAirohaLink.logToFile(TAG, "resp partitionAddress: "
                + Converter.byte2HexStr(partitionAddress));

        mAirohaLink.logToFile(TAG, "resp partitionLength: "
                + Converter.byte2HexStr(partitionLength));

        mAirohaLink.logToFile(TAG, "resp sha256: "
                + Converter.byte2HexStr(sha256));

        if(!Arrays.equals(sha256, mFileSystemBinSha256)){
            mOtaMgr.notifyAppListenerError("Checking updated FileSystem Fail");
            mIsErrorOccurred = true;
        }
    }
}
