package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdFotaCommit;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.IAirohaFotaStage;

import java.io.ByteArrayOutputStream;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_05_Commit extends FotaStage {

    public FotaStage_05_Commit(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_FOTA_COMMIT;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        //"RecipientCount (1 byte),
        //{
        //    Recipient (1 byte)
        //} [%RecipientCount%]"

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(gRespQueryPartitionInfos.length);

        for (int i = 0; i < gRespQueryPartitionInfos.length; i++) {
            byteArrayOutputStream.write(gRespQueryPartitionInfos[i].Recipient);
        }

        byte[] payload = byteArrayOutputStream.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_COMMIT, payload);
        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_COMMIT resp status: " + status);

        // Status

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
