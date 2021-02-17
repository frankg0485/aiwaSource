package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.fotTws.RaceCmdTwsGetBattery;
import com.airoha.android.lib.fota.fotaError.FotaErrorMsg;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.AgentClientEnum;

public class FotaStage_00_GetBattery extends FotaStage {
//    public FotaStage_00_GetBatery(AirohaRaceOtaMgr mgr, byte agentOrClient) {
//        super(mgr);
//
//        mAgentOrClient = agentOrClient;
//
//        mRaceId = RaceId.RACE_BLUETOOTH_GET_BATTERY;
//        mRaceRespType = RaceType.INDICATION;
//    }

    public FotaStage_00_GetBattery(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mAgentOrClient = AgentClientEnum.AGENT;

        mRaceId = RaceId.RACE_BLUETOOTH_GET_BATTERY;
        mRaceRespType = RaceType.INDICATION;
    }

    private byte mAgentOrClient = AgentClientEnum.AGENT;

    @Override
    public void genRacePackets() {
        byte[] payload = new byte[]{mAgentOrClient};

        RacePacket cmd = new RaceCmdTwsGetBattery(payload);
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

        //Status (1 byte),
        //AgentOrClient (1 byte),
        //BatteryStatus (1 byte)

        byte agentOrClient = packet[RacePacket.IDX_PAYLOAD_START + 1];
        byte batteryStatus = packet[RacePacket.IDX_PAYLOAD_START + 2];

        mAirohaLink.logToFile(TAG, String.format("target battery level: %d", mOtaMgr.getFotaDualSettings().batteryThreshold));

        mAirohaLink.logToFile(TAG, String.format("agentOrClient: %d, batteryStatus: %d", agentOrClient, batteryStatus));

        if((batteryStatus & 0xFF) < mOtaMgr.getFotaDualSettings().batteryThreshold) {
            mOtaMgr.notifyBatteryLevelLow();
            mOtaMgr.setFlashOperationAllowed(false);
            mIsErrorOccurred = true;
            mStrErrorReason = FotaErrorMsg.BatteryLow;
        }else {
            mOtaMgr.setFlashOperationAllowed(true);
        }

        return;
    }
}
