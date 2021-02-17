package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_05_Commit;

public class FotaStageDualCommit extends FotaStage {
    public FotaStageDualCommit(AirohaRaceOtaMgr mgr) {
        super(mgr);
    }

    @Override
    public void start() {
        FotaStage commitRelay = new FotaStage_05_CommitRelay(this.mOtaMgr);
        FotaStage commit = new FotaStage_05_Commit(this.mOtaMgr);

        commitRelay.start();
        commit.start();
    }
}
