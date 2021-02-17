package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.fotTws.RaceCmdTwsGetBattery;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.fotaError.FotaErrorMsg;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FotaStage_00_FotaStart extends FotaStage {
    private byte[] mRecipients;

    public FotaStage_00_FotaStart(AirohaRaceOtaMgr mgr, byte[] recipients) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_START;
        mRaceRespType = RaceType.INDICATION;

        mRecipients = recipients;
    }

    @Override
    public void genRacePackets() {
        //"RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]"
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(mRecipients.length);
        try {
            outputStream.write(mRecipients);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] payload = outputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_START);
        cmd.setPayload(payload);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }

        //"Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]"

        return;
    }
}
