package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage_WriteNV;

public class FotaStage_WriteNVRelay extends FotaStage_WriteNV {
    public FotaStage_WriteNVRelay(AirohaRaceOtaMgr mgr, int nvKey, byte[] nvValue) {
        super(mgr, nvKey, nvValue);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRelayRaceRespType = RaceType.RESPONSE;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd);
    }

    @Override
    public void genRacePackets() {
        super.genRacePackets();
    }
}
