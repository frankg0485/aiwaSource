package com.airoha.android.lib.mmi.stage;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public interface IAirohaMmiStage {

//    void genRacePackets();

    void start();

    void prePoolCmdQueue();

    void pollCmdQueue();

    void handleResp(final int raceId, final byte[] packet, int raceType);

//    void parsePayloadAndCheckCompeted(final int raceId, final byte[] packet, byte status, int raceType);

    boolean isCompleted();

    boolean isRespStatusSuccess();

    byte getStatus();

    boolean isCmdQueueEmpty();

    boolean isRetryUpToLimit();


    byte getRespType();

    boolean isExpectedResp(int raceId, int raceType, byte[] packet);

    void stop();

    boolean isStopped();

    int getTotalTaskCount();

    int getCompletedTaskCount();

    boolean isErrorOccurred();

    String getErrorReason();

    String getSimpleName();
}
