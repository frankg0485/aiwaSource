package com.airoha.android.lib.peq;


import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdRelayPass;
import com.airoha.android.lib.fota.stage.for153xMCE.RelayRespExtracter;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public abstract class PeqStage implements IPeqStage {
    protected AirohaPeqMgr mPeqMgr;

    protected AirohaLink mAirohaLink;

    protected boolean mIsCompleted;
    protected boolean mIsError;

    protected String TAG;

    public PeqStage(AirohaPeqMgr mgr) {
        mPeqMgr = mgr;

        mAirohaLink = mgr.getAirohaLink();

        TAG = this.getClass().getSimpleName();
    }

    public void sendCmd() {
        RacePacket cmd = genCmd();

        if(mIsRelay){
            cmd = createWrappedRelayPacket(cmd);
        }

        if (cmd != null)
            mAirohaLink.sendOrEnqueue(cmd.getRaw());
    }

    protected RacePacket genCmd() {
        return null;
    }

    public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
        if(mIsRelay && raceId != RaceId.RACE_RELAY_PASS_TO_DST)
            return;

        if (!mIsRelay && raceId != mRaceId)
            return;

        byte[] stripRelayHeaderPacket;
        int extractedRaceType;
        int extractedRaceId;
        byte status;

        if(mIsRelay){
            if(raceType != RaceType.INDICATION)
                return;

            stripRelayHeaderPacket = RelayRespExtracter.extractRelayRespPacket(packet);
            extractedRaceType = RelayRespExtracter.extractRaceType(stripRelayHeaderPacket);
            extractedRaceId = RelayRespExtracter.extractRaceId(stripRelayHeaderPacket);
            status = RelayRespExtracter.extractStatus(stripRelayHeaderPacket);

        } else {
            stripRelayHeaderPacket = packet;
            extractedRaceType = raceType;
            extractedRaceId = raceId;
            status = packet[RacePacket.IDX_PAYLOAD_START];
        }

        if(extractedRaceId!= mRaceId && extractedRaceType!= mRaceRespType)
            return;

        parsePayloadAndCheckCompeted(extractedRaceId, stripRelayHeaderPacket, status, extractedRaceType);
    }


    protected void parsePayloadAndCheckCompeted(final int raceId, final byte[] packet, byte status, int raceType) {
        if(status == 0x00) {
            mIsCompleted = true;
        }else {
            mIsError = true;
        }
    }

    public boolean isError() {
        mAirohaLink.logToFile(TAG, "isError: " + mIsError);
        return mIsError;
    }

    public boolean isCompleted() {
        mAirohaLink.logToFile(TAG, "isCompleted: " + mIsCompleted);
        return mIsCompleted;
    }

    protected RacePacket genReadNvKeyPacket(byte[] queryId) {
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_READFULLKEY);

//        byte[] queryId = Converter.shortToBytes((short) NvKeyId.AUDIO_PATH);
        byte[] queryLength = Converter.shortToBytes((short) 1000); // 0x03E8

        byte[] payload = new byte[]{queryId[0], queryId[1], queryLength[0], queryLength[1]};

        cmd.setPayload(payload);

        return cmd;
    }

    protected RacePacket genWriteNvKeyPacket(byte[] targetId, byte[] content) {
        assert targetId.length ==2;

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_WRITEFULLKEY);

        byte[] payload = new byte[targetId.length + content.length];

        System.arraycopy(targetId, 0, payload, 0, targetId.length);
        System.arraycopy(content, 0, payload, targetId.length, content.length);

        cmd.setPayload(payload);

        return cmd;
    }

    protected int mRaceId = 0;
    protected byte mRaceRespType = RaceType.RESPONSE;

    // for replay
    protected boolean mIsRelay = false;

//    protected int mRelayRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
//    protected byte mRelayRaceRespType = RaceType.INDICATION;

    protected RacePacket createWrappedRelayPacket(RacePacket cmd) {
        if(cmd == null)
            return null;

        return new RaceCmdRelayPass(mPeqMgr.getAwsPeerDst(), cmd);
    }
}
