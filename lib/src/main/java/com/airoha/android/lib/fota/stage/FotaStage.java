package com.airoha.android.lib.fota.stage;

import android.os.SystemClock;
import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdRelayPass;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.for153xMCE.RelayRespExtracter;
import com.airoha.android.lib.fota.stage.for153xMCE.RespQueryPartitionInfo;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.SHA256;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FotaStage implements IAirohaFotaStage {
    protected String TAG = "FotaStage";

    protected AirohaLink mAirohaLink;

    protected AirohaRaceOtaMgr mOtaMgr;

    protected Queue<RacePacket> mCmdPacketQueue; // ConcurrentLinkedQueue<>()

    protected Map<String, RacePacket> mCmdPacketMap;

    //protected final String KEY_SINGLE_CMD = "0";

    protected boolean mIsLongPacketMode = false;

    private boolean mIsStopped = false;

    protected int mWaitingRespCount = 0;

    protected volatile boolean mIsRespSuccess;

    private static int PRE_POLL_SIZE = 4;

    private static int DELAY_POLL_TIME = 0;

    protected byte mStatusCode = (byte) 0xFF;

    protected int mRaceId = 0;
    protected byte mRaceRespType = RaceType.RESPONSE;

    protected int mInitQueueSize = 0;
    protected int mCompletedTaskCount = 0;

    protected static final int INT_4K = 0x1000;
    protected static final byte[] BYTES_4K = new byte[]{0x00, 0x10, 0x00, 0x00};

//    public static LinkedHashMap<String, PARTITION_DATA> gSingleDeviceDiffPartitions = null;
    public static LinkedList<PARTITION_DATA> gSingleDeviceDiffPartitionsList;
    public static LinkedHashMap<String, PARTITION_DATA> gTwsRightDeviceDiffPartitions = null;
    public static LinkedHashMap<String, PARTITION_DATA> gTwsLeftDeviceDiffPartitions = null;

    protected static RespQueryPartitionInfo[] gRespQueryPartitionInfos = null;

    protected SKIP_TYPE mSkipType = SKIP_TYPE.None;

    protected HashMap<SKIP_TYPE, LinkedList<FotaStage>> mPartialStagesForSkipped = new HashMap<>();

    protected boolean mIsErrorOccurred = false;

    protected String mStrErrorReason = "Unknown";

    protected long mStartTime;

    public class PARTITION_DATA {
        static final int MAX_DATA_LEN = 0x1000;

        public byte[] mAddr;
        public int mDataLen;
        public byte[] mData;
        public byte[] mSHA256;
        public boolean mIsDiff;
        public boolean mIsErased;

        public PARTITION_DATA(byte[] addr, byte[] data, int len) {
            if (len > MAX_DATA_LEN) {
                return;
            }

            mAddr = new byte[4];
            mData = new byte[len];
            mDataLen = len;
            mIsDiff = true;
            mIsErased = false;

            if (addr != null) {
                System.arraycopy(addr, 0, mAddr, 0, 4);
            }

            if (data != null) {
                System.arraycopy(data, 0, mData, 0, len);
                mSHA256 = SHA256.calculate(mData);
            }
        }
    }

    public FotaStage(AirohaRaceOtaMgr mgr) {
        mOtaMgr = mgr;
        mAirohaLink = mgr.getAirohaLink();
        mCmdPacketQueue = new ConcurrentLinkedQueue<>();
        mCmdPacketMap = new LinkedHashMap<>();
        mRaceRespType = RaceType.RESPONSE;

        TAG = getClass().getSimpleName();
    }

    public static int getPrePollSize() {
        return PRE_POLL_SIZE;
    }

    public static void setPrePollSize(int prePollSize) {
        PRE_POLL_SIZE = prePollSize;
    }

    public static void setDelayPollTime(int delayPollTime) {
        DELAY_POLL_TIME = delayPollTime;
    }

    public void addStageForPartialSkip(SKIP_TYPE type, FotaStage stage) {
        if (mPartialStagesForSkipped.containsKey(type)) {
            mPartialStagesForSkipped.get(type).add(stage);
        } else {
            LinkedList<FotaStage> tmp = new LinkedList<>();
            tmp.add(stage);
            mPartialStagesForSkipped.put(type, tmp);
        }
    }

    @Override
    public void genRacePackets() {

    }

    @Override
    public void start() {
        if(mIsStopped)
            return;

        mStartTime = System.currentTimeMillis();

        genRacePackets();
        mInitQueueSize = mCmdPacketQueue.size();
        mAirohaLink.logToFile(TAG, "mInitQueueSize: " + mInitQueueSize);
        prePoolCmdQueue();
    }

    @Override
    public void pollCmdQueue() {

        mAirohaLink.logToFile(TAG, " pollCmdQueue mCmdPacketQueue.size() = " + mCmdPacketQueue.size());
        if (mCmdPacketQueue.size() != 0) {

            if(mOtaMgr.isLongPacketMode() && mWaitingRespCount == 0){
//                SystemClock.sleep(DELAY_POLL_TIME);

                poolCmdToSendLongPacketMode();
                return;
            }

            if(mOtaMgr.isLongPacketMode()){
                return;
            }

            if(DELAY_POLL_TIME > 0){
                SystemClock.sleep(DELAY_POLL_TIME);
            }

            poolCmdToSend();
        }
    }

    @Override
    public void prePoolCmdQueue() {
        if (mCmdPacketQueue.size() != 0) {
            if(mOtaMgr.isLongPacketMode()){
                poolCmdToSendLongPacketMode();
                return;
            }

            if (mCmdPacketQueue.size() >= 2) {
                mAirohaLink.logToFile(TAG, " PrePollSize = " + getPrePollSize());
                for (int i = 0; i < getPrePollSize(); i++) {
                    poolCmdToSend();
                }
            } else {
                poolCmdToSend();
            }
        }
    }

    private void poolCmdToSend() {
        RacePacket cmd = mCmdPacketQueue.poll();
        if (cmd != null) {
            mAirohaLink.sendCommand(cmd.getRaw());

            if (cmd.isNeedResp()) {
                mOtaMgr.startRespTimer();
            }
        }
    }

    private void poolCmdToSendLongPacketMode() {

        List<RacePacket> list = new ArrayList<>();

        int singleCmdArraySize = 0;

        for (int i = 0; i < mOtaMgr.getLongPacketCmdCount(); i++) {
            RacePacket cmd = mCmdPacketQueue.poll();
            if (cmd != null) {
                list.add(cmd);

                singleCmdArraySize = cmd.getRaw().length;
            }
        }

        if (list.size() > 0) {
            mWaitingRespCount = list.size();

            byte[] totalRaws = new byte[list.size() * singleCmdArraySize];

            for(int i = 0; i< list.size() ; i++){
                byte[] raw = list.get(i).getRaw();

                System.arraycopy(raw, 0, totalRaws, i*singleCmdArraySize, singleCmdArraySize);
            }

            mAirohaLink.logToFile(TAG, "long packet delay sleeping");
            SystemClock.sleep(DELAY_POLL_TIME);

            mAirohaLink.sendCommand(totalRaws);
        }
    }

    @Override
    public void parsePayloadAndCheckCompeted(final int raceId, final byte[] packet, byte status, int raceType) {

    }

    @Override
    public void handleResp(final int raceId, final byte[] packet, int raceType) {
        if (raceId != mRaceId)
            return;

        int idx = RacePacket.IDX_PAYLOAD_START;


        mAirohaLink.logToFile(TAG, "Rx packet: " + Converter.byte2HexStr(packet));


        if(mIsRelay){
            // need to strip relay header
            // start from the RacePacket.IDX_PAYLOAD_START

            //             (pass to dst notification)    (race cmd rsp from partner)
            //Agent:  05 5D 0E 00 01 0D 05 04 05 5B 06 00 00 0D 00 00 03 03

            // stripped: 05 5B 06 00 00 0D 00 00 03 03

            byte[] stripRelayHeaderPacket = RelayRespExtracter.extractRelayRespPacket(packet);
            byte extractedRaceType = RelayRespExtracter.extractRaceType(stripRelayHeaderPacket);
            int extractedRaceId = RelayRespExtracter.extractRaceId(stripRelayHeaderPacket);

            if(extractedRaceType != mRelayRaceRespType || extractedRaceId != mRelayRaceId)
                return;

            mStatusCode = RelayRespExtracter.extractStatus(stripRelayHeaderPacket);

            parsePayloadAndCheckCompeted(extractedRaceId, stripRelayHeaderPacket, mStatusCode, extractedRaceType);
        }else {
            mStatusCode = packet[idx];
            parsePayloadAndCheckCompeted(raceId, packet, mStatusCode, raceType);
        }

        if (mStatusCode == StatusCode.FOTA_ERRCODE_SUCESS) {
            mIsRespSuccess = true;

            mCompletedTaskCount++;
        } else {
            mIsRespSuccess = false;
        }

        if(mOtaMgr.isLongPacketMode()){
            mWaitingRespCount--;

            mAirohaLink.logToFile(TAG, "mWaitingRespCount: " + mWaitingRespCount);
        }
    }

    @Override
    public boolean isCompleted() {
        for (RacePacket cmd : mCmdPacketMap.values()) {
            if (!cmd.isRespStatusSuccess()) {
                return false;
            }
        }

        logCompletedTime();
        return true;
    }

    protected void logCompletedTime(){
        mAirohaLink.logToFile(TAG, "time elapsed: " + (System.currentTimeMillis()-mStartTime));
    }

    @Override
    public boolean isCmdQueueEmpty() {
        return (mCmdPacketQueue.isEmpty());
    }

    @Override
    public boolean isRetryUpToLimit() {
        // clear, refill will be called for several retry times
        mCmdPacketQueue.clear();

        for (RacePacket cmd : mCmdPacketMap.values()) {
            if (cmd.isRetryUpperLimit()) {
                mAirohaLink.logToFile(TAG, "retry reach upper limit: " + cmd.toHexString());
                return true;
            }

            if (!cmd.isRespStatusSuccess()) {
                mAirohaLink.logToFile(TAG, "refill the retry cmd to CmdQueue: " + cmd.toHexString());

                cmd.increaseRetryCounter();
                mCmdPacketQueue.offer(cmd);
            }
        }
        return false;
    }

    @Override
    public SKIP_TYPE getSkipType() {
        mAirohaLink.logToFile(TAG, "mSkipType:" + mSkipType.toString());
        return mSkipType;
    }

    @Override
    public LinkedList<FotaStage> getStagesForSkip(SKIP_TYPE type) {
        return mPartialStagesForSkipped.get(type);
    }

    @Override
    public byte getRespType() {
        return mRaceRespType;
    }

    @Override
    public boolean isExpectedResp(int raceType, int raceId, byte[] packet) {
        if(mIsRelay){
            if(packet.length < 9 )
                return false;

            byte[] stripRelayHeaderPacket = RelayRespExtracter.extractRelayRespPacket(packet);
            byte extractedRaceType = RelayRespExtracter.extractRaceType(stripRelayHeaderPacket);
            int extractedRaceId = RelayRespExtracter.extractRaceId(stripRelayHeaderPacket);

            return extractedRaceType == mRelayRaceRespType && extractedRaceId == mRelayRaceId;
        }else {
            return raceType == mRaceRespType && raceId == mRaceId;
        }
    }

    @Override
    public boolean isRespStatusSuccess() {
        mAirohaLink.logToFile(TAG, "mIsRespSuccess: " + String.valueOf(mIsRespSuccess));
        return mIsRespSuccess;
    }

    @Override
    public byte getStatus() {
        // for debug, not necessary for the flow
        return mStatusCode;
    }

    @Override
    public void stop() {
        if(mCmdPacketQueue !=null){
            mCmdPacketQueue.clear();
        }

        mIsStopped = true;
    }

    @Override
    public boolean isStopped() {
        return mIsStopped;
    }

    @Override
    public int getTotalTaskCount() {
        return mInitQueueSize;
    }

    @Override
    public int getCompletedTaskCount() {
        return mCompletedTaskCount;
    }

    @Override
    public boolean isErrorOccurred() {
        return mIsErrorOccurred;
    }

    @Override
    public String getErrorReason() {
        return mStrErrorReason;
    }


    // for replay
    protected boolean mIsRelay = false;

    protected int mRelayRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
    protected byte mRelayRaceRespType = RaceType.INDICATION;

    protected RacePacket createWrappedRelayPacket(RacePacket cmd) {
        return new RaceCmdRelayPass(mOtaMgr.getAwsPeerDst(), cmd);
    }

    protected void placeCmd(RacePacket cmd){

    }

    protected void placeCmd(RacePacket cmd, String key){

    }

    protected RacePacket genReadNvKeyPacket(byte[] queryId) {
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_READFULLKEY);

//        byte[] queryId = Converter.shortToBytes((short) NvKeyId.AUDIO_PATH);
        byte[] queryLength = Converter.shortToBytes((short) 1000); // 0x03E8

        byte[] payload = new byte[]{queryId[0], queryId[1], queryLength[0], queryLength[1]};

        cmd.setPayload(payload);

        return cmd;
    }
}
