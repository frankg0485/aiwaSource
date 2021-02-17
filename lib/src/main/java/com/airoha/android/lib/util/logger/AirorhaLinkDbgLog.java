package com.airoha.android.lib.util.logger;

import android.os.Environment;
import android.util.Log;

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
public class AirorhaLinkDbgLog {

    private String mDeviceName;

    private File mFile = null;

    private BlockingQueue<String> mLogStringQueue;

    private LogThread mLogThread;

    private boolean mIsRunning;

    private boolean mIsOutputToLogi = true;
    private boolean mIsOutputToFile = true;

    public AirorhaLinkDbgLog(String deviceName, boolean isOutputToLogi, boolean isOutputToFile) {
        mDeviceName = deviceName;

        mIsOutputToLogi = isOutputToLogi;
        mIsOutputToFile = isOutputToFile;

        if(mDeviceName == null){
            return;
        }

        if(!isOutputToFile)
            return;

        mFile = new File(Environment.getExternalStorageDirectory(), mDeviceName + "AirohaLink.txt");
        try {
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            mFile = null;
            Log.e("AirorhaLinkDbgLog", e.getMessage());
        }

        if(mFile == null){
            return;
        }

        mLogStringQueue = new LinkedBlockingQueue<>();
        mIsRunning = true;

        mLogThread = new LogThread();
        mLogThread.start();
    }

    public void outputLogI(String tag, String logContent){
        if(mIsOutputToLogi) {
            Log.i(tag, logContent);
        }
    }

    public void addStringToQueue(final String tag, final String logContent) {
        if(mFile == null){
            return;
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (mLogStringQueue) {
//                    if (mLogStringQueue != null) {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

                        String logString = timeStamp + "--" + tag + ":" + logContent + "\n";

                        mLogStringQueue.add(logString);
//                    }
//                }
//            }
//        }).start();
    }

    class LogThread extends Thread {
        @Override
        public void run() {
            while(mIsRunning){
//                synchronized (mLogStringQueue){

                    while(mLogStringQueue.size() > 0){
                        String logStr = mLogStringQueue.poll();

                        if(logStr != null){
                            logToFile(logStr);
                        }
                    }
//                }
            }
        }
    }

    public synchronized void logToFile(String logString) {
        if(mFile == null){
            return;
        }

        FileOutputStream mFos = null;
        try {
            mFos = new FileOutputStream(mFile, true);

            mFos.write(logString.getBytes());
            mFos.flush();
            mFos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFos != null) {
                    mFos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public synchronized void logToFile(String tag, String logContent)
    {
        if(mFile == null){
            return;
        }

        FileOutputStream mFos = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            mFos = new FileOutputStream(mFile, true);
            mFos.write((timeStamp + "--").getBytes());
            mFos.write((tag+": ").getBytes());
            mFos.write(logContent.getBytes());
            mFos.write("\n".getBytes());
            mFos.flush();
            mFos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFos != null) {
                    mFos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {

        if(mFile == null){
            return;
        }
//        synchronized (mLogStringQueue) {
//            if (mLogStringQueue != null) {
//                mLogStringQueue.clear();
//            }
//        }

        mIsRunning = false;
        try {
            mLogThread.join(5000);
        } catch (Exception e) {
            Log.e("AirohaLogger", e.getMessage());
        }
    }
}
