package com.airoha.android.lib.fota;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.fota.actionEnum.DualActionEnum;
import com.airoha.android.lib.fota.actionEnum.SingleActionEnum;
import com.airoha.android.lib.fota.fotaInfo.DualFotaInfo;
import com.airoha.android.lib.fota.fotaInfo.SingleFotaInfo;
import com.airoha.android.lib.fota.fotaSetting.FotaDualSettings;
import com.airoha.android.lib.fota.fotaSetting.FotaSingleSettings;
import com.airoha.android.lib.fota.fotaState.StageEnum;
import com.airoha.android.lib.fota.powerMode.ModeEnum;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.IAirohaFotaStage;
import com.airoha.android.lib.fota.stage.StopReason;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_06_StopRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_ResumeDspRelay;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_06_Stop;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_ResumeDsp;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.airoha.android.lib.fota.stage.IAirohaFotaStage.SKIP_TYPE.None;

public class AirohaRaceOtaMgr {
    public static final int ROM_START_ADDRESS = 0;

    private static final String TAG = "AirohaRaceOtaMgr";

    public static int BOOT_REASON = 1;
    public static int DUMP_INFO = 2;
    public static int DUMP_COMPLETE = 3;

    protected AirohaLink mAirohaLink;
    protected int mTotalStageCount;
    protected int mCompletedStageCount;
    // History
//    protected int mSingleFotaState = StageEnum.APP_UNKNOWN;
    protected int mAgentFotaState = StageEnum.APP_UNKNOWN;
    protected int mPartnerFotaState = StageEnum.APP_UNKNOWN;
    protected boolean mIsTws = false;
    // Settings
    protected FotaSingleSettings mFotaSingleSettings = new FotaSingleSettings(); // use default values
    protected FotaDualSettings mFotaDualSettings = new FotaDualSettings(); // use default values
    // Stages
    protected Queue<IAirohaFotaStage> mStagesQueue = new ConcurrentLinkedQueue<>();
    // Battery level
    protected boolean mIsFlashOperationAllowed = false;
    protected boolean mIsNeedToUpdateFileSystem;
    // Info required for the stages
    protected File mFotaAgentBinFile;
    protected File mFotaPartnerBinFile;
    protected File mFotaFileSystemBinFile;
    protected InputStream mFotaAgentInputStream;
    protected InputStream mFotaPartnerInputStream;
    protected InputStream mFotaFileSystemInputStream;

    protected long mFotaAgentInputStreamSize;
    protected long mFotaPartnerInputStreamSize;
    protected long mFotaFileSystemInputStreamSize;

    protected DualFotaInfo mDualFotaInfo = new DualFotaInfo();
    protected SingleFotaInfo mSingleFotaInfo = new SingleFotaInfo();
    private IAirohaFotaStage mCurrentStage;
    private File mFileSystemBinFile;
    private InputStream mFileSystemInputStream;
    private long mFileSystemInputStreamSize;
    private int mFotaPartitionStartAddress = 0xFF; // default set to unknown
    private int mFotaPartitionLength;
    private String mStrAgentStateEnum;
    private String mStrPartnerStateEnum;
    // App listener
    //private OnAirohaFotaStatusClientAppListener mAppLayerFotaStatusListener;
    private int mFileSystemPartitionLength;
    private byte mAgentFotaStorageType = (byte) 0xFF; //0x00; // default as internal
    private boolean mQueryAddressIsUnreasonable = false;
    private ConcurrentHashMap<String, OnAirohaFotaStatusClientAppListener> mAppLayerListeners;
    // flow control
    private Timer mTimerForRetryTask;
    private Timer mTimerForRespTimeout;
    private Timer mTimerReconnect;
    private Timer mTimerActiveDisconnect;
    private int TIMEOUT_RACE_CMD_NOT_RESP = 1000;// 2018/9/28 [GVA-1995] 30 seconds
    private int TIMEOUT_ROLE_SWITCHED_RECONNECT = 3000; // 1 sec
    private int TIMEOUT_SOCKET_CONNECT = 10000;
    private boolean mIsLongPacketMode = false;
    private int mLongPacketCmdCount = 1;
    private int RECONNECT_RETRY_COUNTER = 10;
    private boolean mIsReconnected = false;
    private boolean mIsCancledDuringRoleSwitch = false;
    private boolean mIsDoingRoleSwitch = false;
    private boolean mIsDoingCommit = false;
    private String mAgentVersion;
    private String mClientVersion;
    private byte mAgentAudioChannel = (byte)0xFF;
    private byte mClientAudioChannel = (byte)0xFF;
    private Dst mAwsPeerdst;
    private ConcurrentHashMap<String, byte[]> mAgentReadNvkeyMap;
    private ConcurrentHashMap<String, byte[]> mPartnerReadNvkeyMap;

    private OnRacePacketListener mOnRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {

            if (mCurrentStage == null)
                return;

            mAirohaLink.logToFile(TAG, "received raceId: " + String.format("%04X", raceId)
                    + ", raceType: " + String.format("%02X", raceType));

            if (!mCurrentStage.isExpectedResp(raceType, raceId, packet)) {
                mAirohaLink.logToFile(TAG, "not the expected race ID or Type");
                return;
            }

            if (mTimerForRetryTask != null) {
                mTimerForRetryTask.cancel();
                mTimerForRetryTask = null;
                mAirohaLink.logToFile(TAG, "mTimerForRetryTask.cancel()");
            }

            if (mTimerForRespTimeout != null) {
                mTimerForRespTimeout.cancel();
                mTimerForRespTimeout = null;
                mAirohaLink.logToFile(TAG, "mTimerForRespTimeout.cancel()");
            }

            if (mCurrentStage.isStopped()) {
                notifyAppListenerError("Stopped unfinished FOTA stages");

                return;
            }


            mCurrentStage.handleResp(raceId, packet, raceType);

            if (!mCurrentStage.isRespStatusSuccess()) {
                notifyAppListenerError(mCurrentStage.getClass().getSimpleName() + " FAIL! Status: " + String.format("%02X", mCurrentStage.getStatus()));
                return;
            }

            if (mCurrentStage.isErrorOccurred()) {
                mCurrentStage.stop();

                // 2019.01.09 output all error to file
                mAirohaLink.logToFile(TAG, mCurrentStage.getErrorReason());

                // output to UI, UI callback should give highlight in different color
                notifyAppListenerError(mCurrentStage.getErrorReason());

                return;
            }

            int completedTaskCount = mCurrentStage.getCompletedTaskCount();
            int totalTaskCount = mCurrentStage.getTotalTaskCount();

            notifyAppListenerProgress(mCurrentStage.getClass().getSimpleName(), completedTaskCount, totalTaskCount);

            if (mCurrentStage.isCompleted()) {
                mAirohaLink.logToFile(TAG, "Completed: " + mCurrentStage.getClass().getSimpleName());
                mCompletedStageCount++;

                // check if address/length reasonable
                if (mQueryAddressIsUnreasonable) {
                    notifyAppListenerInterrupted("Partition length not matched");
                    return;
                }

                String lastStageName = mCurrentStage.getClass().getSimpleName();

                LinkedList<FotaStage> stagesForSkip = null;
                IAirohaFotaStage.SKIP_TYPE skipType = mCurrentStage.getSkipType();
                if (skipType != None) {
                    stagesForSkip = mCurrentStage.getStagesForSkip(mCurrentStage.getSkipType());
                    if (stagesForSkip != null) {
                        mCompletedStageCount = mCompletedStageCount + stagesForSkip.size();
                    }
                }

                switch (skipType) {
                    case All_stages:
                        if (stagesForSkip == null) {
                            notifyAppListenerInterrupted("Interrupted: all partitions are the same, skip the other stages.");
                        } else {
                            reGenStageQueue(skipType);
                        }
                        break;
                    case Compare_stages:
                    case Erase_stages:
                    case Program_stages:
                    case CompareErase_stages:
                    case Client_Erase_stages:
                        reGenStageQueue(skipType);
                        break;
                    case Sinlge_StateUpdate_stages:
                        if (stagesForSkip != null) {
                            reGenStageQueue(skipType);
                        }
                        break;
                }

                // other stages
                mCurrentStage = mStagesQueue.poll();

                if (mCurrentStage != null) {
                    notifyAppListnerStatus("Started: " + mCurrentStage.getClass().getSimpleName());

                    mCurrentStage.start();
                } else {
                    // complete
                    notifyAppListenerCompleted("Completed:" + lastStageName);
                    return;
                }
            } else {
                mAirohaLink.logToFile(TAG, mCurrentStage.getClass().getSimpleName() + ": send next cmd");
                actionAfterStageNotCompleted(raceType);
            }

        }
    };

    private OnAirohaConnStateListener mConnStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(String type) {
//            if (mIsCancledDuringRoleSwitch) {
//                startSendCancelCmd();
//
//                mIsCancledDuringRoleSwitch = false;
//                mIsDoingRoleSwitch = false;
//                mIsDoingCommit = false;
//            }
        }

        @Override
        public void OnConnecting() {

        }

        @Override
        public void OnDisConnecting() {
            cleanForDisconnection();

        }

        @Override
        public void OnDisconnected() {
            // 2018/11/09 Bug fix
            cleanForDisconnection();
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };


    /**
     * Need to have the connected AirohaLink
     *
     * @param airohaLink
     */
    public AirohaRaceOtaMgr(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;
        mAirohaLink.registerOnRacePacketListener(TAG, mOnRacePacketListener);
        mAirohaLink.registerOnConnStateListener(TAG, mConnStateListener);

        mAppLayerListeners = new ConcurrentHashMap<>();
    }

    private void actionAfterStageNotCompleted(int raceType) {
        if (mCurrentStage.isCmdQueueEmpty()) {
            mAirohaLink.logToFile(TAG, "mCurrentStage.isCmdQueueEmpty()");

            // start timer task
//            mAirohaLink.logToFile(TAG, "mTimerForRetryTask waiting for 3000ms");
            mTimerForRetryTask = new Timer();
            mTimerForRetryTask.schedule(new RetryTask(), TIMEOUT_RACE_CMD_NOT_RESP);

        } else {

            // 2016.06.13 Daniel, special case for 2811 debugging
//            if(mCurrentStage instanceof FotaStage_02_TwsFlashPartitionErase){
            if (raceType == mCurrentStage.getRespType()) {
                mCurrentStage.pollCmdQueue();
//                    return;
//                }else {
//                    return;
            }
        }

        // if not empty keep sending or got refilled
//            mCurrentStage.pollCmdQueue();
//        }
    }

    public void cancelDualFota() {
        // stop mCurrentStage

        if (mCurrentStage != null) {
            mCurrentStage.stop();
        }

        mStagesQueue.clear();

        byte[] recipients = new byte[]{Recipient.DontCare};

        mStagesQueue.offer(new FotaStage_06_Stop(this, recipients, StopReason.Cancel));
        mStagesQueue.offer(new FotaStage_06_StopRelay(this, recipients, StopReason.Cancel));

        mStagesQueue.offer(new FotaStage_ResumeDsp(this));
        mStagesQueue.offer(new FotaStage_ResumeDspRelay(this));

        startPollStagetQueue();
    }

    public void cancelSingleFota(byte role) {
        // stop mCurrentStage

        if (mCurrentStage != null) {
            mCurrentStage.stop();
        }

        mStagesQueue.clear();

        byte[] recipients = new byte[]{Recipient.DontCare};

        if(role == AgentClientEnum.AGENT) {
            mStagesQueue.offer(new FotaStage_06_Stop(this, recipients, StopReason.Cancel));
            mStagesQueue.offer(new FotaStage_ResumeDsp(this));
        }

        if(role == AgentClientEnum.CLIENT) {
            mStagesQueue.offer(new FotaStage_06_StopRelay(this, recipients, StopReason.Cancel));
            mStagesQueue.offer(new FotaStage_ResumeDspRelay(this));
        }

        startPollStagetQueue();
    }

    private void checkFileSystemBinLength(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("file path null");
        }
        mAirohaLink.logToFile(TAG, "file system bin: " + filePath);
        mFileSystemBinFile = new File(filePath);

        mFileSystemInputStreamSize = mFileSystemBinFile.length();

        if (mFileSystemBinFile.length() != mFileSystemPartitionLength) {
            notifyAppListenerError("File System Partition length not matched");
            throw new IllegalArgumentException("File System Partition length not matched");
        }

        try {
            mFileSystemInputStream = new FileInputStream(mFileSystemBinFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notifyAppListenerError(e.getMessage());
            return;
        }
    }

//    private void checkFotaRawStreamLength(byte[] raw) {
//        if (raw.length >= mFotaPartitionLength) {
//            notifyAppListenerError("FOTA Partition length not matched");
//            throw new IllegalArgumentException("FOTA Partition length not matched");
//        }
//
//        mFotaInputStreamSize = raw.length;
//
//        mFotaInputStream = new ByteArrayInputStream(raw);
//    }

    protected void cleanForDisconnection() {
        synchronized (mStagesQueue) {
            // 2018/11/09 Bug fix
            if (mTimerForRetryTask != null) {
                mTimerForRetryTask.cancel();
                mTimerForRetryTask = null;
            }

            if (mTimerForRespTimeout != null) {
                mTimerForRespTimeout.cancel();
                mTimerForRespTimeout = null;
            }

            if (mStagesQueue != null) {
                mStagesQueue.clear();
            }
        }
    }

    /**
     * Engineer mode
     *
     * @param enable
     */
    public void enableLongPacketMode(boolean enable) {
        mIsLongPacketMode = enable;
    }

    public AirohaLink getAirohaLink() {
        return mAirohaLink;
    }

    public Dst getAwsPeerDst() {
        return mAwsPeerdst;
    }

    public void setAwsPeerDst(Dst awsPeerDst) {
        mAwsPeerdst = awsPeerDst;
    }

    public File getAgentFotaBinFile() {
        return mFotaAgentBinFile;
    }

    public File getPartnerFotaBinFile() {
        return mFotaPartnerBinFile;
    }

    public File getFotaFileSystemBinFile() {
        return mFotaFileSystemBinFile;
    }

    public FotaDualSettings getFotaDualSettings() {
        return mFotaDualSettings;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @return
     */
    public InputStream getFotaAgentInputStream() {

        try {
            mFotaAgentInputStream = new FileInputStream(mFotaAgentBinFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notifyAppListenerError(e.getMessage());
            return null;
        }

        return mFotaAgentInputStream;
    }

    public InputStream getFotaPartnerInputStream() {

        try {
            mFotaPartnerInputStream = new FileInputStream(mFotaPartnerBinFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notifyAppListenerError(e.getMessage());
            return null;
        }

        return mFotaPartnerInputStream;
    }

    public InputStream getFotaFileSystemInputStream() {

        try {
            mFotaFileSystemInputStream = new FileInputStream(mFotaFileSystemBinFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notifyAppListenerError(e.getMessage());
            return null;
        }

        return mFotaFileSystemInputStream;
    }

    public int getFotaFileSystemInputStreamSize() {
        return (int) mFotaFileSystemInputStreamSize;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @return
     */
    public int getFotaPartitionStartAddress() {
        return mFotaPartitionStartAddress;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param address
     */
    public void setFotaPartitionStartAddress(int address) {
        mFotaPartitionStartAddress = address;

        if (address == ROM_START_ADDRESS) {
            mQueryAddressIsUnreasonable = true;
        } else {
            mQueryAddressIsUnreasonable = false;
        }
    }


    public int getFotaStagePrePollSize() {
        return FotaStage.getPrePollSize();
    }


    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @return
     */
    public byte getFotaStorageType() {
        return mAgentFotaStorageType;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param type
     */
    public void setFotaStorageType(byte type) {
        mAgentFotaStorageType = type;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @return
     */
    public int getLongPacketCmdCount() {
        return mLongPacketCmdCount;
    }

    protected void handleQueriedStates(int queryState) {

    }

    protected void handleTwsQueriedStates() {

    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @return
     */
    public boolean isLongPacketMode() {
        return mIsLongPacketMode;
    }

    private void notifyAppListenerCompleted(String msg) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyCompleted(msg);
            }
        }
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param errorMsg
     */
    public void notifyAppListenerError(String errorMsg) {
        // 2019.01.09 output to file
        mAirohaLink.logToFile(TAG, errorMsg);

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyError(errorMsg);
            }
        }
    }

    public void notifyAppListenerWarning(String warningMsg) {
        // 2019.01.09 output to file
        mAirohaLink.logToFile(TAG, warningMsg);

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyWarning(warningMsg);
            }
        }
    }

    public void clearAppListenerWarning() {
        String emptyString = "";

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyWarning(emptyString);
            }
        }
    }

    private void notifyAppListenerInterrupted(String msg) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyInterrupted(msg);
            }
        }
    }

    protected void notifyAppListenerProgress(String currentStage, int completedTaskCount, int totalTaskCount) {

        int progress = (int) (100 * (((float) completedTaskCount / totalTaskCount) + mCompletedStageCount) / (float) mTotalStageCount);


        Log.d(TAG, "over-all progress: " + progress);

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.onProgressUpdated(currentStage, mCompletedStageCount, mTotalStageCount, completedTaskCount, totalTaskCount);
            }
        }
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param status
     */
    public void notifyAppListnerStatus(String status) {
        mAirohaLink.logToFile(TAG, status);

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyStatus(status);
            }
        }
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     */
    public void notifyBatteryLevelLow() {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyBatterLevelLow();
            }
        }
    }

    private void notifyClientExistence(boolean isExisting) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {

            }
        }
    }

    protected void notifyDualAction(DualActionEnum actionEnum) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.onAvailableDualActionUpdated(actionEnum);
            }
        }
    }

    private void notifyDualFotaInfo() {
        mDualFotaInfo.agentFotaState = mStrAgentStateEnum;
        mDualFotaInfo.partnerFotaState = mStrPartnerStateEnum;
        mDualFotaInfo.agentVersion = mAgentVersion;
        mDualFotaInfo.partnerVersion = mClientVersion;
        mDualFotaInfo.agentAudioChannelSetting = mAgentAudioChannel;
        mDualFotaInfo.partnerAudioChannelSetting = mClientAudioChannel;

        mDualFotaInfo.agentCompanyName = mSingleFotaInfo.agentCompanyName;
        mDualFotaInfo.agentModelName = mSingleFotaInfo.agentModelName;
        mDualFotaInfo.agentReleaseDate = mSingleFotaInfo.agentReleaseDate;

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.onDualFotaInfoUpdated(mDualFotaInfo);
            }
        }
    }

    protected void notifySingleAction(SingleActionEnum actionEnum) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.onAvailableSingleActionUpdated(actionEnum);
            }
        }
    }

    private void notifySingleFotaInfo(String strFotaState, String strVersion) {
        mSingleFotaInfo.agentFotaState = strFotaState;
        mSingleFotaInfo.agentVersion = strVersion;

        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.onSingleFotaInfoUpdated(mSingleFotaInfo);
            }
        }
    }

    private void notifyStageEnum(String stateEnum) {
        for (OnAirohaFotaStatusClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyStateEnum(stateEnum);
            }
        }
    }

    void reGenStageQueue(IAirohaFotaStage.SKIP_TYPE type) {
        synchronized (mStagesQueue) {
//        Queue<IAirohaFotaStage> newStageQueue = new ConcurrentLinkedQueue<>();
            mStagesQueue.clear();
            LinkedList<FotaStage> stagesForSkip = mCurrentStage.getStagesForSkip(type);
            while (mStagesQueue.size() > 0) {
                IAirohaFotaStage tmp = mStagesQueue.poll();
                if (stagesForSkip.contains(tmp)) {
                    continue;
                } else {
                    mStagesQueue.add(tmp);
                }
            }
        }
    }

    // Listeners
    public void registerListener(String observer, OnAirohaFotaStatusClientAppListener appListener) {
        mAppLayerListeners.put(observer, appListener);
    }

    protected void renewStageQueue() {
        if (mStagesQueue != null) {
            mStagesQueue.clear();

            mCompletedStageCount = 0;
        }

        mStagesQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param queryState
     */
    public void setAgentFotaState(byte[] queryState) {
        mStrAgentStateEnum = Converter.byte2HerStrReverse(queryState);

        mAirohaLink.logToFile(TAG, "RACE_FOTA_QUERY_STATE resp state: " + mStrAgentStateEnum);

//        notifyStageEnum(mStrAgentStateEnum);

        mAgentFotaState = (queryState[0] & 0xFF) | ((queryState[1] & 0xFF) << 8);

        handleQueriedStates(mAgentFotaState);

        notifySingleFotaInfo(mStrAgentStateEnum, mAgentVersion);
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param version
     */
    public void setAgentVersion(byte[] version) {
        String hexStr = Converter.byte2HexStr(version).replace(" ", "");
        mAgentVersion = Converter.hexStrToAsciiStr(hexStr);
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param info
     */
    public void setAgentFwInfo(byte[] info) {
        if(info.length > 20) {
            // new AE format
            byte[] versionInfo = new byte[6];
            System.arraycopy(info, 0, versionInfo, 0, 6);
            byte[] dateInfo = new byte[3];
            System.arraycopy(info, 6, dateInfo, 0, 3);
            byte[] companyInfo = new byte[20];
            System.arraycopy(info, 9, companyInfo, 0, 20);
            byte[] modelInfo = new byte[20];
            System.arraycopy(info, 29, modelInfo, 0, 20);

            String hexStr = Converter.byte2HexStr(companyInfo).replace(" ", "");
            mSingleFotaInfo.agentCompanyName = Converter.hexStrToAsciiStr(hexStr);

            hexStr = Converter.byte2HexStr(modelInfo).replace(" ", "");
            mSingleFotaInfo.agentModelName = Converter.hexStrToAsciiStr(hexStr);

            mSingleFotaInfo.agentReleaseDate = (dateInfo[0]+2000) + "/" + dateInfo[1] + "/" + dateInfo[2];

            int buildNumber = Converter.BytesToInt(versionInfo[3], versionInfo[2]);
            int revisionNumber = Converter.BytesToInt(versionInfo[5], versionInfo[4]);
        }
        else {
            // only model name
            String hexStr = Converter.byte2HexStr(info).replace(" ", "");
            mSingleFotaInfo.agentModelName = Converter.hexStrToAsciiStr(hexStr);
        }
    }

    public void setAgentAudioChannel(byte channel) {
        mAgentAudioChannel = channel;
    }

    public void addReadNvKeyEvent(String keyId, byte[] nvValue, boolean is_agent){
        if(is_agent) {
            if (mAgentReadNvkeyMap == null) {
                mAgentReadNvkeyMap = new ConcurrentHashMap<>();
            }
            if (mAgentReadNvkeyMap.containsKey(keyId)) {
                mAgentReadNvkeyMap.remove(keyId);
            }
            mAgentReadNvkeyMap.put(keyId, nvValue);
        }
        else{
            if (mPartnerReadNvkeyMap == null) {
                mPartnerReadNvkeyMap = new ConcurrentHashMap<>();
            }
            if (mPartnerReadNvkeyMap.containsKey(keyId)) {
                mPartnerReadNvkeyMap.remove(keyId);
            }
            mPartnerReadNvkeyMap.put(keyId, nvValue);
        }
    }

    public void removeReadNvKeyEvent(String keyId, boolean is_agent){
        if(is_agent) {
            if (mAgentReadNvkeyMap == null) {
                mAgentReadNvkeyMap = new ConcurrentHashMap<>();
            }
            if (mAgentReadNvkeyMap.containsKey(keyId)) {
                mAgentReadNvkeyMap.remove(keyId);
            }
        }
        else{
            if (mPartnerReadNvkeyMap == null) {
                mPartnerReadNvkeyMap = new ConcurrentHashMap<>();
            }
            if (mPartnerReadNvkeyMap.containsKey(keyId)) {
                mPartnerReadNvkeyMap.remove(keyId);
            }
        }
    }

    public byte[] getReadNvKeyEvent(String keyId, boolean is_agent){
        byte[] rtn = null;
        if(is_agent) {
            if (mAgentReadNvkeyMap != null && mAgentReadNvkeyMap.containsKey(keyId)) {
                rtn = mAgentReadNvkeyMap.get(keyId);
            }
        }
        else{
            if (mPartnerReadNvkeyMap != null && mPartnerReadNvkeyMap.containsKey(keyId)) {
                rtn = mPartnerReadNvkeyMap.get(keyId);
            }
        }
        return rtn;
    }

    public void setClientFotaState(byte[] queryState) {
        mPartnerFotaState = Converter.BytesToU16(queryState[1], queryState[0]);

        mStrPartnerStateEnum = Converter.byte2HerStrReverse(queryState);

        handleQueriedStates(mPartnerFotaState);

        notifySingleFotaInfo(mStrPartnerStateEnum, mClientVersion);

        handleTwsQueriedStates();

        notifyDualFotaInfo();
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param version
     */
    public void setPartnerVersion(byte[] version) {
        String hexStr = Converter.byte2HexStr(version).replace(" ", "");
        mClientVersion = Converter.hexStrToAsciiStr(hexStr);
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     *
     * @param info
     */
    public void setPartnerFwInfo(byte[] info) {
        if(info.length > 20) {
            // new AE format
            byte[] versionInfo = new byte[6];
            System.arraycopy(info, 0, versionInfo, 0, 6);
            byte[] dateInfo = new byte[3];
            System.arraycopy(info, 6, dateInfo, 0, 3);
            byte[] companyInfo = new byte[20];
            System.arraycopy(info, 9, companyInfo, 0, 20);
            byte[] modelInfo = new byte[20];
            System.arraycopy(info, 29, modelInfo, 0, 20);

            String hexStr = Converter.byte2HexStr(companyInfo).replace(" ", "");
            mDualFotaInfo.partnerCompanyName = Converter.hexStrToAsciiStr(hexStr);

            hexStr = Converter.byte2HexStr(modelInfo).replace(" ", "");
            mDualFotaInfo.partnerModelName = Converter.hexStrToAsciiStr(hexStr);

            mDualFotaInfo.partnerReleaseDate = (dateInfo[0]+2000) + "/" + dateInfo[1] + "/" + dateInfo[2];

            int buildNumber = Converter.BytesToInt(versionInfo[3], versionInfo[2]);
            int revisionNumber = Converter.BytesToInt(versionInfo[5], versionInfo[4]);
        }
        else {
            // only model name
            String hexStr = Converter.byte2HexStr(info).replace(" ", "");
            mDualFotaInfo.partnerModelName = Converter.hexStrToAsciiStr(hexStr);
        }
    }

    public void setPartnerAudioChannel(byte channel) {
        mClientAudioChannel = channel;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     */
    public void setFlashOperationAllowed(boolean allowed) {
        mIsFlashOperationAllowed = allowed;
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     */
    public void setFotaPartitionLength(int partitionLength) {
        mFotaPartitionLength = partitionLength;
    }

    protected void setInputFile(String filePath, int type) {
        //0: Agent, 1: Partner, 2:FileSystem
        if (filePath == null) {
            throw new IllegalArgumentException("file path null");
        }
        if (type < 0 || type > 2) {
            throw new IllegalArgumentException("file type error");
        }
        switch(type)
        {
            case 0:
                mAirohaLink.logToFile(TAG, "fota Agent bin: " + filePath);
                mFotaAgentBinFile = new File(filePath);
                mFotaAgentInputStreamSize = mFotaAgentBinFile.length();
                mAirohaLink.logToFile(TAG, "fota Agent bin size: " + mFotaAgentInputStreamSize);
                break;
            case 1:
                mAirohaLink.logToFile(TAG, "fota Partner bin: " + filePath);
                mFotaPartnerBinFile = new File(filePath);
                mFotaPartnerInputStreamSize = mFotaPartnerBinFile.length();
                mAirohaLink.logToFile(TAG, "fota Partner bin size: " + mFotaPartnerInputStreamSize);
                break;
            case 2:
                mAirohaLink.logToFile(TAG, "fota file system bin: " + filePath);
                mFotaFileSystemBinFile = new File(filePath);
                mFotaFileSystemInputStreamSize = mFotaFileSystemBinFile.length();
                mAirohaLink.logToFile(TAG, "fota file system bin size: " + mFotaFileSystemInputStreamSize);
                break;
        }
    }

    public void setNeedToUpdateFileSystem(boolean isNeedToUpdateFilesystem) {
        mIsNeedToUpdateFileSystem = isNeedToUpdateFilesystem;
    }

    public void startDualFota(String agentFilePath, String partnerFilePath, FotaDualSettings settings) throws IllegalArgumentException {

    }


    protected void startPollStagetQueue() {
        mTotalStageCount = mStagesQueue.size();
        mCompletedStageCount = 0;

        mCurrentStage = mStagesQueue.poll();
        mCurrentStage.start();
    }

    /**
     * used by Airoha internal classed. Ignore for client App development
     */
    public void startRespTimer() {
        synchronized (mStagesQueue) {
            if (mTimerForRespTimeout != null) {
                mTimerForRespTimeout.cancel();
            }

//        mAirohaLink.logToFile(TAG, "mTimerForRespTimeout started");

            mTimerForRespTimeout = new Timer();
            mTimerForRespTimeout.schedule(new RetryTask(), TIMEOUT_RACE_CMD_NOT_RESP);
        }
    }

    public void unregisterListener(OnAirohaFotaStatusClientAppListener appListener) {
        mAppLayerListeners.remove(appListener);
    }

    class ReconnectTask extends TimerTask {

        @Override
        public void run() {
            mAirohaLink.logToFile(TAG, "ReconnectTask start");

//            boolean result = false;

            // 2018.08.21 reset
            mIsReconnected = false;

            for (int i = 0; i < RECONNECT_RETRY_COUNTER; i++) {
                try {
                    if (mTimerActiveDisconnect != null) {
                        mTimerActiveDisconnect.cancel();
                        mTimerActiveDisconnect = null;
                    }

                    mTimerActiveDisconnect = new Timer();
                    // the DisconnectTask will force mAriohaLink.reConnect to
                    mTimerActiveDisconnect.schedule(new DisconnectTask(), TIMEOUT_SOCKET_CONNECT);

                    mAirohaLink.logToFile(TAG, "trying to reconnect");
                    mIsReconnected = mAirohaLink.reConnect();

                    // OK, if the task is run or not
                    mTimerActiveDisconnect.cancel();

                    mAirohaLink.logToFile(TAG, "reconnect result: " + mIsReconnected);

                    if (mIsReconnected == true) {
                        break;
                    } else {
                        SystemClock.sleep(TIMEOUT_ROLE_SWITCHED_RECONNECT);
                    }
                } catch (IllegalArgumentException e) {
                    mAirohaLink.logToFile(TAG, e.getMessage());

                }
            }
        }
    }

    class DisconnectTask extends TimerTask {
        @Override
        public void run() {
            mAirohaLink.logToFile(TAG, "timeout, mIsReconnected = " + mIsReconnected);

            if (!mIsReconnected) {
                mAirohaLink.logToFile(TAG, "reconnect timeout, active disconnect");
                mAirohaLink.disconnect();
//                this.cancel();
                mAirohaLink.logToFile(TAG, "exit DisconnectTask");
            }

        }
    }

    class RetryTask extends TimerTask {

        @Override
        public void run() {
            if (!mAirohaLink.isConnected())
                return;

            mAirohaLink.logToFile(TAG, "start to check cmds need retry");
            if(mCurrentStage == null) {
                return;
            }
            if (mCurrentStage.isRetryUpToLimit()) {
                notifyAppListenerError(mCurrentStage.getClass().getSimpleName() + " retry failed");
                return;
            } else {
                mCurrentStage.prePoolCmdQueue();
            }
        }
    }
}
