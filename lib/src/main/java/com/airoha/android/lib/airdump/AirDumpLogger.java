package com.airoha.android.lib.airdump;

import android.os.Environment;
import android.util.Log;

import com.airoha.android.lib.util.logger.LogEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Evonne.Hsieh on 2016/6/7.
 */
public class AirDumpLogger {

    String TAG = "AirDumpLogger";

    private File mFile = null;
    private FileOutputStream mFos = null;
    private BlockingQueue<LogEvent> mLogEventQueue;
    private LogThread mLogThread;
    private boolean mIsRunning;
    private String timeStamp;

    public AirDumpLogger(String filename)
    {
        mFile = new File(Environment.getExternalStorageDirectory(), filename);
        try {
            mFos = new FileOutputStream(mFile, true);
        } catch (IOException e) {
            Log.d("AirDumpLogger", "create FileOutputStream fail");
            e.printStackTrace();
        }
        createLogQueue();
    }

    private void createLogQueue() {

        mLogEventQueue = new LinkedBlockingQueue<>();
        mIsRunning = true;
        mLogThread = new LogThread();
    }

    public void startLogger()
    {
        Log.d(TAG, "startLogger");
        mIsRunning = true;
        if(mLogThread == null)
        {
            Log.d(TAG, "mLogThread is null");
            mLogThread = new LogThread();
        }
        mLogThread.start();
    }

    public void addEventToQueue(final LogEvent event) {
        if(mLogEventQueue == null)
        {
            Log.d(TAG, "mLogEventQueue null");
        }
        if (mLogEventQueue != null) {
            mLogEventQueue.add(event);
            Log.d(TAG, "mLogEventQueue add " + event.logName);
        }
    }

    class LogThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "LogThread");
            while(mIsRunning){
                if(mLogEventQueue!=null && mLogEventQueue.size() > 0){
                    LogEvent event = mLogEventQueue.poll();

                    if(event != null){
                        if(event.logType == LogEvent.LOG_DUMP)
                        {
                            logToFile(event.logStr);
                        }
                    }
                }
            }
        }
    }

    public synchronized void logToFile(String logString) {
        try {
            mFos.write(logString.getBytes());
            Log.d(TAG, "write log: " + logString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "FileNotFoundException: " + e.getMessage());
            stop();
        }  catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException 2: " + e.getMessage());
            stop();
        }
    }

    public void stop() {

        synchronized (mLogEventQueue) {
            if (mLogEventQueue != null) {
                mLogEventQueue.clear();
            }

            if(mLogThread != null) {
                mLogThread = null;
            }

            if(mFile != null) {
                mFile = null;
            }

            try {
                if (mFos != null) {
                    mFos.flush();
                    mFos.close();
                }
            } catch (IOException e) {
                Log.d("AirDumpLogger", "close FileOutputStream fail");
                e.printStackTrace();
            }

            mIsRunning = false;
        }
    }
}
