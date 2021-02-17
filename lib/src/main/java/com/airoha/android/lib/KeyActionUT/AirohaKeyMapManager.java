package com.airoha.android.lib.KeyActionUT;

import com.airoha.android.lib.Common.ReadNvKey;
import com.airoha.android.lib.Common.ReadNvKeyRelay;
import com.airoha.android.lib.Common.ReloadNvkeyToRam;
import com.airoha.android.lib.Common.ReloadNvkeyToRamRelay;
import com.airoha.android.lib.Common.WriteNvKey;
import com.airoha.android.lib.Common.WriteNvKeyRelay;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetAudioChannelRelay;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetAvaDst;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_GetAudioChannel;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;

import java.util.Arrays;

public class AirohaKeyMapManager extends AirohaRaceOtaMgr {
    private static final String TAG = "AirohaRFMgr";

    private OnStatusUiListener mOnStatusUiListener;

    private OnRacePacketListener mOnRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            //Partner
            if(packet.length >= 14 && packet[0] == 0x05 && packet[1] == 0x5D && packet[4] == 0x01 && packet[5] == 0x0D){
                byte[] _packet = Arrays.copyOfRange(packet, 8, packet.length);
                int _raceId = _packet[4] + _packet[5] * 256;
                int _raceType = _packet[1];
                mOnStatusUiListener.OnActionCompleted(_raceId, _packet, _raceType, false);
            }
            //Agent
            else{
                mOnStatusUiListener.OnActionCompleted(raceId, packet, raceType, true);
            }
        }
    };

    public AirohaKeyMapManager(AirohaLink airohaLink, OnStatusUiListener listener) {
        super(airohaLink);
        mOnStatusUiListener = listener;
        mAirohaLink.registerOnRacePacketListener(TAG, mOnRacePacketListener);
    }


    public interface OnStatusUiListener {
        void OnActionCompleted(final int raceId, final byte[] packet, final int raceType, final boolean isAgent);
    }

//    public void getKeyMap(boolean ask_agent, boolean ask_partner) {
//        renewStageQueue();
//        if(ask_agent) {
//            mStagesQueue.offer(new ReadNvKey(this, (short) 0xF2E7));
//        }
//        if(ask_partner) {
//            mStagesQueue.offer(new ReadNvKeyRelay(this, (short) 0xF2E7));
//        }
//        startPollStagetQueue();
//    }

    public void setKeyMap(byte[]nv_value, boolean is_agent) {
        renewStageQueue();
        if(is_agent) {
            mStagesQueue.offer(new WriteNvKey(this, (short) 0xF2E7, nv_value));
            mStagesQueue.offer(new ReloadNvkeyToRam(this, (short) 0xF2E7));
        }
        else{
            mStagesQueue.offer(new WriteNvKeyRelay(this, (short) 0xF2E7, nv_value));
            mStagesQueue.offer(new ReloadNvkeyToRamRelay(this, (short) 0xF2E7));
        }
        startPollStagetQueue();
    }

    public void checkPartnerStatus() {
        renewStageQueue();
        mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));
        startPollStagetQueue();
    }

    public void checkChannelNgetKeyMap(boolean ask_agent, boolean ask_partner) {
        renewStageQueue();

        if(ask_agent) {
            mStagesQueue.offer(new FotaStage_00_GetAudioChannel(this));
        }
        if(ask_partner) {
            mStagesQueue.offer(new FotaStage_00_GetAudioChannelRelay(this));
        }
        if(ask_agent) {
            mStagesQueue.offer(new ReadNvKey(this, (short) 0xF2E7));
        }
        if(ask_partner) {
            mStagesQueue.offer(new ReadNvKeyRelay(this, (short) 0xF2E7));
        }
        startPollStagetQueue();
    }
}