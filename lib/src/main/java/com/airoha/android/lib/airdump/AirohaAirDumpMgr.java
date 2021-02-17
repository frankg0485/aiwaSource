package com.airoha.android.lib.airdump;

import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.OnAirohaStatusUiListener;
import com.airoha.android.lib.mmi.stage.MmiStage;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.LogEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AirohaAirDumpMgr extends AirohaMmiMgr {
    private static final String TAG = "AirohaAirDumpMgr";

    public String timeStamp = "";
    private OnAirohaStatusUiListener mStatusUiListener;
    private AirDumpLogger mlogger;

    private OnRacePacketListener mAirDumpPacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, byte[] packet, int raceType) {

            // write to log
            if(packet[2] == 3)
                return;

            //mStatusUiListener.OnActionCompleted("Rx: " + Converter.byte2HexStr(packet));

            short len = Converter.BytesToShort(packet[3], packet[2]);
            int dataLength = len - 2;

            byte[] dest = new byte[dataLength];
            System.arraycopy( packet, 6, dest, 0, dataLength);

            StringBuilder sb = new StringBuilder();
            int usCount = 0;
            for(int i = 0; i < dest.length; i+=2)
            {
                short tmp = Converter.BytesToShort(dest[i+1], dest[i]);
                usCount++;
                sb.append(tmp);
                sb.append(' ');
                if(usCount == 17)
                {
                    LogEvent evt = new LogEvent();
                    evt.logName = "AirohaAirDump_" + timeStamp + ".log";
                    evt.logType = LogEvent.LOG_DUMP;
                    evt.logStr = sb.toString().trim() + "\n";
                    mStatusUiListener.OnActionCompleted(evt.logStr);
                    mlogger.addEventToQueue(evt);

                    sb.setLength(0);
                    usCount = 0;
                }
            }

        }
    };

    /**
     * Need to have the connected AirohaLink
     *
     * @param airohaLink
     */
    public AirohaAirDumpMgr(AirohaLink airohaLink, OnAirohaStatusUiListener listener) {
        super(airohaLink);
        mAirohaLink.registerOnRacePacketListener(TAG, mAirDumpPacketListener);
        mStatusUiListener = listener;
    }

    public void startAirDump() {

        renewStageQueue();

        MmiStage commit = new StageAirDump(this);
        ((StageAirDump) commit).payload[0] = 1;
        mStagesQueue.offer(commit);

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        mlogger = new AirDumpLogger("AirDump_" + timeStamp + ".log");
        mlogger.startLogger();

        //mStatusUiListener.OnActionCompleted("Tx: " + Converter.byte2HexStr(((StageAirDump) commit).getRaw()));

        startPollStagetQueue();
    }

    public void stopAirDump() {
        renewStageQueue();

        MmiStage commit = new StageAirDump(this);
        ((StageAirDump) commit).payload[0] = 0;
        mStagesQueue.offer(commit);

        //mStatusUiListener.OnActionCompleted("Tx: " + Converter.byte2HexStr(((StageAirDump) commit).getRaw()));

        startPollStagetQueue();

        mlogger.stop();
    }

}
