package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdQueryState;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FotaStage_00_GetFwInfo extends FotaStage {
    private byte[] mRecipients;

    public FotaStage_00_GetFwInfo(AirohaRaceOtaMgr mgr, byte[] recipients) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_GET_AE_INFO;
        mRaceRespType = RaceType.INDICATION;

        mRecipients = recipients;
    }

    @Override
    public void genRacePackets() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //"RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]"
        outputStream.write(mRecipients.length);
        try {
            outputStream.write(mRecipients);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] payload = outputStream.toByteArray();

        //Cmd format
        //05 + type + length(2 byte) + CMD id(2 byte) + RecipientCount(1 byte) + Recipient(1 byte)
        // 05  5A     0300             091C                                      FF(0: agent, 1: partner, 0xFF: don't care)
        //Rsp format
        //05 + type + length(2 byte) + CMD id(2 byte) + status(1 byte) + RecipientCount(1 byte) + Recipient(1 byte) + payload

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_GET_AE_INFO);
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
        mAirohaLink.logToFile(TAG, "FotaStage_00_GetFwInfo resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            return;
        }

        //Rsp format
        //05 + type + length(2 byte) + CMD id(2 byte) + status(1 byte) + RecipientCount(1 byte) + Recipient(1 byte) + payload
        int idx = RacePacket.IDX_PAYLOAD_START+1;

        int recipientCount = packet[idx];
        idx = idx + 1;

        byte aeLength = packet[9];
        byte[] info = new byte[aeLength];
        System.arraycopy(packet, 10, info, 0, (int)aeLength);
        passToMgr(info);
    }

    protected void passToMgr(byte[] info){
        mOtaMgr.setAgentFwInfo(info);
    }
}
