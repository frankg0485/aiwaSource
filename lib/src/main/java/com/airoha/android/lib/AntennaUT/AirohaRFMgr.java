package com.airoha.android.lib.AntennaUT;

import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.fota.stage.for153xMCE.FotaStage_00_GetAvaDst;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;

import java.util.Arrays;


public class AirohaRFMgr extends AirohaRaceOtaMgr {
    private static final String TAG = "AirohaRFMgr";

    private OnRfStatusUiListener mOnRfStatusUiListener;
    private boolean mIsAgent;
    private boolean mCheckPartnerStatus = true;

    public class AntennaInfo {
        short Status;
        //RSSI info
        byte Rssi;
        byte PhoneRssi;
        long IfpErrCnt;
        long AclErrCnt;
        long AudioPktNum;
        long DspLostCnt;
        byte AagcRssi;
        byte PhoneAagcRssi;
        short AagcGain;
        short PhoneAagcGain;
        //Debug info
        boolean IsExistDebugInfo = false;
        long SyncStartCnt;
        long RecoveryCnt;
        long DropRecoveryCnt;
        long DspEmptyCnt;
        long DspOutOfSyncCnt;
        long DspSeqLossWaitCnt;
        long PiconetClock;
        int LowHeapDropCnt;
        int FullBufferDropCnt;
        short[] RemoteChMap = new short[10];
        short[] LocalChMap = new short[10];

        public short getStatus(){
            return Status;
        }
        public byte getRssi(){
            return Rssi;
        }
        public byte getPhoneRssi(){
            return PhoneRssi;
        }
        public long getIfpErrCnt(){
            return IfpErrCnt;
        }
        public long getAclErrCnt(){
            return AclErrCnt;
        }
        public long getAudioPktNum(){
            return AudioPktNum;
        }
        public long getDspLostCnt(){
            return DspLostCnt;
        }
        public byte getAagcRssi(){
            return AagcRssi;
        }
        public byte getPhoneAagcRssi(){
            return PhoneAagcRssi;
        }
        public short getAagcGain(){
            return AagcGain;
        }
        public short getPhoneAagcGain(){
            return PhoneAagcGain;
        }

        public boolean getIsDebugInfoExist(){
            return IsExistDebugInfo;
        }
        public long getSyncStartCnt(){
            return SyncStartCnt;
        }
        public long getRecoveryCnt(){
            return RecoveryCnt;
        }
        public long getDropRecoveryCnt(){
            return DropRecoveryCnt;
        }
        public long getDspEmptyCnt(){
            return DspEmptyCnt;
        }
        public long getDspOutOfSyncCnt(){
            return DspOutOfSyncCnt;
        }
        public long getDspSeqLossWaitCnt(){
            return DspSeqLossWaitCnt;
        }
        public long getPiconetClock(){
            return PiconetClock;
        }
        public int getLowHeapDropCnt(){
            return LowHeapDropCnt;
        }
        public int getFullBufferDropCnt(){
            return FullBufferDropCnt;
        }
        public String getRemoteChMapStr(){
            String rtn = "";
            for (int i = 0; i < 10; i++){
                if(i == 9){
                    rtn += RemoteChMap[i];
                }
                else {
                    rtn += RemoteChMap[i] + ", ";
                }
            }
            return rtn;
        }
        public String getLocalChMapStr(){
            String rtn = "";
            for (int i = 0; i < 10; i++){
                if(i == 9){
                    rtn += LocalChMap[i];
                }
                else {
                    rtn += LocalChMap[i] + ", ";
                }
            }
            return rtn;
        }
    }

    private OnRacePacketListener mOnRacePacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {

            if(mCheckPartnerStatus || mIsAgent){
                mOnRfStatusUiListener.OnActionCompleted(raceId, packet, raceType);
            }
            else if(!mIsAgent && packet.length > 8)
            {
                byte[] _packet = Arrays.copyOfRange(packet, 8, packet.length);
                int _raceId = _packet[4] + _packet[5] << 8;
                int _raceType = _packet[1];
                mOnRfStatusUiListener.OnActionCompleted(_raceId, _packet, _raceType);
            }
        }
    };

    public AirohaRFMgr(AirohaLink airohaLink, OnRfStatusUiListener listener) {
        super(airohaLink);
        mOnRfStatusUiListener = listener;
        mAirohaLink.registerOnRacePacketListener(TAG, mOnRacePacketListener);
    }

    public void getAntennaReport(boolean is_Agent) {
        renewStageQueue();
        mCheckPartnerStatus = false;
        mIsAgent = is_Agent;

        if(is_Agent) {
            FotaStage commit = new AntennaInfoRpt(this);
            mStagesQueue.offer(commit);
        }
        else{
            mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));
            FotaStage commit = new AntennaInfoRptRelay(this);
            mStagesQueue.offer(commit);
        }
        startPollStagetQueue();
    }

    public void checkPartnerStatus() {
        mCheckPartnerStatus = true;
        renewStageQueue();
        mStagesQueue.offer(new FotaStage_00_GetAvaDst(this));
        startPollStagetQueue();
    }

    public AntennaInfo parseAntennaReport(byte[] packet) {
        AntennaInfo rtn = new AntennaInfo();
        if (packet.length != 29 && packet.length != 81) {
            rtn.Status = (byte) 0xFF;
            return rtn;
        }
        rtn.Status = getShortValue(packet[6]);
        rtn.Rssi = packet[7];
        rtn.PhoneRssi = packet[8];
        rtn.IfpErrCnt = getLongValue(packet[9], packet[10], packet[11], packet[12]);
        rtn.AclErrCnt = getLongValue(packet[13], packet[14], packet[15], packet[16]);
        rtn.AudioPktNum = getLongValue(packet[17], packet[18], packet[19], packet[20]);
        rtn.DspLostCnt = getLongValue(packet[21], packet[22], packet[23], packet[24]);
        rtn.AagcRssi = packet[25];
        rtn.PhoneAagcRssi = packet[26];
        rtn.AagcGain = getShortValue(packet[27]);
        rtn.PhoneAagcGain = getShortValue(packet[28]);

        if(packet.length > 29){
            rtn.IsExistDebugInfo = true;
            rtn.SyncStartCnt = getLongValue(packet[29], packet[30], packet[31], packet[32]);
            rtn.RecoveryCnt = getLongValue(packet[33], packet[34], packet[35], packet[36]);
            rtn.DropRecoveryCnt = getLongValue(packet[37], packet[38], packet[39], packet[40]);
            rtn.DspEmptyCnt = getLongValue(packet[41], packet[42], packet[43], packet[44]);
            rtn.DspOutOfSyncCnt = getLongValue(packet[45], packet[46], packet[47], packet[48]);
            rtn.DspSeqLossWaitCnt = getLongValue(packet[49], packet[50], packet[51], packet[52]);
            rtn.PiconetClock = getLongValue(packet[53], packet[54], packet[55], packet[56]);
            rtn.LowHeapDropCnt = getIntValue(packet[57], packet[58]);
            rtn.FullBufferDropCnt = getIntValue(packet[59], packet[60]);
            for (int i = 0; i < 10; i++){
                rtn.RemoteChMap[i] = getShortValue(packet[61 + i]);
                rtn.LocalChMap[i] = getShortValue(packet[71 + i]);
            }
        }

        return rtn;
    }

    private long getLongValue(byte b0, byte b1, byte b2, byte b3){
        long rtn = 0L;
        rtn = rtn + ((long)b0 & 0xff)
                + (((long)b1 & 0xff) << 8)
                + (((long)b2 & 0xff) << 16)
                + (((long)b3 & 0xff) << 24);
        return rtn;
    }

    private int getIntValue(byte b0, byte b1){
        int rtn = 0;
        rtn = rtn + ((int)b0 & 0xff)
                + (((int)b1 & 0xff) << 8);
        return rtn;
    }

    private short getShortValue(byte b0){
        short rtn = 0;
        rtn += ((short)b0 & 0xff);
        return rtn;
    }

    public interface OnRfStatusUiListener {
        void OnActionCompleted(final int raceId, final byte[] packet, final int raceType);
    }
}

