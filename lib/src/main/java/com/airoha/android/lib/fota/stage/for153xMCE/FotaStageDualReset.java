package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_05_DetachReset;

public class FotaStageDualReset extends FotaStage {
    public FotaStageDualReset(AirohaRaceOtaMgr mgr) {
        super(mgr);
    }

    @Override
    public void start() {
        FotaStage resetRelay = new FotaStage_05_DetachResetRelay(this.mOtaMgr);
        FotaStage reset = new FotaStage_05_DetachReset(this.mOtaMgr);

        resetRelay.start();
        reset.start();
    }
}
