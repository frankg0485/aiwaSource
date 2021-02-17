package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

/**
 * Airoha internal use
 */
public class PeqStageUpdatePeqSubset extends PeqStage {
    public PeqStageUpdatePeqSubset(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        return genWriteNvKeyPacket(mPeqMgr.getAudioPathTargetNvKey(), mPeqMgr.getWriteBackPeqSubsetContent());
    }
}
