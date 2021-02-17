package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FotaStage_WriteState extends FotaStage {

    private int mStateEnum;

    public FotaStage_WriteState(AirohaRaceOtaMgr mgr, int writeState) {
        super(mgr);

        mRaceId = RaceId.RACE_FOTA_WRITE_STATE;
        mRaceRespType = RaceType.INDICATION;

        mStateEnum = writeState;
    }

    @Override
    public void genRacePackets() {

        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    FotaState (2 bytes)
        //} [%RecipientCount%]

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

        assert gRespQueryPartitionInfos.length == 1;

        for (int i = 0; i < gRespQueryPartitionInfos.length; i++) {
            byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
            try {
                byteArrayOutputStream.write(Converter.ShortToBytes((short) mStateEnum));
            } catch (IOException e) {
                e.printStackTrace();

                return;
            }
        }


        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_WRITE_STATE, payload);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_WRITE_STATE resp status: " + status);

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
        //    Recipient (1 byte),
        //    FotaState (2 bytes)
        //} [%RecipientCount%]
    }
}
