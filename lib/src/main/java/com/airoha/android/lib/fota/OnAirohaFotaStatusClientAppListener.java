package com.airoha.android.lib.fota;

import com.airoha.android.lib.fota.actionEnum.DualActionEnum;
import com.airoha.android.lib.fota.actionEnum.SingleActionEnum;
import com.airoha.android.lib.fota.fotaInfo.DualFotaInfo;
import com.airoha.android.lib.fota.fotaInfo.SingleFotaInfo;

/**
 * App should register and listen to the callback for the states of FOTA flow.
 * {@link Airoha153xMceRaceOtaMgr}
 */
public interface OnAirohaFotaStatusClientAppListener {
    void notifyError(String error);

    void notifyStatus(String status);

    void notifyCompleted(String msg);

    void notifyInterrupted(String msg);

    void notifyStateEnum(String state);

    void notifyBatterLevelLow();

    void notifyClientExistence(boolean isClientExisting);

    // for debug ease
    void notifyWarning(String errorMsg);

    void onAvailableDualActionUpdated(DualActionEnum actionEnum);

    void onAvailableSingleActionUpdated(SingleActionEnum actionEnum);

    void onDualFotaInfoUpdated(DualFotaInfo info);

    void onSingleFotaInfoUpdated(SingleFotaInfo info);

    void onProgressUpdated(final String current_stage, final int completed_stage_count, final int total_stage_count, final int completed_task_count, final int total_task_count);
}
