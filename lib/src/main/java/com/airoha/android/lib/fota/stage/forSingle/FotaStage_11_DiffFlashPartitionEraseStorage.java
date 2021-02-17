package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdStoragePartitionErase;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FotaStage_11_DiffFlashPartitionEraseStorage extends FotaStage {

    public FotaStage_11_DiffFlashPartitionEraseStorage(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_STORAGE_PARTITION_ERASE;
        mRaceRespType = RaceType.INDICATION;
    }

    private int mInitialQueuedSize = 0;

    private int mResonseCounter = 0;


    @Override
    public void genRacePackets() {

//        Iterator itor = gSingleDeviceDiffPartitions.values().iterator();
//
//        List<PARTITION_DATA> listOfPartitionData = new LinkedList<>(gSingleDeviceDiffPartitions.values());
//
//        Collections.reverse(listOfPartitionData);
//
//        itor = listOfPartitionData.iterator();

        /// Because the partition list is reversed at FotaStage_13_GetPartitionEraseStatusStorage,
        /// we need to reserve it again for erasing.
        Collections.reverse(gSingleDeviceDiffPartitionsList);

        Iterator itor = gSingleDeviceDiffPartitionsList.iterator();

        /// one cmd packet with different agent address and client address.
        while(itor.hasNext()) {
            PARTITION_DATA tmp = (PARTITION_DATA) itor.next();
            if ((tmp.mIsDiff == true) && (tmp.mIsErased == false)) {

                //EraseInfoCount (1 byte),
                //{
                //    Recipient (1 byte),
                //    StorageType (1 byte),
                //    Address (4 bytes),
                //    Length (4 bytes)
                //} [%EraseInfoCount%]

                assert gRespQueryPartitionInfos.length == 1;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

                try {
                    for (int i = 0; i< gRespQueryPartitionInfos.length; i++){
                        byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
                        byteArrayOutputStream.write(gRespQueryPartitionInfos[i].StorageType);
                        byteArrayOutputStream.write(tmp.mAddr);
                        byteArrayOutputStream.write(Converter.intToByteArray(tmp.mDataLen));
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                byte[] payload = byteArrayOutputStream.toByteArray();

                RacePacket racePacket = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_PARTITION_ERASE);
                racePacket.setPayload(payload);
                String key = Converter.byte2HexStr(tmp.mAddr);

                placeCmd(racePacket, key);
            }
        }

        /// After all the erase packets are generated, the partition list should be reserved again for programming.
        Collections.reverse(gSingleDeviceDiffPartitionsList);

        mInitialQueuedSize = mCmdPacketQueue.size();
        mResonseCounter = 0; // reset
    }

    @Override
    protected void placeCmd(RacePacket cmd, String key) {
        cmd.setQueryKey(key);
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(key, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "FotaStage_11_DiffFlashPartitionEraseStorage resp status: " + status);
        mResonseCounter++;
//        mOtaMgr.notifyAppListnerStatus(String.format("Erasing: %d / %d", mResonseCounter, mInitialQueuedSize));

        //Status (1 byte),
        //EraseInfoCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes)
        //} [%EraseInfoCount%]
        assert packet[RacePacket.IDX_PAYLOAD_START+1] == 0x01; // 2018.10.30 testing

        int idx = RacePacket.IDX_PAYLOAD_START+4;
        byte[] respAddr = Arrays.copyOfRange(packet, idx, idx+4);

        RacePacket cmd = mCmdPacketMap.get(Converter.byte2HexStr(respAddr));
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
                mOtaMgr.notifyAppListenerWarning("addr is not resp yet: " + cmd.getQueryKey());
                return false;
            }
        }

        logCompletedTime();
        mOtaMgr.clearAppListenerWarning();
        return true;
    }
}
