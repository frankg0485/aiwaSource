package com.airoha.android.lib.peq;

public class PeqStageUpdatePeqSubsetRelay extends PeqStageUpdatePeqSubset {
    public PeqStageUpdatePeqSubsetRelay(AirohaPeqMgr mgr) {
        super(mgr);

        mIsRelay = true;
    }
}
