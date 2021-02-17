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

public class FotaStage_00_GetVersion extends FotaStage {
    private byte[] mRecipients;

    public FotaStage_00_GetVersion(AirohaRaceOtaMgr mgr, byte[] recipients) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_GET_VERSION;
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

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_GET_VERSION);
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
        mAirohaLink.logToFile(TAG, "RACE_FOTA_GET_VERSION resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            return;
        }

        //"Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    VersionLength (1 byte),
        //    Version (%VersionLength%)
        //} [%RecipientCount%]"
        int idx = RacePacket.IDX_PAYLOAD_START+1;

        int recipientCount = packet[idx];
        idx = idx + 1;

        if(recipientCount == 1){
            byte recipient = packet[idx];
            idx = idx + 1;

            int versionLength = packet[idx];
            idx = idx + 1;

            byte[] version = new byte[versionLength];
            System.arraycopy(packet, idx, version, 0, versionLength);
            idx = idx + versionLength;

            passToMgr(version);
        }
    }

    protected void passToMgr(byte[] version){
        mOtaMgr.setAgentVersion(version);
    }
}
