package com.airoha.android.lib.mmi.stage;

import android.os.SystemClock;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdRelayPass;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.for153xMCE.RelayRespExtracter;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class MmiStage implements IAirohaMmiStage {
    protected String TAG = "FotaStage";

    protected AirohaLink mAirohaLink;

    protected AirohaMmiMgr mMmiMgr;

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

    private int mInitQueueSize = 0;
    private int mCompletedTaskCount = 0;


    protected boolean mIsErrorOccurred = false;

    protected String mStrErrorReason = "Unknown";


    public MmiStage(AirohaMmiMgr mgr) {
        mMmiMgr = mgr;
        mAirohaLink = mgr.getAirohaLink();
        mCmdPacketQueue = new ConcurrentLinkedQueue<>();
        mCmdPacketMap = new LinkedHashMap<>();
        mRaceRespType = RaceType.RESPONSE;

        TAG = this.getClass().getSimpleName();
    }

    public static int getPrePollSize() {
        return PRE_POLL_SIZE;
    }

    public static void setPrePollSize(int prePollSize) {
        PRE_POLL_SIZE = prePollSize;
    }

    public static int getDelayPollTime() {
        return DELAY_POLL_TIME;
    }

    public static void setDelayPollTime(int delayPollTime) {
        DELAY_POLL_TIME = delayPollTime;
    }


    protected void genRacePackets() {

    }

    @Override
    public void start() {
        if (mIsStopped)
            return;

        genRacePackets();
        mInitQueueSize = mCmdPacketQueue.size();
        mAirohaLink.logToFile(TAG, "mInitQueueSize: " + mInitQueueSize);
        prePoolCmdQueue();
    }

    @Override
    public void pollCmdQueue() {

        mAirohaLink.logToFile(TAG, " pollCmdQueue mCmdPacketQueue.size() = " + mCmdPacketQueue.size());
        if (mCmdPacketQueue.size() != 0) {

            if (DELAY_POLL_TIME > 0) {
                SystemClock.sleep(DELAY_POLL_TIME);
            }

            poolCmdToSend();
        }
    }

    @Override
    public void prePoolCmdQueue() {
        if (mCmdPacketQueue.size() != 0) {

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

//            if (cmd.isNeedResp()) {
//                mMmiMgr.startRespTimer();
//            }
        }
    }

    protected void parsePayloadAndCheckCompeted(final int raceId, final byte[] packet, byte status, int raceType) {

    }

    @Override
    public void handleResp(final int raceId, final byte[] packet, int raceType) {
        if (raceId != mRaceId)
            return;

        int idx = RacePacket.IDX_PAYLOAD_START;
        mAirohaLink.logToFile(TAG, "Rx packet: " + Converter.byte2HexStr(packet));
        mIsRespSuccess = false;

        if (mIsRelay) {
            byte[] stripRelayHeaderPacket = RelayRespExtracter.extractRelayRespPacket(packet);
            byte extractedRaceType = RelayRespExtracter.extractRaceType(stripRelayHeaderPacket);
            int extractedRaceId = RelayRespExtracter.extractRaceId(stripRelayHeaderPacket);

            if (extractedRaceType != mRelayRaceRespType || extractedRaceId != mRelayRaceId)
                return;

            mStatusCode = RelayRespExtracter.extractStatus(stripRelayHeaderPacket);
            parsePayloadAndCheckCompeted(extractedRaceId, stripRelayHeaderPacket, mStatusCode, extractedRaceType);
        } else {
            mStatusCode = packet[idx];
            parsePayloadAndCheckCompeted(raceId, packet, mStatusCode, raceType);
        }

        if (mStatusCode == StatusCode.FOTA_ERRCODE_SUCESS) {
            mIsRespSuccess = true;
            mCompletedTaskCount++;
        } else {
            mIsRespSuccess = false;
        }

        mAirohaLink.logToFile(TAG, "mStatusCode =" + mStatusCode);
        mAirohaLink.logToFile(TAG, "mIsRespSuccess =" + mIsRespSuccess);
    }

    @Override
    public boolean isCompleted() {
        for (RacePacket cmd : mCmdPacketMap.values()) {
            if (!cmd.isRespStatusSuccess()) {
                return false;
            }
        }

        return true;
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
    public byte getRespType() {
        return mRaceRespType;
    }

    @Override
    public boolean isExpectedResp(int raceId, int raceType, byte[] packet) {
        if (mIsRelay) {
            if (packet.length < 9)
                return false;

            byte[] stripRelayHeaderPacket = RelayRespExtracter.extractRelayRespPacket(packet);
            byte extractedRaceType = RelayRespExtracter.extractRaceType(stripRelayHeaderPacket);
            int extractedRaceId = RelayRespExtracter.extractRaceId(stripRelayHeaderPacket);

            return extractedRaceType == mRelayRaceRespType && extractedRaceId == mRelayRaceId;
        } else {
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
        if (mCmdPacketQueue != null) {
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

    @Override
    public String getSimpleName() {
        return TAG;
    }

    // for replay
    protected boolean mIsRelay = false;

    protected int mRelayRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
    protected byte mRelayRaceRespType = RaceType.INDICATION;

    protected RacePacket createWrappedRelayPacket(RacePacket cmd) {
        return new RaceCmdRelayPass(mMmiMgr.getAwsPeerDst(), cmd);
    }
}
