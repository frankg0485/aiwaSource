package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetFwInfo;

public class FotaStage_00_GetFwInfoRelay extends FotaStage_00_GetFwInfo {
    public FotaStage_00_GetFwInfoRelay(AirohaRaceOtaMgr mgr, byte[] recipients) {
        super(mgr, recipients);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_FOTA_GET_AE_INFO;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd);
    }

    @Override
    protected void passToMgr(byte[] info) {
        mOtaMgr.setPartnerFwInfo(info);
    }
}
