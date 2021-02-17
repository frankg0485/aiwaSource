package com.airoha.android.lib.mmi;

import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.mmi.stage.IAirohaMmiStage;
import com.airoha.android.lib.mmi.stage.MmiStageGetAvaDst;
import com.airoha.android.lib.mmi.stage.MmiStageGetGameMode;
import com.airoha.android.lib.mmi.stage.MmiStageReset;
import com.airoha.android.lib.mmi.stage.MmiStageResetRelay;
import com.airoha.android.lib.mmi.stage.MmiStageSetGameMode;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A class to control Airoha 153x device.
 * Don't forget to register the callback {@link OnAirohaMmiClientAppListener}.
 * <p>
 * The step of AB153x MMI control related operation roughly like below
 * <p>
 * 1. Create a {@link AirohaLink}
 * <p>
 * 2. implement its callback interface OnAirohaConnStateListener and register it in AirohaLink
 * <p>
 * 3. Create a AirohaMmiMgr with the AirohaLink instance as parameter
 * <p>
 * 4. Implement the interface OnAirohaMmiClientAppListener and register it in AirohaMmiMgr by registerMmiClientAppListener().
 * <p>
 * 5. Use AirohaLink to connect the target device
 * <p>
 * 6. When SPP is connected, use getGameModeState() to query device information
 * <p>
 * 7. The result will callback to OnGameModeStateChanged
 * */
public class AirohaMmiMgr {

    private static final String TAG = "AirohaMmiMgr";

    protected AirohaLink mAirohaLink;

    protected int mTotalStageCount;
    protected int mCompletedStageCount;
	
    private Dst mAwsPeerDst;
    private IAirohaMmiStage mCurrentStage;
    private ConcurrentHashMap<String, OnAirohaMmiClientAppListener> mAppLayerListeners;
	
    public Queue<IAirohaMmiStage> mStagesQueue;
	
    private OnRacePacketListener mOnRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {

            if(mCurrentStage == null){ // not active querying
                checkTwsLinkStatusChangeReport(raceId, packet, raceType);

                return;
            }

            if (mCurrentStage != null) {
                if (!mCurrentStage.isExpectedResp(raceId, raceType, packet)) {
                    mAirohaLink.logToFile(TAG, "not the expected race ID or Type");
                    return;
                }

                mCurrentStage.handleResp(raceId, packet, raceType);

                if (mCurrentStage.isRespStatusSuccess()) {
                    AirohaMmiMgr.this.notifyAppListenersSuccess(mCurrentStage.getSimpleName());
                }

                mCurrentStage = mStagesQueue.poll();

                // next one
                if(mCurrentStage != null) {
                    mCurrentStage.start();
                }
            }
        }
    };

    /**
     * MMI manager constructor, need to have a connected AirohaLink
     */
    public AirohaMmiMgr(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;
        mAirohaLink.registerOnRacePacketListener(TAG, mOnRacePacketListener);
//        mAirohaLink.registerOnRaceMmiRoleSwitchIndListener(TAG, mOnRaceMmiRoleSwitchIndListener);

        mAppLayerListeners = new ConcurrentHashMap<>();
    }
	
	/**
     * Need to set a listener implementing  {@link OnAirohaMmiClientAppListener} interface for handling MMI response
     *
     * @param subcriberName
     * @param listener
     */
    public void registerMmiClientAppListener(String subcriberName, OnAirohaMmiClientAppListener listener) {
        mAppLayerListeners.put(subcriberName, listener);
    }
	
	/**
     * Get the Game mode state.
     * {@link OnAirohaMmiClientAppListener#notifyGameModeState(byte)} will notify the game mode state change
     */
    public void getGameModeState() {
        renewStageQueue();

        mStagesQueue.offer(new MmiStageGetGameMode(this));

        startPollStagetQueue();
    }

    /**
     * Set the Game mode state.
     * {@link OnAirohaMmiClientAppListener#notifyGameModeState(byte)} will notify the game mode state change
     *
     * @param isEnabled true is to enable game mode, otherwise is to disable game mode.
     */
    public void setGameModeState(boolean isEnabled) {
        renewStageQueue();

        mStagesQueue.offer(new MmiStageSetGameMode(this, isEnabled));

        startPollStagetQueue();
    }

    /**
     * Set the Game mode state.
     * {@link OnAirohaMmiClientAppListener#notifyResetState(byte, byte)} will notify the reset state change
     *
     */
    public void reset(boolean to_agnent, boolean to_Partner) {
        renewStageQueue();
        if(to_Partner)
            mStagesQueue.offer(new MmiStageResetRelay(this));
        if(to_agnent)
            mStagesQueue.offer(new MmiStageReset(this));

        startPollStagetQueue();
    }

    /**
     * for internal use.
     */
    public void notifyGameModeState(byte state){
        for(OnAirohaMmiClientAppListener listener : mAppLayerListeners.values()) {
            if(listener!=null){
                listener.notifyGameModeState(state);
            }
        }
    }

    /**
     * for internal use.
     */
    public void notifyResetState(byte role, byte state){
        for(OnAirohaMmiClientAppListener listener : mAppLayerListeners.values()) {
            if(listener!=null){
                listener.notifyResetState(role, state);
            }
        }
    }

    /**
     * Check if the partner can be found
     *
     * {@link OnAirohaMmiClientAppListener#notifyPartnerIsExisting(boolean)} will notify the result
     */
    public void checkPartnerExistence(){
        renewStageQueue();

        mStagesQueue.offer(new MmiStageGetAvaDst(this));
//        mStagesQueue.offer(new MmiStageCheckAgentChannel(this));

        startPollStagetQueue();
    }

    /**
     * for internal use.
     */
    public AirohaLink getAirohaLink() {
        return mAirohaLink;
    }
	
    /**
     * for internal use.
     */
    synchronized public void renewStageQueue() {
        if (mStagesQueue != null) {
            mStagesQueue.clear();

            mCompletedStageCount = 0;
        }

        mStagesQueue = new ConcurrentLinkedQueue<>();
    }
	
    /**
     * for internal use.
     */
    synchronized public void startPollStagetQueue() {
        mTotalStageCount = mStagesQueue.size();
        mCompletedStageCount = 0;

        mCurrentStage = mStagesQueue.poll();
        mCurrentStage.start();
    }
	
    /**
     * for internal use.
     */
    public void notifyGameModeStatueChanged(boolean isEnabled) {
        for (OnAirohaMmiClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.OnGameModeStateChanged(isEnabled);
            }
        }
    }

    /**
     * for internal use.
     */
    public Dst getAwsPeerDst() {
        return mAwsPeerDst;
    }

    /**
     * for internal use.
     */
    public void setAwsPeerDst(Dst awsPeerDst) {

        mAwsPeerDst = awsPeerDst;

        if(mAwsPeerDst != null){
            notifyPartnerExisting(true);

//            checkAgentChannel();
        }else {
            notifyPartnerExisting(false);

            // maybe clear the queue for stopping relay cmds
//            if(mStagesQueue != null)
//                mStagesQueue.clear();
        }
    }
	

    ConcurrentHashMap<String, OnAirohaMmiClientAppListener> getAppLayerListeners() {
        return mAppLayerListeners;
    }

    private void notifyPartnerExisting(boolean isExisting) {
        for (OnAirohaMmiClientAppListener listener : mAppLayerListeners.values()) {
            if (listener != null) {
                listener.notifyPartnerIsExisting(isExisting);
            }
        }
    }
	
    private void notifyAppListenersSuccess(String stageName) {
        for(OnAirohaMmiClientAppListener listener : mAppLayerListeners.values()){
            if(listener!=null){
                listener.OnRespSuccess(stageName);
            }
        }
    }

    private void checkTwsLinkStatusChangeReport(int raceId, byte[] packet, int raceType){
        // logic start
        // Rx packet :  05 5D 08 00 00 0D [00 00 05 04 03 03]

        if(raceId!= RaceId.RACE_RELAY_GET_AVA_DST)
            return;

        List<Dst> dstList = new ArrayList<>();
        for (int i = RacePacket.IDX_PAYLOAD_START; i < packet.length - 1; i = i + 2) {
            Dst dst = new Dst();
            dst.Type = packet[i];
            dst.Id = packet[i + 1];
            dstList.add(dst);
        }


        Dst awsPeerDst = null;
        for (Dst dst : dstList) {
            if (dst.Type == AvailabeDst.RACE_CHANNEL_TYPE_AWSPEER) {
                awsPeerDst = dst;
                break;
            }
        }

        if (awsPeerDst == null) {
            // do error handling
//            mIsErrorOccurred = true;
//            mStrErrorReason = FotaErrorMsg.PartnerNotExisting;
            mAirohaLink.logToFile(TAG, "partner not existing");
        }

        // pass to manager, null is OK
        setAwsPeerDst(awsPeerDst);
    }
}
