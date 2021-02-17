package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.StopSender;

import java.io.ByteArrayOutputStream;

public class FotaStage_06_Stop extends FotaStage {
    private byte[] mRecipients;
    private byte mReason;

    public FotaStage_06_Stop(AirohaRaceOtaMgr mgr, byte[] recipients, byte reason) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_STOP;
        mRaceRespType = RaceType.INDICATION;

        mRecipients = recipients;
        mReason = reason;
    }

    @Override
    public void genRacePackets() {
        //"Sender (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    Reason (1 byte)
        //} [%RecipientCount%]"

        //Sender (1 byte)
        //    0: agent
        //    7: Smart Phone
        //Recipient (1 byte)
        //    0: agent
        //    1, partner
        //    7: Smart Phone
        //Reason (1 byte)
        //    0: cancel
        //    1: fail
        //    2: timeout
        //    3: partner lost
        //    4: Active stop
        //
        //For AB153x, It's implemeted @20181101 on teak-dev-maindev branch."
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(StopSender.SmartPhone); // Smart Phone
        byteArrayOutputStream.write(mRecipients.length); //RecipientCount

        for(int i = 0; i < mRecipients.length; i++){
            byteArrayOutputStream.write(mRecipients[i]);
            byteArrayOutputStream.write(mReason); // Cancel
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_STOP);
        cmd.setPayload(payload);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_STOP resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
