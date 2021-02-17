package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdStartTransaction;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayOutputStream;

public class FotaStage_01_StartTranscation extends FotaStage {

    public FotaStage_01_StartTranscation(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_START_TRANSCATION;
        mRaceRespType = RaceType.INDICATION;
    }

    @Override
    public void genRacePackets() {
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

        for(int i = 0; i< gRespQueryPartitionInfos.length; i++) {
            byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_START_TRANSCATION, payload);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_START_TRANSCATION resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }

        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]
    }


}
