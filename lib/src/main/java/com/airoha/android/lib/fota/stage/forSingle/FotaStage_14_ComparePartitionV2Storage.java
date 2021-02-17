package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.RaceCmdGetStoragePartitionSHA256;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.ContentConcatenater;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.SHA256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.airoha.android.lib.util.Converter.byte2HexStr;
import static com.airoha.android.lib.util.Converter.intToByteArray;

public class FotaStage_14_ComparePartitionV2Storage extends FotaStage {

    public FotaStage_14_ComparePartitionV2Storage(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRaceRespType = RaceType.INDICATION;
    }


    private int mInitialQueuedSize = 0;
    private int mResonseCounter = 0;

    private HashMap<Byte, String> mTargetSHA256_1_mapKey;
    private HashMap<Byte, String> mLastNotErasedAddressMap;

    private HashMap<Byte, ArrayList<PARTITION_DATA>> mTargetSHA256_1_pds;
    private HashMap<Byte, PARTITION_DATA> mLastNotErasedPartitionDataMap;
    private HashMap<Byte, PARTITION_DATA> mTheLastPd;

    private HashMap<Byte, byte[]> mTargetSHA256_1;
    private HashMap<Byte, byte[]> mLastNotErasedSha256Map;

    private HashMap<Byte, byte[]> mRealSHA256_1;
    private HashMap<Byte, byte[]> mRealSHA256_2;

    @Override
    public void genRacePackets() {

        mTargetSHA256_1_mapKey = new HashMap<>(); // store an address + data length
        mLastNotErasedAddressMap = new HashMap<>(); // store an address
        mTargetSHA256_1_pds = new HashMap<>(); // store the collection of partition data for SHA256
        mLastNotErasedPartitionDataMap = new HashMap<>(); // store the last not erased partition
        mTheLastPd = new HashMap<>(); // store the last partition
        mTargetSHA256_1 = new HashMap<>(); // store the SHA256 of partition collection
        mLastNotErasedSha256Map = new HashMap<>(); // store the SHA256 of the last not erased partition
        mRealSHA256_1 = new HashMap<>(); //
        mRealSHA256_2 = new HashMap<>(); //

        try {
//            ArrayList<PARTITION_DATA> partitionList = new ArrayList(gSingleDeviceDiffPartitions.values());
            List<PARTITION_DATA> partitionList = gSingleDeviceDiffPartitionsList;

            PARTITION_DATA agentFirstPd = partitionList.get(0);
//            if (!agentFirstPd.mIsErased) {
                generateCmd(Recipient.DontCare, partitionList);
//            }

        } catch (Exception e) {
            mOtaMgr.notifyAppListenerError(e.getMessage());
            return;
        }

        mInitialQueuedSize = mCmdPacketQueue.size();
        mResonseCounter = 0; // reset
    }

    private void generateCmd(Byte role, List<PARTITION_DATA> partitionList) {
        int totalPartitionNum = partitionList.size();

        //if (Arrays.equals(mTheLastPd.get(role).mAddr, mLastNotErasedPartitionDataMap.get(role).mAddr)) {
        //                            /// all partitions are the same, so skip all stages.
        //                            ret = SKIP_TYPE.All_stages;
        mTheLastPd.put(role, partitionList.get(totalPartitionNum - 1));

        int theLastNotErasedIndex = -1;
        for (int i = totalPartitionNum - 1; i >= 0; --i) {
            PARTITION_DATA tmp = partitionList.get(i);
            if (tmp.mIsErased == false) {
                theLastNotErasedIndex = i;
                break;
            }
        }

        if (theLastNotErasedIndex >= 0) {
            PARTITION_DATA theLastNotErasedPartitionData = partitionList.get(theLastNotErasedIndex);
            mLastNotErasedPartitionDataMap.put(role, theLastNotErasedPartitionData);
//            mLastNotErasedAddressMap.put(role, byte2HexStr(theLastNotErasedPartitionData.mAddr) + byte2HexStr(role));
            mLastNotErasedSha256Map.put(role, theLastNotErasedPartitionData.mSHA256);
            byte[] dataLen = intToByteArray(theLastNotErasedPartitionData.mDataLen);

            mAirohaLink.logToFile(TAG, "target role: "
                    + Converter.byte2HexStr(role));

            mAirohaLink.logToFile(TAG, "target LastNotErasedPartitionData_addr: "
                    + Converter.byte2HexStr(theLastNotErasedPartitionData.mAddr));

            mAirohaLink.logToFile(TAG, "target LastNotErasedPartitionData_byteLen: "
                    + Converter.byte2HexStr(dataLen));

            mAirohaLink.logToFile(TAG, "target LastNotErasedPartitionDataSHA256: "
                    + Converter.byte2HexStr(theLastNotErasedPartitionData.mSHA256));
            
            assert gRespQueryPartitionInfos.length == 1;
            RacePacket racePacket = new RaceCmdGetStoragePartitionSHA256(
                    gRespQueryPartitionInfos[0].Recipient, gRespQueryPartitionInfos[0].StorageType,
                    theLastNotErasedPartitionData.mAddr, dataLen);

//            String key = mLastNotErasedAddressMap.get(role);
            String key = Converter.byte2HexStr(theLastNotErasedPartitionData.mAddr) +
                    Converter.byte2HexStr(dataLen);

            mLastNotErasedAddressMap.put(role, key);

            placeCmd(racePacket, key);
        }

        if (theLastNotErasedIndex > 0) {
            ArrayList<PARTITION_DATA> sha256_1_pd_list = new ArrayList<>();
            mTargetSHA256_1_pds.put(role, sha256_1_pd_list);
            byte[] sha256_1_data = new byte[0];
//            for (int i = 0; i < theLastNotErasedIndex; ++i) {
//                PARTITION_DATA tmp = partitionList.get(i);
//                if (tmp.mIsErased == false) {
//                    sha256_1_pd_list.add(tmp);
//                    sha256_1_data = ContentConcatenater.concatenateByteArrays(sha256_1_data, tmp.mData);
//                } else {
//                    break;
//                }
//            }
            for (int i = theLastNotErasedIndex - 1; i >= 0; i--) {
                PARTITION_DATA tmp = partitionList.get(i);
                if (tmp.mIsErased == false) {
                    sha256_1_pd_list.add(tmp);
                    sha256_1_data = ContentConcatenater.concatenateByteArrays(sha256_1_data, tmp.mData);
                } else {
                    break;
                }
            }


            mTargetSHA256_1.put(role, SHA256.calculate(sha256_1_data)); // calculate the concated datas

//            byte[] sha256_1_addr = partitionList.get(0).mAddr;
            byte[] sha256_1_addr = partitionList.get(theLastNotErasedIndex -1).mAddr;
            String sha256_1_strAddr = byte2HexStr(sha256_1_addr);
//            mTargetSHA256_1_mapKey.put(role, sha256_1_strAddr + byte2HexStr(role));
            int sha256_1_len = 0;
//            for (PARTITION_DATA pd : mTargetSHA256_1_pds.get(role)) {
//                sha256_1_len += pd.mDataLen;
//            }
            sha256_1_len = sha256_1_data.length;

            if (sha256_1_len == 0) {
                // skip the SHA256_1 comparision
                mTargetSHA256_1.put(role, new byte[0]);
                mRealSHA256_1.put(role, new byte[0]);
            } else {
                byte[] sha256_1_byteLen = intToByteArray(sha256_1_len);

                assert gRespQueryPartitionInfos.length == 1;
                RacePacket racePacket = new RaceCmdGetStoragePartitionSHA256(
                        gRespQueryPartitionInfos[0].Recipient, gRespQueryPartitionInfos[0].StorageType,
                        sha256_1_addr, sha256_1_byteLen);

//                String key = mTargetSHA256_1_mapKey.get(role);
                String key = Converter.byte2HexStr(sha256_1_addr) +
                        Converter.byte2HexStr(sha256_1_byteLen);

                mTargetSHA256_1_mapKey.put(role, key);

                placeCmd(racePacket, key);

                mAirohaLink.logToFile(TAG, "target role: "
                        + Converter.byte2HexStr(role));

                mAirohaLink.logToFile(TAG, "target sha256_1_addr: "
                        + Converter.byte2HexStr(sha256_1_addr));

                mAirohaLink.logToFile(TAG, "target sha256_1_byteLen: "
                        + Converter.byte2HexStr(sha256_1_byteLen));

                mAirohaLink.logToFile(TAG, "target targetSHA256_1: "
                        + Converter.byte2HexStr(mTargetSHA256_1.get(role)));
            }
        }
    }

    protected void placeCmd(RacePacket racePacket, String key) {
        racePacket.setQueryKey(key);
        mCmdPacketQueue.offer(racePacket);
        mCmdPacketMap.put(key, racePacket);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        if(raceType != RaceType.INDICATION)
            return;

        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes),
        //    SHA256 (32 bytes)
        //} [%RecipientCount%]


        mAirohaLink.logToFile(TAG, "resp status: " + status);
        mResonseCounter++;
//        mOtaMgr.notifyAppListnerStatus(String.format("Comparing: %d / %d", mResonseCounter, mInitialQueuedSize));

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

        String strAddr = byte2HexStr(partitionAddress);
//        String mapKey = strAddr + byte2HexStr(recipient);

        String mapKey = Converter.byte2HexStr(partitionAddress) + Converter.byte2HexStr(partitionLength);

        if (mapKey.equals(mTargetSHA256_1_mapKey.get(recipient))) {
            mRealSHA256_1.put(recipient, sha256);
        } else if (mapKey.equals(mLastNotErasedAddressMap.get(recipient))) {
            mRealSHA256_2.put(recipient, sha256);
        }

        RacePacket cmd = mCmdPacketMap.get(mapKey);
        if(cmd!=null){
            if (status == StatusCode.FOTA_ERRCODE_SUCESS){
                cmd.setIsRespStatusSuccess();
            } else {
                //cmd.increaseRetryCounter();
                return;
            }
        }

    }

    @Override
    public boolean isCompleted() {
        for(RacePacket cmd : mCmdPacketMap.values()){
            if(!cmd.isRespStatusSuccess()){
                String msg = cmd.getQueryKey() + "is not resp yet";

                mOtaMgr.notifyAppListenerWarning(msg);
                return false;
            }
        }

        mOtaMgr.clearAppListenerWarning();

        SKIP_TYPE agentSkipType = getSkipType(Recipient.DontCare);//getSkipType(AGENT);
        SKIP_TYPE clientSkipType = SKIP_TYPE.All_stages;

        if (agentSkipType == SKIP_TYPE.All_stages) {
            if (clientSkipType == SKIP_TYPE.All_stages || clientSkipType == SKIP_TYPE.Erase_stages){
                mSkipType = SKIP_TYPE.All_stages;
            } else {
                mSkipType = SKIP_TYPE.Program_stages;
            }
        } else if (agentSkipType == SKIP_TYPE.Erase_stages &&
                (clientSkipType == SKIP_TYPE.All_stages || clientSkipType == SKIP_TYPE.Erase_stages)){
            mSkipType = SKIP_TYPE.Erase_stages;
        }

        logCompletedTime();
        return true;
    }

    SKIP_TYPE getSkipType(Byte role) {
        SKIP_TYPE ret = SKIP_TYPE.None;

        if (mLastNotErasedSha256Map.containsKey(role)) {
            mAirohaLink.logToFile(TAG, "role: "
                    + Converter.byte2HexStr(role));

            mAirohaLink.logToFile(TAG, "mLastNotErasedSha256Map: "
                    + Converter.byte2HexStr(mLastNotErasedSha256Map.get(role)));

            mAirohaLink.logToFile(TAG, "mRealSHA256_2: "
                    + Converter.byte2HexStr(mRealSHA256_2.get(role)));

            if(Arrays.equals(mLastNotErasedSha256Map.get(role), mRealSHA256_2.get(role))) {
                mLastNotErasedPartitionDataMap.get(role).mIsDiff = false;
            }

            if (!mTargetSHA256_1.containsKey(role) ) {
                if (mLastNotErasedPartitionDataMap.get(role).mIsDiff == false) {
                    ret = SKIP_TYPE.Erase_stages;
                }
            } else {
                mAirohaLink.logToFile(TAG, "mTargetSHA256_1: "
                        + Converter.byte2HexStr(mTargetSHA256_1.get(role)));

                mAirohaLink.logToFile(TAG, "mRealSHA256_1: "
                        + Converter.byte2HexStr(mRealSHA256_1.get(role)));

                if (Arrays.equals(mTargetSHA256_1.get(role), mRealSHA256_1.get(role))) {
//                if(true) {
                    for (PARTITION_DATA pd : mTargetSHA256_1_pds.get(role)) {
                        pd.mIsDiff = false;
                    }

                    if (mLastNotErasedPartitionDataMap.get(role).mIsDiff == false) {
                        if (Arrays.equals(mTheLastPd.get(role).mAddr, mLastNotErasedPartitionDataMap.get(role).mAddr)) {
                            /// all partitions are the same, so skip all stages.
                            ret = SKIP_TYPE.All_stages;
                        } else {
                            /// all the non-erased partitions are the same, so go to program resuming.
                            ret = SKIP_TYPE.Erase_stages;
                        }
                    }
                }
            }
        } else {
            ret = SKIP_TYPE.Erase_stages;
        }

        return ret;
    }
}
