package com.airoha.android.lib.offlinedump;

import android.os.Handler;
import android.os.Message;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.minidump.StageDump;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.OnAirohaStatusUiListener;
import com.airoha.android.lib.mmi.stage.MmiStage;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLogRaw;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AirohaOfflineDumpMgr extends AirohaMmiMgr {
    private static final String TAG = "AirohaOfflineDumpMgr";

    public String timeStamp = "";
    private int mRespCount = 0;
    private int mPageCount = 0;
    private int mRetLength = 0;
    private int mRetAddress = 0;
    AirorhaLinkDbgLogRaw mOfflineDumpRaw = null;

    private Handler mHandler;

    private OnRacePacketListener mAirDumpPacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            if(raceId == RaceId.RACE_GET_OFFLINE_DUMP_ADDR)
            {
                byte[] length = new byte[4];
                byte[] address = new byte[4];
                System.arraycopy(packet, 7, length, 0, 4);
                // 0x08000000 ~ 0x0bffffff : internal flash
                // 0x0c000000 ~ 0x0fffffff: external flash
                // 要去掉前面的0x08和0x0c
                System.arraycopy(packet, 11, address, 0, 3);
                mRetLength = Converter.bytesToInt32(length);
                mRetAddress = Converter.bytesToInt32(address);

                Message msg = Message.obtain();
                msg.what = AirohaRaceOtaMgr.DUMP_INFO;
                msg.obj = "Length: " + Integer.toString(mRetLength) + " \nAddress: " + Integer.toString(mRetAddress) ;
                mHandler.sendMessage(msg);

                // 計算要read幾個page
                mPageCount = mRetLength/256;
            }

            if(raceId == RaceId.RACE_STORAGE_PAGE_READ)
            {
                //if (mCurrentStage.isCompleted()) {
                byte[] data = new byte[256];
                System.arraycopy(packet, 14, data, 0, 256);
                mOfflineDumpRaw.addRawBytesToQueue(data);
                mRespCount++;
                Message msg = Message.obtain();
                msg.what = AirohaRaceOtaMgr.DUMP_INFO;
                msg.obj = Converter.byte2HexStr(packet);
                mHandler.sendMessage(msg);

                if(mRespCount == mPageCount)
                {
                    mOfflineDumpRaw.stop();
                    Message msg2 = Message.obtain();
                    msg2.what = AirohaRaceOtaMgr.DUMP_COMPLETE;
                    mHandler.sendMessage(msg2);
                }
                //}
            }
        }
    };

    /**
     * Need to have the connected AirohaLink
     *
     * @param airohaLink
     */
    public AirohaOfflineDumpMgr(AirohaLink airohaLink, Handler handler) {
        super(airohaLink);
        mAirohaLink.registerOnRacePacketListener(TAG, mAirDumpPacketListener);
        mHandler = handler;
    }

    public void startDump() {
        if(mPageCount == 0) {
            return;
        }

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        mOfflineDumpRaw = new AirorhaLinkDbgLogRaw("offline_log_"+timeStamp+".bin");
        mOfflineDumpRaw.startLogger();

        renewStageQueue();

        for(int i = 0; i < mPageCount; i++)
        {
            MmiStage commit = new StageDump(this);
            ((StageDump) commit).payload[0] = 0;  // storage type
            ((StageDump) commit).payload[1] = 1;  // page num

            byte[] addrArr = Converter.intToByteArray(mRetAddress);
            ((StageDump) commit).payload[2] = addrArr[0];
            ((StageDump) commit).payload[3] = addrArr[1];
            ((StageDump) commit).payload[4] = addrArr[2];
            ((StageDump) commit).payload[5] = addrArr[3];

            mStagesQueue.offer(commit);
            mRetAddress += 256;
        }

        startPollStagetQueue();
    }

    public void getDumpAddress() {
        mRespCount = 0;

        renewStageQueue();

        MmiStage commit = new StageGetOfflineDumpAddress(this);

        mStagesQueue.offer(commit);

        startPollStagetQueue();
    }

    public void makeAssert() {
        renewStageQueue();

        MmiStage commit = new StageAssert(this);

        mStagesQueue.offer(commit);

        startPollStagetQueue();
    }

}
