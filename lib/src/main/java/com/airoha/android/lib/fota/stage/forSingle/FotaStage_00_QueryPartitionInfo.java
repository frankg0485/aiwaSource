package com.airoha.android.lib.fota.stage.forSingle;

import androidx.annotation.NonNull;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.for153xMCE.QueryPartitionInfo;
import com.airoha.android.lib.fota.stage.for153xMCE.RespQueryPartitionInfo;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_00_QueryPartitionInfo extends FotaStage {

    private QueryPartitionInfo[] mQueryPartitionInfos;

    public FotaStage_00_QueryPartitionInfo(AirohaRaceOtaMgr mgr, QueryPartitionInfo[] queryPartitionInfos) {
        super(mgr);
        mRaceId = RaceId.RACE_FOTA_PARTITION_INFO_QUERY;
        mRaceRespType = RaceType.INDICATION;
        mQueryPartitionInfos = queryPartitionInfos;
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
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @NonNull
    protected RacePacket createRacePacket() throws IOException {
        //PartitionInfoCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    PartitionID (1 byte)
        //} [%PartitionInfoCount%]

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(mQueryPartitionInfos.length);
        for (int i = 0; i < mQueryPartitionInfos.length; i++) {
            byteArrayOutputStream.write(mQueryPartitionInfos[i].toRaw());
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_PARTITION_INFO_QUERY, payload);

        return cmd;
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_PARTITION_INFO_QUERY resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            return;
        }

        extractPartitionInfoFromPacket(packet);
    }

    protected void extractPartitionInfoFromPacket(byte[] packet) {
        gRespQueryPartitionInfos = RespQueryPartitionInfo.extractRespPartitionInfo(packet);

        if(gRespQueryPartitionInfos.length==2){
            // check Fota partition and FileSystem partition address

            RespQueryPartitionInfo partitionInfo1 = gRespQueryPartitionInfos[0];
            RespQueryPartitionInfo partitionInfo2 = gRespQueryPartitionInfos[1];

            // for 153X
            if(partitionInfo1.Recipient == Recipient.DontCare && partitionInfo2.Recipient == Recipient.DontCare){
                int addr1 = Converter.bytesToInt32(partitionInfo1.Address);
                int addr2 = Converter.bytesToInt32(partitionInfo2.Address);

                if(addr1 == addr2){
                    mOtaMgr.setNeedToUpdateFileSystem(true);
                }
            }
        }
    }
}
