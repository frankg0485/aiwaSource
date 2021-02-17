package com.airoha.android.lib.minidump;

import android.os.Handler;
import android.os.Message;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.stage.MmiStage;
import com.airoha.android.lib.offlinedump.StageAssert;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLogRaw;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AirohaMiniDumpMgr extends AirohaMmiMgr {
    private static final String TAG = "AirohaMiniDumpMgr";

    private String timeStamp = "";
    private int mRespCount = 0;
    private int mPageCount = 0;
    private int mAddress = 0;
    AirorhaLinkDbgLogRaw mMiniDumpRaw = null;
    private Handler mHandler;

    private OnRacePacketListener mAirDumpPacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            if(raceId == RaceId.RACE_GET_BOOT_REASON)
            {
                byte[] tmp = new byte[4];
                System.arraycopy(packet, 7, tmp, 0, 4);
                int ret = Converter.bytesToInt32(tmp);
                Message msg = Message.obtain();
                msg.what = AirohaRaceOtaMgr.BOOT_REASON;
                msg.obj = "Reason: " + Integer.toString(ret);
                mHandler.sendMessage(msg);
            }

            if(raceId == RaceId.RACE_GET_DUMP_ADDR)
            {
                byte[] length = new byte[4];
                byte[] address = new byte[4];
                System.arraycopy(packet, 7, length, 0, 4);
                // 0x08000000 ~ 0x0bffffff : internal flash
                // 0x0c000000 ~ 0x0fffffff: external flash
                // 要去掉前面的0x08和0x0c
                System.arraycopy(packet, 11, address, 0, 3);
                int retLength = Converter.bytesToInt32(length);
                int retAddress = Converter.bytesToInt32(address);
                Message msg = Message.obtain();
                msg.what = AirohaRaceOtaMgr.DUMP_INFO;
                msg.obj = "Length: " + Integer.toString(retLength) + "\nAddress: " + Integer.toString(retAddress) ;
                mHandler.sendMessage(msg);

                // 計算要read幾個page
                mPageCount = retLength/256;
                mAddress = retAddress;
                //startDump(retAddress, mPageCount);
            }

            if(raceId == RaceId.RACE_STORAGE_PAGE_READ)
            {
                //if (mCurrentStage.isCompleted()) {
                byte[] data = new byte[256];
                System.arraycopy(packet, 14, data, 0, 256);
                mMiniDumpRaw.addRawBytesToQueue(data);

                Message msg = Message.obtain();
                msg.what = AirohaRaceOtaMgr.DUMP_INFO;
                msg.obj = Converter.byte2HexStr(data);
                mHandler.sendMessage(msg);

                mRespCount++;
                if(mRespCount == mPageCount)
                {
                    mMiniDumpRaw.stop();
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
    public AirohaMiniDumpMgr(AirohaLink airohaLink, Handler handler) {
        super(airohaLink);
        mAirohaLink.registerOnRacePacketListener(TAG, mAirDumpPacketListener);
        mHandler = handler;
    }

    public void startDump() {
        if(mPageCount == 0) {
            Message msg = Message.obtain();
            msg.what = AirohaRaceOtaMgr.DUMP_COMPLETE;
            mHandler.sendMessage(msg);
            return;
        }

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        mMiniDumpRaw = new AirorhaLinkDbgLogRaw("mini_dump_"+timeStamp+".bin");
        mMiniDumpRaw.startLogger();

        renewStageQueue();

        for(int i = 0; i < mPageCount; i++)
        {
            MmiStage commit = new StageDump(this);
            ((StageDump) commit).payload[0] = 0;  // storage type
            ((StageDump) commit).payload[1] = 1;  // page num

            byte[] addrArr = Converter.intToByteArray(mAddress);
            ((StageDump) commit).payload[2] = addrArr[0];
            ((StageDump) commit).payload[3] = addrArr[1];
            ((StageDump) commit).payload[4] = addrArr[2];
            ((StageDump) commit).payload[5] = addrArr[3];

            mStagesQueue.offer(commit);
            mAddress += 256;
        }

        startPollStagetQueue();
    }

    public void getReason() {
        renewStageQueue();

        MmiStage commit = new StageGetBootReason(this);

        mStagesQueue.offer(commit);

        startPollStagetQueue();

        getDumpAddress();
    }

    public void getDumpAddress() {
        mRespCount = 0;

        renewStageQueue();

        MmiStage commit = new StageGetDumpAddress(this);

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
