        package com.airoha.android.lib.peq;

/**
 * Airoha internal use
 */
public class PeqStageLoadUiDataRelay extends PeqStageLoadUiData {

    public PeqStageLoadUiDataRelay(AirohaPeqMgr mgr, byte[] queryId) {
        super(mgr, queryId);

        mIsRelay = true;
    }
}
