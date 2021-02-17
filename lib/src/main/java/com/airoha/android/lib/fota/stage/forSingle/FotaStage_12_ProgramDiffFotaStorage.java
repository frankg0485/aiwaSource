package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdStoragePageProgram;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.StoragePageData;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.CRC8;
import com.airoha.android.lib.util.ContentChecker;
import com.airoha.android.lib.util.Converter;

import java.util.Arrays;
import java.util.Stack;

import static com.airoha.android.lib.util.Converter.bytesToInt32;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_12_ProgramDiffFotaStorage extends FotaStage {

    private static final int INT_256 = 0x100;

    private int mInitialQueuedSize = 0;

    private int mResonseCounter = 0;

    private byte mPageCount = (byte) 0x01;

    public FotaStage_12_ProgramDiffFotaStorage(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_STORAGE_PAGE_PROGRAM;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {

        for(PARTITION_DATA tmp : gSingleDeviceDiffPartitionsList){ // gSingleDeviceDiffPartitionsList is reversed order by 4K

            if (tmp.mIsDiff == false) {
                continue;
            }

            Stack<StoragePageData> pageDataLinkedList = new Stack<StoragePageData>(); // pop the stack to Cmd Queue
            int dataStartIndex = 0;
            int beginAddr = bytesToInt32(tmp.mAddr);
            final int endAddr = beginAddr + tmp.mDataLen;

            // create command lists
            while (beginAddr < endAddr) {
                byte[] payload = new byte[261];
                Arrays.fill(payload, (byte) 0x00);

                int dataLen = INT_256;
                if ((beginAddr + dataLen) > endAddr) {
                    dataLen = endAddr - beginAddr;
                }

                byte[] data = new byte[INT_256];
                Arrays.fill(data, (byte) 0xFF);
                System.arraycopy(tmp.mData, dataStartIndex, data, 0, dataLen);

                if (!ContentChecker.isAllDummyHexFF(data)) {
                    // CRC
                    CRC8 crc8 = new CRC8((byte) 0x00);
                    crc8.update(data);
                    byte crc = (byte) crc8.getValue();
                    payload[0] = crc;

                    // Address
                    byte[] bytesAddr = Converter.intToByteArray(beginAddr);
                    System.arraycopy(bytesAddr, 0, payload, 1, 4);

                    // Data
                    System.arraycopy(data, 0, payload, 5, data.length);

                    pageDataLinkedList.push(new StoragePageData(crc, bytesAddr, data));
                }


                dataStartIndex += INT_256;
                beginAddr += INT_256;
            }

            // pop the stack, cmd queue reverse order by 256
            while(!pageDataLinkedList.empty()) {
                // pop the stack
                StoragePageData storagePageData = pageDataLinkedList.pop();

                byte storageType = gRespQueryPartitionInfos[0].StorageType;

                StoragePageData[] spds = new StoragePageData[]{storagePageData};

                RacePacket racePacketToSend = new RaceCmdStoragePageProgram(storageType, (byte) spds.length, spds);
                String key = Converter.byte2HexStr(spds[0].StorageAddress);

                // to Cmd Queue
                placeCmd(racePacketToSend, key);
            }
        }

        mInitialQueuedSize = mCmdPacketQueue.size();
        mResonseCounter = 0; // reset
    }

    @Override
    protected void placeCmd(RacePacket cmd, String key) {
        cmd.setQueryKey(key);
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(key, cmd); //just for recording
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_STORAGE_PAGE_PROGRAM resp status: " + status);

//        "Status (1 byte),
//        StorageType (1 byte),
//        CompletedPageCount (1 byte),
//        StorageAddress[%CompletedPageCount% * 4]"

        byte storageTyp = packet[RacePacket.IDX_PAYLOAD_START + 1];
        byte completedPageCount = packet[RacePacket.IDX_PAYLOAD_START + 2];

        assert completedPageCount == 1;

        byte[] storageAddress = new byte[4*completedPageCount]; // TODO could be more that 1
        System.arraycopy(packet, RacePacket.IDX_PAYLOAD_START+3, storageAddress, 0, storageAddress.length);

//        byte[] address = new byte[4];

//        int idx = RacePacket.IDX_PAYLOAD_START + 1;
//        System.arraycopy(packet, idx, address, 0, 4);

//        mAirohaLink.logToFile(TAG, "address: " + Converter.byte2HexStr(address) + ", status: " + status);

        mResonseCounter++;
//        mOtaMgr.notifyAppListnerStatus(String.format("Programming: %d / %d", mResonseCounter, mInitialQueuedSize));

        mAirohaLink.logToFile(TAG, String.format("Programming: %d / %d", mResonseCounter, mInitialQueuedSize));
        mAirohaLink.logToFile(TAG, String.format("Current queue size: %d", mCmdPacketQueue.size()));


//        RacePacket racePacket = new RacePacket(packet);
//        byte[] respAddr = racePacket.getAddr();

        for(int i = 0; i< completedPageCount; i++) {

            byte[] respAddr = new byte[4];

            System.arraycopy(storageAddress, i*4, respAddr, 0, 4);

            RacePacket cmd = mCmdPacketMap.get(Converter.byte2HexStr(respAddr));
            if (cmd != null) {
                if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
                    mAirohaLink.logToFile(TAG, "cmd.setIsRespStatusSuccess()");
                    cmd.setIsRespStatusSuccess();
                } else {
                    mAirohaLink.logToFile(TAG, "cmd status = " + Converter.byte2HexStr(status));
                    //cmd.increaseRetryCounter();
                    return;
                }
            }
        }
    }

    @Override
    public boolean isCompleted() {
        for (RacePacket cmd : mCmdPacketMap.values()) {
            if (!cmd.isRespStatusSuccess()) {
                mOtaMgr.notifyAppListenerWarning("addr is not resp yet: " + cmd.getQueryKey());
                return false;
            }
        }

        logCompletedTime();
        mOtaMgr.clearAppListenerWarning();
        return true;
    }


}
