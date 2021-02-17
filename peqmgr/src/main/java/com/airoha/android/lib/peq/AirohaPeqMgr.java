package com.airoha.android.lib.peq;

//import android.support.annotation.NonNull;
import android.util.Log;

import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.lib153xPeq.Ab153xPeq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.airoha.android.lib.util.Converter.shortArrToBytes;

/**
 * A class to control device PEQ.
 * Don't forget to register the callback {@link OnPeqStatusUiListener}.
 * <p>
 * 1. Create a {@link AirohaLink}
 * <p>
 * 2. implement its callback interface OnAirohaConnStateListener and register it in AirohaLink
 * <p>
 * 3. Implement the interface OnPeqStatusUiListener
 * <p>
 * 4. Create a AirohaPeqMgr with the AirohaLink instance and the OnPeqStatusUiListener instance as parameter
 * <p>
 * 5. Use AirohaLink to connect the target device
 * <p>
 * 6. When SPP is connected, use loadPeqUiData to query device PEQ
 * <p>
 * 7. The result will callback to OnLoadPeqUiData
 * */
public class AirohaPeqMgr {
    private static final String TAG = "AirohaPeqMgr";
    // controlled by Stage
    private byte[] mAudioPathTargetNvKey;
    // controlled by API
    private final byte[] mPeqCoefTargetNvKey = new byte[2];
    private final byte[] mPeqUiDataTargetNvKey = new byte[2];
    private AirohaLink mAirohaLink;
    private Map<Rate, Double> mRateValueMap;
    private Map<Rate, CoefParamStruct> mRateCoefParamStructMap;
    private OnPeqStatusUiListener mOnPeqStatusUiListener;

//    private PeqUiDataStru mPeqUiDataStru;

    private byte[] mSaveCoefPaload;
    private byte[] mSavePeqUiDataPayload;

    private Action mAction;

    private Queue<IPeqStage> mPeqStageQueue;

    private IPeqStage mCurrentStage;

    private byte[] mWriteBackPeqSubsetContent;

    private byte[] mWriteBackAudioPathContent;

    private Dst mAwsPeerDst;

    /**
     * Actions will be indicated in {@link OnPeqStatusUiListener}
     */
    public enum Action{
        RealTimeUpdate,
        SaveCoef,
        SaveUiData,
        LoadUiData
    }

    public enum TargetDeviceEnum {
        AGENT,
        CLIENT,
        DUAL
    }

    /**
     * PEQ manager constructor, need a AirohaLink instance and a OnPeqStatusUiListener instance.
     *
     * @param airohaLink
     * @param listener
     */
    public AirohaPeqMgr(AirohaLink airohaLink, OnPeqStatusUiListener listener) {
        initRateValueMap();

        mOnPeqStatusUiListener = listener;

        mAirohaLink = airohaLink;
        mAirohaLink.registerOnRacePacketListener(TAG, mOnRacePacketListener);

        /**
             *  Call this once to reduce 50ms latency in further operations , need to put this in a worker thread
             */
        for(double fs : mRateValueMap.values()){
            Ab153xPeq.calcZ(fs);
        }
    }

//    public void preInitToSpeedUp(){
//        for(double fs : mRateValueMap.values()){
//            Ab153xPeq.calcZ(fs);
//        }
//    }

    /**
     * To real-time update PEQ.
     * Agent and Partner will sync automatically, so we don't need to take care the target device role.
     * @param peqUiDataStru
     * @see PeqUiDataStru
     * {@link OnPeqStatusUiListener#OnActionCompleted(Action)} will indicate action completed
     */
    public void startRealtimeUpdate(PeqUiDataStru peqUiDataStru) {
        synchronized (this) {
            mAction = Action.RealTimeUpdate;

            try{
                boolean isParamGened = execute(peqUiDataStru);

                if(!isParamGened){
                    mOnPeqStatusUiListener.OnActionError(mAction);

                    return;
                }
            }catch (Exception e) {
                mAirohaLink.logToFile(TAG, e.getMessage());
                mOnPeqStatusUiListener.OnActionError(mAction);

                return;
            }

            if (mPeqStageQueue != null) {
                mPeqStageQueue.clear();
            }


            mPeqStageQueue = new LinkedList<>();

            mPeqStageQueue.offer(new PeqStageRealTimeUpdate(this, mRateCoefParamStructMap));


            mCurrentStage = mPeqStageQueue.poll();
            mCurrentStage.sendCmd();
        }
    }

    /**
     * Client App calll this to store the coef. generated from the algo. to the device
     * @param peqIndex 1~4
     * @param targetDevice AGENT: save to Agent, Client: save to Partner, Dual: save to Agent and Partner
     * @throws IllegalArgumentException
     * {@link OnPeqStatusUiListener#OnActionCompleted(Action)} will indicate action completed
     */
    public void savePeqCoef(int peqIndex, TargetDeviceEnum targetDevice) throws IllegalArgumentException { // 1~4
        if (peqIndex < 0 || peqIndex > 4) {
            throw new IllegalArgumentException("input 1~4");
        }

        mAction = Action.SaveCoef;

        int peqCoefNvKey = 0xF27C + peqIndex - 1;

        mPeqCoefTargetNvKey[0] = (byte) (peqCoefNvKey & 0xFF);
        mPeqCoefTargetNvKey[1] = (byte) ((peqCoefNvKey >> 8) & 0xFF);

        try{
            mSaveCoefPaload = genSaveCoefPayload();
        }catch (Exception e){
            mAirohaLink.logToFile(TAG, e.getMessage());
            mOnPeqStatusUiListener.OnActionError(mAction);

            return;
        }

        if (mPeqStageQueue != null) {
            mPeqStageQueue.clear();
        }

        mPeqStageQueue = new LinkedList<>();

        mPeqStageQueue.offer(new PeqStageReadAudiPath(this));
        mPeqStageQueue.offer(new PeqStageReadPeqSubset(this));

        if (targetDevice != TargetDeviceEnum.CLIENT) {
            mPeqStageQueue.offer(new PeqStageReclaimNvkey(this, PeqStageReclaimNvkey.Option.SaveCoef));
            mPeqStageQueue.offer(new PeqStageSaveCoef(this));
            mPeqStageQueue.offer(new PeqStageReclaimNvkey(this, PeqStageReclaimNvkey.Option.SavePeqPath));
            mPeqStageQueue.offer(new PeqStageUpdatePeqSubset(this));
            mPeqStageQueue.offer(new PeqStageReclaimNvkey(this, PeqStageReclaimNvkey.Option.SaveAudioPath));
            mPeqStageQueue.offer(new PeqStageUpdateAudioPath(this));
            mPeqStageQueue.offer(new PeqStageHostAudioSaveStatus(this));
        }

        if (targetDevice != TargetDeviceEnum.AGENT) {
            // check if partner existing
            mPeqStageQueue.offer(new PeqStageGetAvaDst(this));
            // can be skipped
            mPeqStageQueue.offer(new PeqStageReclaimNvkeyRelay(this, PeqStageReclaimNvkey.Option.SaveCoef));
            mPeqStageQueue.offer(new PeqStageSaveCoefRelay(this));
            mPeqStageQueue.offer(new PeqStageReclaimNvkeyRelay(this, PeqStageReclaimNvkey.Option.SavePeqPath));
            mPeqStageQueue.offer(new PeqStageUpdatePeqSubsetRelay(this));
            mPeqStageQueue.offer(new PeqStageReclaimNvkeyRelay(this, PeqStageReclaimNvkey.Option.SaveAudioPath));
            mPeqStageQueue.offer(new PeqStageUpdateAudioPathRelay(this));
            mPeqStageQueue.offer(new PeqStageHostAudioSaveStatusRelay(this));
        }

        mCurrentStage = mPeqStageQueue.poll();
        mCurrentStage.sendCmd();
    }

    /**
     * To get the stored User Input to display on UI
     * @param peqIndex 1~4
     * @param targetDevice AGENT and DUAL: get UI Data from Agent, Client: get UI Data from Partner
     * @throws IllegalArgumentException
     * {@link OnPeqStatusUiListener#OnActionCompleted(Action)} will indicate action completed
     */
    public void loadPeqUiData(int peqIndex, TargetDeviceEnum targetDevice) throws IllegalArgumentException { // 1~4
        if (peqIndex < 0 || peqIndex > 4) {
            throw new IllegalArgumentException("input 1~4");
        }

        mAction = Action.LoadUiData;

        int peqUiDataNvKey = 0xEF00 + peqIndex - 1;

        mPeqUiDataTargetNvKey[0] = (byte) (peqUiDataNvKey & 0xFF);
        mPeqUiDataTargetNvKey[1] = (byte) ((peqUiDataNvKey >> 8) & 0xFF);

        if (mPeqStageQueue != null) {
            mPeqStageQueue.clear();
        }

        mPeqStageQueue = new LinkedList<>();

        if (targetDevice != TargetDeviceEnum.CLIENT) {
            mPeqStageQueue.offer(new PeqStageLoadUiData(this, mPeqUiDataTargetNvKey));
        } else {
            mPeqStageQueue.offer(new PeqStageLoadUiDataRelay(this, mPeqUiDataTargetNvKey));
        }

        mCurrentStage = mPeqStageQueue.poll();
        mCurrentStage.sendCmd();
    }

    /**
     * To store the User Input to device
     * @param peqIndex 1~4
     * @param peqUiDataStru
     * @param targetDevice AGENT: save to Agent, Client: save to Partner, Dual: save to Agent and Partner
     * @throws IllegalArgumentException
     * {@link OnPeqStatusUiListener#OnActionCompleted(Action)} will indicate action completed
     */
    public void savePeqUiData(int peqIndex, PeqUiDataStru peqUiDataStru, TargetDeviceEnum targetDevice) throws IllegalArgumentException { // 1~4
        if (peqIndex < 0 || peqIndex > 4) {
            throw new IllegalArgumentException("input 1~4");
        }

        mAction = Action.SaveUiData;

        int peqUiDataNvKey = 0xEF00 + peqIndex - 1;

        mPeqUiDataTargetNvKey[0] = (byte) (peqUiDataNvKey & 0xFF);
        mPeqUiDataTargetNvKey[1] = (byte) ((peqUiDataNvKey >> 8) & 0xFF);

        mSavePeqUiDataPayload = genSaveUiDataPayload(peqUiDataStru);

        if (mPeqStageQueue != null) {
            mPeqStageQueue.clear();
        }

        mPeqStageQueue = new LinkedList<>();

        if (targetDevice != TargetDeviceEnum.CLIENT) {
            mPeqStageQueue.offer(new PeqStageReclaimNvkey(this, PeqStageReclaimNvkey.Option.SaveUiData));
            mPeqStageQueue.offer(new PeqStageSaveUiData(this));
        }

        if (targetDevice != TargetDeviceEnum.AGENT) {
            // check if partner existing
            mPeqStageQueue.offer(new PeqStageGetAvaDst(this));
            // can be skipped
            mPeqStageQueue.offer(new PeqStageReclaimNvkeyRelay(this, PeqStageReclaimNvkey.Option.SaveUiData));
            mPeqStageQueue.offer(new PeqStageSaveUiDataRelay(this));
        }

        mCurrentStage = mPeqStageQueue.poll();
        mCurrentStage.sendCmd();
    }

    /**
     * for internal use.
     */
    public void setAwsPeerDst(Dst awsPeerDst) {
        if(awsPeerDst == null){
            // maybe clear the queue for stopping relay cmds

            mPeqStageQueue.clear();

            Log.d(TAG, "peer not existing, following task removed");
            Log.d(TAG, "stage queue size: " + mPeqStageQueue.size());
        }

        mAwsPeerDst = awsPeerDst;
    }

    /**
     * for internal use.
     */
    public Dst getAwsPeerDst() {
        return mAwsPeerDst;
    }

    byte[] getPeqUiDataNvKey() {
        return mPeqUiDataTargetNvKey;
    }

    byte[] getSavePeqUiDataPayload() {
        return mSavePeqUiDataPayload;
    }

    byte[] getAudioPathWriteBackContent() {
        return mWriteBackAudioPathContent;
    }

    void setAudioPathWriteBackContent(byte[] content) {
        mWriteBackAudioPathContent = content;
    }

    void setAudioPathTargetNvKey(byte[] nvKey){
        assert nvKey.length ==2;

        mAudioPathTargetNvKey = nvKey;
    }

    byte[] getAudioPathTargetNvKey() {
        return mAudioPathTargetNvKey;
    }

    byte[] getPeqCoefTargetNvKey() {
        return mPeqCoefTargetNvKey;
    }

    byte[] getWriteBackPeqSubsetContent() {
        return mWriteBackPeqSubsetContent;
    }

    void setWriteBackPeqSubsetContent(byte[] writeBackPeqSubsetContent) {
        this.mWriteBackPeqSubsetContent = writeBackPeqSubsetContent;
    }

    private OnRacePacketListener mOnRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            // 2019.05.07 [BTA-4110]
            if(mCurrentStage == null)
                return;

            mCurrentStage.handleRespOrInd(raceId, packet, raceType);

            if(mCurrentStage.isError()) {
                mOnPeqStatusUiListener.OnActionError(mAction);
            }

            if (mCurrentStage.isCompleted()) {
                mCurrentStage = mPeqStageQueue.poll();

                if (mCurrentStage != null) {
                    mCurrentStage.sendCmd();
                }else {
                    mOnPeqStatusUiListener.OnActionCompleted(mAction);
                }
            }
        }
    };

    void notifyOnLoadPeqUiData(final PeqUiDataStru peqUiDataStru) {
        mOnPeqStatusUiListener.OnLoadPeqUiData(peqUiDataStru);
    }

    private void initRateValueMap() {
        mRateValueMap = new Hashtable<>();

        mRateValueMap.put(Rate.R441, 44100.0);
        mRateValueMap.put(Rate.R48, 48000.0);
    }

    private boolean execute(PeqUiDataStru peqUiDataStru) {
        int thread_id = 0;

        mRateCoefParamStructMap = new HashMap<>();

        for (Rate rate : mRateValueMap.keySet()) {
            List<PeqBandInfo> peqBandInfos = peqUiDataStru.getPeqBandInfoList();

            Ab153xPeq.setParam(thread_id, mRateValueMap.get(rate), peqBandInfos.size(), 1, 0, 0);

            int set = 0;
            for (PeqBandInfo pbi : peqBandInfos) {
                if (pbi.isEnable()) {
                    double freq = pbi.getFreq();
                    double gain = pbi.getGain();
                    double bw = pbi.getBw();

                    Ab153xPeq.setPeqPoint(thread_id, set, freq, gain, bw);

                    set = set + 1;
                }
            }

            if (set > 0) {
                mAirohaLink.logToFile(TAG, "sampling rate: " + rate.toString());

                // Step 2: COFE Generate
                int returnCode = Ab153xPeq.generateCofe(thread_id);
//                Log.d(TAG, "generateCofe return: " + returnCode);
                mAirohaLink.logToFile(TAG, "generateCofe returnCode: " + returnCode);

                if(returnCode != 0)
                    return false;

                // Step 3: freq resp related , get rescale value
                // for APP, skip Step 3
                // Step 4: Apply rescale value
                // If not to do rescaling
                // then the function: change_rescale_cofe(int thread_id , double gscal_value), gscal_value = 1.0
                // 2018.11.15 https://eservice.airoha.com.tw/browse/BTA-2398
                // 2018.11.29 https://eservice.airoha.com.tw/browse/BTA-2652
                returnCode = Ab153xPeq.changeRescaleCofe(thread_id, -12); // change to 4.0 -> change to 12.0
//                Log.d(TAG, "generateCofe return: " + returnCode);
                mAirohaLink.logToFile(TAG, "changeRescaleCofe returnCode: " + returnCode);

                // Step5 : get final COFE

                int numOfCoef = (short) Ab153xPeq.getCofeCount(thread_id);

//                Log.d(TAG, "getCofeCount return: " + numOfCoef);
                mAirohaLink.logToFile(TAG, "getCofeCount: " + numOfCoef);

                short[] fwCoef = Ab153xPeq.getCofeParam(thread_id);
                mAirohaLink.logToFile(TAG, "getCofeParam(shorts): " + Converter.shortArrToString(fwCoef));

                byte[] bytesFwCoef = shortArrToBytes(fwCoef);

                // TODO to be removed
//                Log.d(TAG, "coef:" + Converter.byte2HexStr(bytesFwCoef));
                mAirohaLink.logToFile(TAG, "getCofeParam(bytes): " + Converter.byte2HexStr(bytesFwCoef));

                CoefParamStruct coefParamStruct = new CoefParamStruct(rate.getValue(), (short) numOfCoef, bytesFwCoef);

                mRateCoefParamStructMap.put(rate, coefParamStruct);
            }
        }

        return true;
    }

    byte[] getSaveCoefPaload() {
        return mSaveCoefPaload;
    }

    private byte[] genSaveUiDataPayload(PeqUiDataStru peqUiDataStru) {
        List<Byte> byteList = new ArrayList<>();

        byte[] raw = peqUiDataStru.getRaw();

        for (byte b : raw) {
            byteList.add(b);
        }

        byte[] payload = new byte[byteList.size()];

        for (int i = 0; i < byteList.size(); i++) {
            payload[i] = byteList.get(i);
        }

        return payload;
    }

    private byte[] genSaveCoefPayload() {
        List<Byte> byteList = new ArrayList<>();

//        for(byte b: mPeqCoefTargetNvKey) {
//            byteList.add(b);
//        }

        byte[] numberOfSampleRate = Converter.shortToBytes((short) mRateCoefParamStructMap.size());
        byte[] peqAlgoVersion = new byte[]{0x00, 0x00};


        for (byte b : numberOfSampleRate) {
            byteList.add(b);
        }

        for (byte b : peqAlgoVersion) {
            byteList.add(b);
        }

        for (CoefParamStruct coefParamStruct : mRateCoefParamStructMap.values()) {
            byte[] raw = coefParamStruct.getRaw();

            for (byte b : raw) {
                byteList.add(b);
            }
        }

        byte[] payload = new byte[byteList.size()];

        for (int i = 0; i < byteList.size(); i++) {
            payload[i] = byteList.get(i);
        }

        return payload;
    }

    AirohaLink getAirohaLink() {
        return mAirohaLink;
    }

    /**
     * App should implement this interface to receive PEQ API response.
     */
    public interface OnPeqStatusUiListener {
        /**
         * Indicating the completed actions after calling the APIs
         * @param action
         */
        void OnActionCompleted(final Action action);

        /**
         * Indicating errors occurs after calling the APIs
         * @param action
         */
        void OnActionError(final Action action);

        /**
         * Client App listens for this callback to get the stored User Input and parse the info to display on UI
         * @param peqUiDataStru
         */
        void OnLoadPeqUiData(final PeqUiDataStru peqUiDataStru);
    }
}
