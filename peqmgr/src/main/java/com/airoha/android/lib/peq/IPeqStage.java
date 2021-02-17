package com.airoha.android.lib.peq;

/**
 * Airoha internal use
 */
public interface IPeqStage {
    void sendCmd();

    void handleRespOrInd(int raceId, byte[] packet, int raceType);

    boolean isError();

    boolean isCompleted();
}
