package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.airoha.android.lib.util.Converter.byte2HexStr;
import static com.airoha.android.lib.util.Converter.intToByteArray;

public class FotaStage_13_GetPartitionEraseStatusStorage extends FotaStage {

    protected int mInitialQueuedSize = 0;
    protected int mResonseCounter = 0;
    protected int mErasedNum = 0;
    protected InputStream mInputStream;

    public FotaStage_13_GetPartitionEraseStatusStorage(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_GET_4K_ERASED_STATUS;
        mRaceRespType = RaceType.INDICATION;
    }

    public FotaStage_13_GetPartitionEraseStatusStorage(AirohaRaceOtaMgr mgr, InputStream ios) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_GET_4K_ERASED_STATUS;
        mRaceRespType = RaceType.INDICATION;

        mInputStream = ios;
    }

    @Override
    public void genRacePackets() {

        // get address
//        int startAddress = mOtaMgr.getFotaPartitionStartAddress();
        int startAddress = Converter.bytesToInt32(gRespQueryPartitionInfos[0].Address);

        InputStream ios = mInputStream;

//        gSingleDeviceDiffPartitions = new LinkedHashMap<String, PARTITION_DATA>();

        LinkedList<PARTITION_DATA> partitionDataList = new LinkedList<>();

        byte[] data = new byte[INT_4K];
        Arrays.fill(data, (byte) 0xFF);

        try {
            int total_len = 0;
            while (true) {
                int len = ios.read(data);
                if (len == -1) {
                    break;
                }

                total_len += INT_4K;

                byte[] bytesAddr = intToByteArray(startAddress);
                String strAddr = byte2HexStr(bytesAddr);

//                gSingleDeviceDiffPartitions.put(strAddr, new PARTITION_DATA(bytesAddr, data, len));

                partitionDataList.add(new PARTITION_DATA(bytesAddr, data, len));

                // shift 4K address
                startAddress = startAddress + INT_4K;
            }

//            Collections.reverse(partitionDataList);

            gSingleDeviceDiffPartitionsList = partitionDataList;

            byte[] totlaByteLen = intToByteArray(total_len);

            //RecipientCount (1 byte),
            //{
            //    Recipient (1 byte),
            //    StorageType (1 byte),
            //    Address (4 bytes),
            //    Length (4 bytes)
            //} [%RecipientCount%]

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert gRespQueryPartitionInfos.length == 1;// 2018.10.30 testing

            byteArrayOutputStream.write(gRespQueryPartitionInfos.length);
            for (int i = 0; i < gRespQueryPartitionInfos.length; i++) {
                byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
                byteArrayOutputStream.write(gRespQueryPartitionInfos[i].StorageType);
                byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Address);
                byteArrayOutputStream.write(totlaByteLen);
            }

            byte[] payload = byteArrayOutputStream.toByteArray();

            assert payload.length == 11; // 2018.10.30 testing

            RacePacket racePacket = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_GET_4K_ERASED_STATUS);
            racePacket.setPayload(payload);

//            RacePacket racePacket = new RaceCmdStorageGetPartitionErasedStatus(mOtaMgr.getRecipient(), mOtaMgr.getFotaStorageType(), startBytesAddr, totlaByteLen);
            placeCmd(racePacket);

        } catch (IOException e) {
            mOtaMgr.notifyAppListenerError(e.getMessage());
            return;
        }

        mInitialQueuedSize = mCmdPacketQueue.size();
        mResonseCounter = 0; // reset
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        if (raceType != RaceType.INDICATION)
            return;

        // Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes),
        //    ErasedStatusLength (2 bytes),
        //    ErasedStatus[%ErasedStatusLength%]
        //} [%RecipientCount%]

        mAirohaLink.logToFile(TAG, "resp status: " + status);
        mResonseCounter++;
//        mOtaMgr.notifyAppListnerStatus(String.format("GetPartitionEraseStatus: %d / %d", mResonseCounter, mInitialQueuedSize));

        int idx = RacePacket.IDX_PAYLOAD_START + 1;

        byte recipientCount = packet[idx];
        idx = idx + 1;

        assert recipientCount == 1; // 2018.10.30 testing

        byte recipient = packet[idx];
        idx = idx + 1;

        byte storageType = packet[idx];
        idx = idx + 1;

        byte[] partitionAddress = new byte[4];
        System.arraycopy(packet, idx, partitionAddress, 0, 4);
        idx = idx + 4;
        mAirohaLink.logToFile(TAG, "partitionAddress: " + Converter.byte2HexStr(partitionAddress));

        byte[] partitionLength = new byte[4];
        System.arraycopy(packet, idx, partitionLength, 0, 4);
        idx = idx + 4;
        mAirohaLink.logToFile(TAG, "partitionLength: " + Converter.byte2HexStr(partitionLength));

        int totalBitNum = Converter.bytesToInt32(partitionLength) / INT_4K;
        mAirohaLink.logToFile(TAG, "totalBitNum: " + String.valueOf(totalBitNum));

        byte[] eraseStatusSize = new byte[2];
        System.arraycopy(packet, idx, eraseStatusSize, 0, 2);
        idx = idx + 2;
        mAirohaLink.logToFile(TAG, "eraseStatusSize: " + Converter.byte2HexStr(eraseStatusSize));

        int eraseStatusByteLen = Converter.BytesToU16(eraseStatusSize[1], eraseStatusSize[0]);
        mAirohaLink.logToFile(TAG, "eraseStatusByteLen: " + String.valueOf(eraseStatusByteLen));

        byte[] eraseStatus = new byte[eraseStatusByteLen];
        System.arraycopy(packet, idx, eraseStatus, 0, eraseStatusByteLen);
        idx = idx + eraseStatusByteLen;
        mAirohaLink.logToFile(TAG, "eraseStatus: " + Converter.byte2HexStr(eraseStatus));

        // 2018.12.07 reverse the eraseStatus
//        byte[] eraseStatusReversed = new byte[eraseStatusByteLen];
//        for(int i = 0; i< eraseStatusByteLen; i++){
//            eraseStatusReversed[eraseStatusByteLen - 1 - i] = eraseStatus[i];
//        }
        // 2018.12.07 reverse bits
//        for(int i = 0; i< eraseStatusByteLen; i++){
//            byte reverted = reverse(eraseStatusReversed[i]);
//            eraseStatusReversed[i] = reverted;
//        }

        mErasedNum = 0;
//        ArrayList<PARTITION_DATA> partitionList = new ArrayList(gSingleDeviceDiffPartitions.values());
//        ArrayList<PARTITION_DATA> partitionList = new ArrayList<>(gSingleDeviceDiffPartitionsList);
        for (int i = 0; i < totalBitNum; ++i) {
            int byteIndex = i / 8;
            int bitOffset = i % 8;
            int bitResult = 0x80 >> bitOffset;
            boolean isErased = ((eraseStatus[byteIndex] & bitResult) == bitResult);
//            partitionList.get(i).mIsErased = isErased;
            gSingleDeviceDiffPartitionsList.get(i).mIsErased = isErased;
            if (isErased) {
                mErasedNum += 1;
            }
        }

        if (mErasedNum == gSingleDeviceDiffPartitionsList.size()) {
            mSkipType = SKIP_TYPE.CompareErase_stages;
        }
//        } else if (partitionList.get(0).mIsErased) {
//        } else if (gSingleDeviceDiffPartitionsList.get(0).mIsErased) {
        // 2018.12.21 [BTA-3021]
//        } else if (gSingleDeviceDiffPartitionsList.getLast().mIsErased) {
//            // Since the first block is erased, we can skip the SHA256 comparision and go to erase resuming.
//            mSkipType = SKIP_TYPE.Compare_stages;
//        }


        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (cmd != null) {
            if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
                mAirohaLink.logToFile(TAG, "cmd success");
                cmd.setIsRespStatusSuccess();

                // 2018.12.10 reverse the list for later
                Collections.reverse(gSingleDeviceDiffPartitionsList);

            } else {
                mAirohaLink.logToFile(TAG, "cmd error");
                //cmd.increaseRetryCounter();
                return;
            }
        }
    }

    public static byte reverse(byte x) {
        byte b = 0;
        for (int i = 0; i < 8; ++i) {
            b<<=1;
            b|=( x &1);
            x>>=1;
        }
        return b;
    }
}
