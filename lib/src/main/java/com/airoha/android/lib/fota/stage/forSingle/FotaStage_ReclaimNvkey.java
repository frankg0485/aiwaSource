package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

public class FotaStage_ReclaimNvkey extends FotaStage {

    private short mReclaimSize;

    public FotaStage_ReclaimNvkey(AirohaRaceOtaMgr mgr, short reclaimSize) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_RECLAIM;
        mRaceRespType = RaceType.RESPONSE;

        mReclaimSize = reclaimSize;
    }

    @Override
    public void genRacePackets() {
        byte[] reclaimSize = Converter.shortToBytes(mReclaimSize);

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_RECLAIM);

        cmd.setPayload(reclaimSize);

        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_NVKEY_RECLAIM resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        // special !!!
        if (status != 0x00){ // special !!!
            cmd.setIsRespStatusSuccess();

            mStatusCode = 0x00; // trick
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
