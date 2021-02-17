package com.airoha.android.lib.fota.stage;

import java.util.LinkedList;

public interface IAirohaFotaStage {

    enum SKIP_TYPE {
        All_stages,
        Compare_stages,
        Erase_stages,
        Program_stages,
        CompareErase_stages,
        Client_Erase_stages,
        Sinlge_StateUpdate_stages,
        None
    }

    void genRacePackets();

    void start();

    void prePoolCmdQueue();

    void pollCmdQueue();

    void handleResp(final int raceId, final byte[] packet, int raceType);

    void parsePayloadAndCheckCompeted(final int raceId, final byte[] packet, byte status, int raceType);

    boolean isCompleted();

    boolean isRespStatusSuccess();

    byte getStatus();

    boolean isCmdQueueEmpty();

    boolean isRetryUpToLimit();

    SKIP_TYPE getSkipType();

    LinkedList<FotaStage> getStagesForSkip(SKIP_TYPE type);

    byte getRespType();

    boolean isExpectedResp(int raceType, int raceId, byte[] packet);

    void stop();

    boolean isStopped();

    int getTotalTaskCount();

    int getCompletedTaskCount();

    boolean isErrorOccurred();

    String getErrorReason();
}
