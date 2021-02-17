package com.airoha.android.lib.fota.stage.forSingle;

import androidx.annotation.NonNull;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdQueryState;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.for153xMCE.RespFotaState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FotaStage_00_QueryState extends FotaStage {

    private byte[] mRecipients;
//    private RespFotaState[] mRespFotaStates;

    public FotaStage_00_QueryState(AirohaRaceOtaMgr mgr, byte[] recipients) {
        super(mgr);
        mRaceId = RaceId.RACE_FOTA_QUERY_STATE;
        mRaceRespType = RaceType.INDICATION;

        mRecipients = recipients;
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //"RecipientCount (1 byte),
            //{
            //    Recipient (1 byte)
            //} [%RecipientCount%]"
            outputStream.write(mRecipients.length);
            outputStream.write(mRecipients);


        byte[] payload = outputStream.toByteArray();

        RacePacket cmd = new RaceCmdQueryState();
        cmd.setPayload(payload);
        return cmd;
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_QUERY_STATE resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            return;
        }

        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    FotaState (2 bytes)
        //} [%RecipientCount%]


        RespFotaState[] extracted = RespFotaState.extractRespFotaStates(packet);
        passInfoToMgr(extracted);
    }


    protected void passInfoToMgr(RespFotaState[] respFotaStates) {
        for (RespFotaState respFotaState : respFotaStates) {
            switch (respFotaState.Recipient) {
                case Recipient.DontCare:

                    mOtaMgr.setAgentFotaState(respFotaState.FotaState);
                    break;

                default:
                    break;
            }
        }
    }
}
