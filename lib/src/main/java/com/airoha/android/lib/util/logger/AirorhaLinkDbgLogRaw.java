package com.airoha.android.lib.util.logger;

import android.os.Environment;
import android.util.Log;

import com.airoha.android.lib.airdump.AirDumpLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class AirorhaLinkDbgLogRaw {

    private static String TAG = "AirorhaLinkDbgLogRaw";
    private File mPureRawFile = null;
    private FileOutputStream mFos = null;

    private BlockingQueue<byte[]> mLogRawQueue;

    private LogThread mLogThread;

    private boolean mIsRunning;

    public AirorhaLinkDbgLogRaw(String filename) {

        mPureRawFile = new File(Environment.getExternalStorageDirectory(), filename);

        try {
            if (!mPureRawFile.exists()) {
                mPureRawFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mFos = new FileOutputStream(mPureRawFile, true);
        } catch (IOException e) {
            Log.d("AirDumpLogger", "create FileOutputStream fail");
            e.printStackTrace();
        }

        mLogRawQueue = new LinkedBlockingQueue<>();
    }

    public void startLogger()
    {
        mIsRunning = true;

        mLogThread = new LogThread();
        mLogThread.start();
    }

    public synchronized void addRawBytesToQueue(byte[] rawBytes) {
        if(mLogRawQueue!= null){

            mLogRawQueue.add(rawBytes);
        }
    }

    class LogThread extends Thread {
        @Override
        public void run() {
            while(mIsRunning){
                synchronized (AirorhaLinkDbgLogRaw.this){
                    if(mLogRawQueue!=null && mLogRawQueue.size() > 0){
                        byte[] raw = mLogRawQueue.poll();

                        if(raw != null){
                            logToFile(raw);
                        }
                    }
                }
            }
        }
    }

    public synchronized void logToFile(byte[] raw) {
        if(mPureRawFile == null){
            return;
        }

        try {
            mFos.write(raw);
        }  catch (FileNotFoundException e) {
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
        synchronized (mLogRawQueue) {
            if (mLogRawQueue != null) {
                mLogRawQueue.clear();
            }

            if(mLogThread != null) {
                mLogThread = null;
            }

            if(mPureRawFile != null) {
                mPureRawFile = null;
            }

            try {
                if (mFos != null) {
                    mFos.flush();
                    mFos.close();
                }
            } catch (IOException e) {
                Log.d("OnlineDumpLogger", "close FileOutputStream fail");
                e.printStackTrace();
            }

            mIsRunning = false;
        }
    }
}
