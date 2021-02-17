package com.airoha.android.lib.minidump;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.stage.MmiStage;

public class StageGetDumpAddress extends MmiStage {

    String TAG = "StageGetDumpAddress";

    public StageGetDumpAddress(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_GET_DUMP_ADDR;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, mRaceId);

        placeCmd(cmd);
    }

    public void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_GET_DUMP_ADDR resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
