package com.airoha.ab153x;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.airoha.android.lib.AntennaUT.AirohaRFMgr;
import com.airoha.android.lib.AntennaUT.AntennaUTListenerMgr;
import com.airoha.android.lib.AntennaUT.AntennaUtLogUiListener;
import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.transport.AirohaLink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AntennaUTService extends Service {
    private final static String TAG = "Airoha_" + AntennaUTService.class.getSimpleName();

    private AirohaLink mAirohaLink;
    private AirohaRFMgr mAirohaRfMgr;
    private AntennaUTListenerMgr mAntennaUTListenerMgr;

    public static ArrayAdapter<String> gAgentLogAdapter;
    public static ArrayAdapter<String> gPartnerLogAdapter;
    private boolean isConnected = false;
    private boolean isReporting = false;
    private int reportTimeIndex = 0;
    private int testRoleIndex = 0;

    //Antenna UT
    protected boolean mReportDataStatus;
    protected boolean mRoleState;
    protected boolean mCmdRunning;
    protected boolean mPartnerPrepared;
    protected boolean mAgentFinished;
    protected boolean mPartnerFinished;
    protected boolean mHasPrintPartnerErrMsg;

    final Handler mHandlerTime = new Handler();
    protected int mReportTime;
    protected int mRetryMaxTime = 10;
    protected int mRetryCount = 0;
    protected int mLogMaxCount = 50;

    protected String AgentLogFilename = null;
    protected String PartnerLogFilename = null;
    protected String StatisticsReportFilename = null;

    private boolean mStatisticsEnable = false;
    private int mStatisticsCount = 0;

    private int mAgentRssiCount = 0;
    private int mAgentHeadsetTotalRssi = 0;
    private int mAgentHeadsetMaxRssi = 0;
    private int mAgentHeadsetMinRssi = 0;
    private int mAgentPhoneTotalRssi = 0;
    private int mAgentPhoneMaxRssi = 0;
    private int mAgentPhoneMinRssi = 0;

    private int mPartnerRssiCount = 0;
    private int mPartnerHeadsetTotalRssi = 0;
    private int mPartnerHeadsetMaxRssi = 0;
    private int mPartnerHeadsetMinRssi = 0;
    private int mPartnerPhoneTotalRssi = 0;
    private int mPartnerPhoneMaxRssi = 0;
    private int mPartnerPhoneMinRssi = 0;


    public enum Role {
        Both,
        Agent,
        Partner
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mAirohaLink = new AirohaLink(this);
        mAirohaRfMgr = new AirohaRFMgr(mAirohaLink, mOnRfStatusUiListener);
        mAntennaUTListenerMgr = new AntennaUTListenerMgr();
        initFlagsNParameters();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mAirohaRfMgr = null;
        mAirohaLink = null;
        super.onDestroy();
    }

    public AirohaLink getAirohaLink(){
        return mAirohaLink;
    }

    public AirohaRFMgr getAirohaRfMgr(){
        if (mAirohaRfMgr == null) {
            mAirohaRfMgr = new AirohaRFMgr(mAirohaLink, mOnRfStatusUiListener);
        }
        return mAirohaRfMgr;
    }

    public void setConnectionStatus(boolean status) {
        isConnected = status;
    }
    public boolean getConnectionStatus() { return isConnected;}
    public void setReportStatus(boolean status) {
        isReporting = status;
    }
    public boolean getReportStatus() {
        return isReporting;
    }
    public void setReportTimeIndex(int index) {
        reportTimeIndex = index;
    }
    public int getReportTimeIndex() {
        return reportTimeIndex;
    }
    public void setTestRoleIndex(int index) {
        testRoleIndex = index;
    }
    public int getTestRoleIndex() {
        return testRoleIndex;
    }

    public class LocalBinder extends Binder {
        public AntennaUTService getService() {
            return AntennaUTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public void setForeground(String strUpate) {Intent resultIntent = new Intent(this, AntennaUTActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Because clicking the notification opens a new ("special") activity, there's  no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        0//PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Create the notification object through the builder
        Notification noti = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(strUpate)
                .setContentIntent(resultPendingIntent).build();

        // call startForeground
        startForeground(1234, noti);
    }

    private AirohaRFMgr.OnRfStatusUiListener mOnRfStatusUiListener = new AirohaRFMgr.OnRfStatusUiListener() {
        @Override
        public void OnActionCompleted(final int raceId, final byte[] packet, final int raceType) {
            if (raceId == 0x0D00) {
                List<Dst> dstList = new ArrayList<>();
                for (int i = RacePacket.IDX_PAYLOAD_START; i < packet.length - 1; i = i + 2) {
                    Dst dst = new Dst();
                    dst.Type = packet[i];
                    dst.Id = packet[i + 1];
                    dstList.add(dst);
                }
                for (Dst dst : dstList) {
                    if (dst.Type == AvailabeDst.RACE_CHANNEL_TYPE_AWSPEER) {
                        mPartnerPrepared = true;
                        return;
                    }
                }
            }
            if (raceId == 0x1700 && raceType == 0x5B) {
                mReportDataStatus = true;
            } else if (raceId == 0x1700 && raceType == 0x5D && mReportDataStatus) {
                AirohaRFMgr.AntennaInfo antennaInfo = mAirohaRfMgr.parseAntennaReport(packet);

                if (antennaInfo.getStatus() == 0) {
                    String msg = "Rssi:" + antennaInfo.getRssi()
                            + ", Phone Rssi:" + antennaInfo.getPhoneRssi()
                            + ", IfpErrCnt:" + antennaInfo.getIfpErrCnt()
                            + ", AclErrCnt:" + antennaInfo.getAclErrCnt()
                            + ", AudioPktNum:" + antennaInfo.getAudioPktNum()
                            + ", DspLostCnt:" + antennaInfo.getDspLostCnt()
                            + ", AagcRssi:" + antennaInfo.getAagcRssi()
                            + ", Phone AagcRssi:" + antennaInfo.getPhoneAagcRssi()
                            + ", AagcGain:" + antennaInfo.getAagcGain()
                            + ", Phone AagcGain:" + antennaInfo.getPhoneAagcGain();

                    if(antennaInfo.getIsDebugInfoExist()){
                        msg = msg + ", SyncStartCnt:" + antennaInfo.getSyncStartCnt()
                                + ", RecoveryCnt:" + antennaInfo.getRecoveryCnt()
                                + ", DropRecoveryCnt:" + antennaInfo.getDropRecoveryCnt()
                                + ", DspEmptyCnt:" + antennaInfo.getDspEmptyCnt()
                                + ", DspOutOfSyncCnt:" + antennaInfo.getDspOutOfSyncCnt()
                                + ", DspSeqLossWaitCnt:" + antennaInfo.getDspSeqLossWaitCnt()
                                + ", PiconetClock:0x" + String.format("%08x", antennaInfo.getPiconetClock())
                                + ", LowHeapDropCnt:" + antennaInfo.getLowHeapDropCnt()
                                + ", FullBufferDropCnt:" + antennaInfo.getFullBufferDropCnt()
                                + ", RemoteChMap:" + antennaInfo.getRemoteChMapStr()
                                + ", LocalChMap:" + antennaInfo.getLocalChMapStr();
                    }

                    addLogMsg(mRoleState, msg);

                    if(mStatisticsEnable) {
                        saveRssiInfo(mRoleState, antennaInfo.getRssi(), antennaInfo.getPhoneRssi());
                    }

                    if (mRoleState) {
                        mAgentFinished = true;
                        Log.d(TAG, "AgentFinished");
                    } else {
                        mPartnerFinished = true;
                        Log.d(TAG, "PartnerFinished");
                    }
                } else {
                    addLogMsg(mRoleState, "Report Status Error.");
                }
                mReportDataStatus = false;
                mCmdRunning = false;
                Log.d(TAG, "mCmdRunning = false");
            }
        }
    };

    public void addLogUiListerner(String name, AntennaUtLogUiListener listener)
    {
        if(name == null && listener == null) return;
        mAntennaUTListenerMgr.addListener(name, listener);
    }

    public void RemoveLogUiListerner(String name)
    {
        if(name == null) return;
        mAntennaUTListenerMgr.removeListener(name);
    }

    private void addLogMsg(boolean is_Agent, String msg) {

        Log.d(TAG, "isAgent" + is_Agent + ", msg = " + msg);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS    ");
        String timeStr = sdf.format(new Date());
        if(is_Agent)
            synchronized (AntennaUTService.gAgentLogAdapter) {
                AntennaUTService.gAgentLogAdapter.add(timeStr + msg);
                if(AntennaUTService.gAgentLogAdapter.getCount() >= mLogMaxCount)
                {
                    AntennaUTService.gAgentLogAdapter.remove(AntennaUTService.gAgentLogAdapter.getItem(0));
                }
            }
        else{
            synchronized (AntennaUTService.gPartnerLogAdapter) {
                AntennaUTService.gPartnerLogAdapter.add(timeStr + msg);
                if(AntennaUTService.gPartnerLogAdapter.getCount() >= mLogMaxCount)
                {
                    AntennaUTService.gPartnerLogAdapter.remove(AntennaUTService.gPartnerLogAdapter.getItem(0));
                }
            }
        }
        mAntennaUTListenerMgr.OnAddLog(is_Agent, timeStr + msg);
        writeLogMsgFile(is_Agent, timeStr + msg);
    }

    private void writeLogMsgFile(boolean is_Agent, String msg) {
        try {
            File mSDFile = Environment.getExternalStorageDirectory();
            String filename;
            if(is_Agent) {
                filename = AgentLogFilename;
            }
            else
            {
                filename = PartnerLogFilename;
            }

            File file = new File(mSDFile, filename);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.toString(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg + "\r\n");
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wirteStatisticsReportLogFile(String msg){
        try {
            File mSDFile = Environment.getExternalStorageDirectory();
            String filename = StatisticsReportFilename;

            File file = new File(mSDFile, filename);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.toString(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg + "\r\n");
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initFlagsNParameters() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        AgentLogFilename = "AntennaUT_Agent_" + timeStr + ".txt";
        PartnerLogFilename = "AntennaUT_Partner_" + timeStr + ".txt";
        mReportDataStatus = false;
        mCmdRunning = false;
        mReportTime = 1000;
        mPartnerPrepared = false;
        mRetryCount = 0;
        mHasPrintPartnerErrMsg = false;
        mAgentFinished = false;
        mPartnerFinished = false;
    }

    private void initStatisticsParameters() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        StatisticsReportFilename = "AntennaUT_StatisticsReport_" + timeStr + ".txt";
        mAgentRssiCount = 0;
        mAgentHeadsetTotalRssi = 0;
        mAgentHeadsetMaxRssi = 0;
        mAgentHeadsetMinRssi = 0;
        mAgentPhoneTotalRssi = 0;
        mAgentPhoneMaxRssi = 0;
        mAgentPhoneMinRssi = 0;

        mPartnerRssiCount = 0;
        mPartnerHeadsetTotalRssi = 0;
        mPartnerHeadsetMaxRssi = 0;
        mPartnerHeadsetMinRssi = 0;
        mPartnerPhoneTotalRssi = 0;
        mPartnerPhoneMaxRssi = 0;
        mPartnerPhoneMinRssi = 0;
    }

    public void startTest(int times) {
        if(times != 0){
            mStatisticsEnable = true;
            mStatisticsCount = times;
            initStatisticsParameters();
        }
        else{
            mStatisticsEnable = false;
        }
        initFlagsNParameters();

        mReportTime = (reportTimeIndex + 1) * 1000;
        if (testRoleIndex == Role.Both.ordinal()) {
            mAirohaRfMgr.checkPartnerStatus();
            mHandlerTime.postDelayed(checkBothFlow, 1000);
        } else if (testRoleIndex == Role.Agent.ordinal()) {
            mRoleState = true;
            mHandlerTime.postDelayed(checkAgentOrPartnerFlow, 1000);
        } else {
            mAirohaRfMgr.checkPartnerStatus();
            mRoleState = false;
            mHandlerTime.postDelayed(checkAgentOrPartnerFlow, 1000);
        }
    }

    private void saveRssiInfo(boolean role, int headset_rssi, int phone_rssi)
    {
        if(role){
            mAgentRssiCount++;
            mAgentHeadsetTotalRssi += headset_rssi;
            mAgentPhoneTotalRssi += phone_rssi;

            if(mAgentRssiCount == 1){
                mAgentHeadsetMaxRssi = headset_rssi;
                mAgentHeadsetMinRssi = headset_rssi;
                mAgentPhoneMaxRssi = phone_rssi;
                mAgentPhoneMinRssi = phone_rssi;
                return;
            }
            if(headset_rssi > mAgentHeadsetMaxRssi){
                mAgentHeadsetMaxRssi = headset_rssi;
            }
            else if(headset_rssi <= mAgentHeadsetMinRssi){
                mAgentHeadsetMinRssi = headset_rssi;
            }
            if(phone_rssi > mAgentPhoneMaxRssi){
                mAgentPhoneMaxRssi = phone_rssi;
            }
            else if(phone_rssi <= mAgentPhoneMinRssi){
                mAgentPhoneMinRssi = phone_rssi;
            }
        }
        else{
            mPartnerRssiCount++;
            mPartnerHeadsetTotalRssi += headset_rssi;
            mPartnerPhoneTotalRssi += phone_rssi;

            if(mPartnerRssiCount == 1){
                mPartnerHeadsetMaxRssi = headset_rssi;
                mPartnerHeadsetMinRssi = headset_rssi;
                mPartnerPhoneMaxRssi = phone_rssi;
                mPartnerPhoneMinRssi = phone_rssi;
                return;
            }
            if(headset_rssi > mPartnerHeadsetMaxRssi){
                mPartnerHeadsetMaxRssi = headset_rssi;
            }
            else if(headset_rssi <= mPartnerHeadsetMinRssi){
                mPartnerHeadsetMinRssi = headset_rssi;
            }
            if(phone_rssi > mPartnerPhoneMaxRssi){
                mPartnerPhoneMaxRssi = phone_rssi;
            }
            else if(phone_rssi <= mPartnerPhoneMinRssi){
                mPartnerPhoneMinRssi = phone_rssi;
            }
        }

        if(testRoleIndex != 0) {
            if (mAgentRssiCount >= mStatisticsCount || mPartnerRssiCount >= mStatisticsCount) {
                isReporting = false;
                mAntennaUTListenerMgr.OnReportStop();
                mAntennaUTListenerMgr.OnStatisticsReport(genStatisticsReport());

            }
        }
        else{
            if (mAgentRssiCount >= mStatisticsCount && mPartnerRssiCount >= mStatisticsCount) {
                isReporting = false;
                mAntennaUTListenerMgr.OnReportStop();
                mAntennaUTListenerMgr.OnStatisticsReport(genStatisticsReport());
            }
        }
    }

    private String genStatisticsReport(){

        DecimalFormat df =new DecimalFormat("#.##");

        String msg = "";
        msg += mStatisticsCount + " Times" + ", Report Interval: " + mReportTime / 1000 + "s" + "\r\n\r\n";
        if(mAgentRssiCount > 0) {
            msg += "Agent (Headset RSSI) \r\n"
                    + "RSSI_avg: " + df.format(mAgentHeadsetTotalRssi / mStatisticsCount) + "\r\n"
                    + "RSSI_min: " + mAgentHeadsetMinRssi + "\r\n"
                    + "RSSI_max: " + mAgentHeadsetMaxRssi + "\r\n"
                    + "RSSI diviation: " + Math.abs(mAgentHeadsetMaxRssi - mAgentHeadsetMinRssi) + "\r\n\r\n"
                    + "Agent (Phone RSSI) \r\n"
                    + "RSSI_avg: " + df.format(mAgentPhoneTotalRssi / mStatisticsCount) + "\r\n"
                    + "RSSI_min: " + mAgentPhoneMinRssi + "\r\n"
                    + "RSSI_max: " + mAgentPhoneMaxRssi + "\r\n"
                    + "RSSI diviation: " + Math.abs(mAgentPhoneMaxRssi - mAgentPhoneMinRssi) + "\r\n\r\n";
        }
        if(mPartnerRssiCount > 0){
            msg += "Partner (Headset RSSI) \r\n"
                    + "RSSI_avg: " + df.format(mPartnerHeadsetTotalRssi / mStatisticsCount) + "\r\n"
                    + "RSSI_min: " + mPartnerHeadsetMinRssi + "\r\n"
                    + "RSSI_max: " + mPartnerHeadsetMaxRssi + "\r\n"
                    + "RSSI diviation: " + Math.abs(mPartnerHeadsetMaxRssi - mPartnerHeadsetMinRssi) + "\r\n\r\n"
                    + "Partner (Phone RSSI) \r\n"
                    + "RSSI_avg: " + df.format(mPartnerPhoneTotalRssi / mStatisticsCount) + "\r\n"
                    + "RSSI_min: " + mPartnerPhoneMinRssi + "\r\n"
                    + "RSSI_max: " + mPartnerPhoneMaxRssi + "\r\n"
                    + "RSSI diviation: " + Math.abs(mPartnerPhoneMaxRssi - mPartnerPhoneMinRssi) + "\r\n\r\n";
        }
        wirteStatisticsReportLogFile(msg);
        return msg;
    }

    private final Runnable checkBothFlow = new Runnable()
    {
        public void run()
        {
            Log.d(TAG, "isReporting " + isReporting + ", mCmdRunning " + mCmdRunning
                    + ", mRoleState " + mRoleState
                    + ", mAgentFinished" + mAgentFinished
                    + ", mPartnerPrepared " + mPartnerPrepared);
            if(isReporting && !mCmdRunning) {
                mRetryCount = 0;
                mCmdRunning = true;
                if(!mAgentFinished)
                {
                    mRoleState = true;
                    mAirohaRfMgr.getAntennaReport(mRoleState);
                    mHandlerTime.postDelayed(this, 200);
                }
                else if(!mAgentFinished && !mPartnerPrepared)
                {
                    addLogMsg(false, "Partner is not connected.");
                }
                else if(!mPartnerFinished)
                {
                    mRoleState = false;
                    mAirohaRfMgr.getAntennaReport(mRoleState);
                    mHandlerTime.postDelayed(this, 200);
                }
                else {
                    mCmdRunning = false;
                    mAgentFinished = false;
                    mPartnerFinished = false;
                    mHandlerTime.postDelayed(this, mReportTime);
                }
            }
            else if(isReporting && mRetryCount < mRetryMaxTime)
            {
                mRetryCount++;
                mHandlerTime.postDelayed(this, 400);
            }
            else if(mRetryCount >= mRetryMaxTime)
            {
                mRetryCount = 0;
                mCmdRunning = false;
                addLogMsg(mRoleState, "No Response.");

                isReporting = false;
                mAntennaUTListenerMgr.OnReportStop();
            }
        }
    };

    private final Runnable checkAgentOrPartnerFlow = new Runnable() {
        public void run() {
            if (mRoleState == false && !mPartnerPrepared) {
                addLogMsg(false, "Partner is not connected.");
                return;
            }
            if (isReporting && !mCmdRunning) {
                mRetryCount = 0;
                mCmdRunning = true;
                mAirohaRfMgr.getAntennaReport(mRoleState);
                mHandlerTime.postDelayed(this, mReportTime);
            } else if (isReporting && mRetryCount < mRetryMaxTime) {
                mRetryCount++;
                mHandlerTime.postDelayed(this, mReportTime);
            } else if (mRetryCount >= mRetryMaxTime) {
                mRetryCount = 0;
                mCmdRunning = false;
                addLogMsg(mRoleState, "No Response.");

                isReporting = false;
                mAntennaUTListenerMgr.OnReportStop();
            }
        }
    };
}
