package com.airoha.android.lib.onlinedump;

import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.OnAirohaStatusUiListener;
import com.airoha.android.lib.mmi.stage.MmiStage;
import com.airoha.android.lib.offlinedump.StageAssert;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLogRaw;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AirohaOnlineDumpMgr extends AirohaMmiMgr {
    private static final String TAG = "AirohaOnlineDumpMgr";

    private OnAirohaStatusUiListener mStatusUiListener;
    private AirorhaLinkDbgLogRaw mlogger;
    private String timeStamp = "";

    private OnRacePacketListener mAirDumpPacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {
            // write to log
            if(packet[2] == 3)
                return;

            mlogger.addRawBytesToQueue(packet);
            mStatusUiListener.OnActionCompleted(Converter.byte2HexStr(packet));
        }
    };

    /**
     * Need to have the connected AirohaLink
     *
     * @param airohaLink
     */
    public AirohaOnlineDumpMgr(AirohaLink airohaLink, OnAirohaStatusUiListener listener) {
        super(airohaLink);
        mAirohaLink.registerOnRacePacketListener(TAG, mAirDumpPacketListener);
        mStatusUiListener = listener;
    }

    public void startDump() {

        renewStageQueue();

        MmiStage commit = new StageOnlineDump(this);
        ((StageOnlineDump) commit).payload[0] = 1;
        mStagesQueue.offer(commit);

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        mlogger = new AirorhaLinkDbgLogRaw("online_log_"+timeStamp+".nma");
        mlogger.startLogger();

        startPollStagetQueue();
    }

    public void stopDump() {
        renewStageQueue();

        MmiStage commit = new StageOnlineDump(this);
        ((StageOnlineDump) commit).payload[0] = 0;
        mStagesQueue.offer(commit);

        //mStatusUiListener.OnActionCompleted("Tx: " + Converter.byte2HexStr(((StageAirDump) commit).getRaw()));

        startPollStagetQueue();

        mlogger.stop();
    }

    public void getBuildInfo() {
        renewStageQueue();

        MmiStage commit = new StageGetBuildInfo(this);
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
