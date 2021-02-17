package com.airoha.android.lib.transport;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.airoha.android.lib.BuildConfig;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.physical.IPhysical;
import com.airoha.android.lib.physical.ble.AirohaBleController;
import com.airoha.android.lib.physical.spp.AirohaSppController;
import com.airoha.android.lib.physical.spp.AirohaSppControllerCh2;
import com.airoha.android.lib.physical.spp.AirohaSppControllerCh3;
import com.airoha.android.lib.transport.Commander.QueuedCmdsCommander;
import com.airoha.android.lib.transport.PacketParser.OnAirohaRespTimeoutListener;
import com.airoha.android.lib.transport.PacketParser.OnRaceMmiPacketListener;
import com.airoha.android.lib.transport.PacketParser.OnRaceMmiRoleSwitchIndListener;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.transport.PacketParser.RacePacketByH4Dispatcher;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLog;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLogRaw;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.airoha.android.lib.transport.PacketParser.RacePacketByH4Dispatcher.RACE_BY_H4_START;

/**
 * A class to handle SPP connect/disconnect, read/write.
 * <p>
 * Don't forget to register the callback {@link OnAirohaConnStateListener} and {@link OnAirohaRespTimeoutListener}.
 * */
public class AirohaLink implements ITransport {

    public static final UUID UUID_AIROHA_SPP = UUID.fromString("00000000-0000-0000-0099-AABBCCDDEEFF");
    public final static UUID UUID_AIROHA1520_CH2 = UUID.fromString("81C2E72A-0591-443E-A1FF-05F988593351");
    public final static UUID UUID_AIROHA1520_CH3 = UUID.fromString("F8D1FBE4-7966-4334-8024-FF96C9330E15");

    private static final String TAG = "AirohaLink";

    private final Context mCtx;
    private boolean mIsConnected = false;

    private BluetoothManager mBluetoothManager;
    /* Get Default Adapter */
    private BluetoothAdapter mBluetoothAdapter = null;

    private QueuedCmdsCommander mQueuedCmdsCommander;

    private ConcurrentHashMap<String, OnAirohaConnStateListener> mConnStateListenerMap = null;

    private ConcurrentHashMap<String, OnAirohaRespTimeoutListener> mRespTimoutListenerMap = null;

    private IPhysical mPhysical;

    private RacePacketByH4Dispatcher mRacePacketByH4Dispatcher;

    private AirorhaLinkDbgLog mLogger;

    private AirorhaLinkDbgLogRaw mLoggerRaw;

    private int mTimeoutRaceCmdNotResp = 6000;

    private Timer mTimerForCmdResp;

    private String mAddressReconnect;

    private final Object mLockConnectApi = new Object();
    private final Object mLockDisconnectApi = new Object();

    private int MAX_RECONNECT_RETRY = 20;
    private boolean mIsActiveDisconnectTriggered = false;
    private Timer mTimerForCheckProfile;
    private static final int TIME_CHECK_PROFILE = 3000;// 3-sec

    private static final int MAX_CONNECTION_ERROR = 5;
    private int mConnectionErrorCounter = 0;
    private volatile Timer mTimerForConnectionError;

    class CmdTimeoutTask extends TimerTask {

        @Override
        public void run() {
            // raise callback to client

            logToFile(TAG, "CMD_NEED_RESP(0x5A) send but not responded. Timeout!!!");
            for (OnAirohaRespTimeoutListener listener : mRespTimoutListenerMap.values()) {
                listener.OnRespTimeout();
            }

            mTimerForCmdResp.cancel();
            mTimerForCmdResp = null;
        }
    }

    class CheckProfileTask extends TimerTask {

        @Override
        public void run() {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                return;
            }

            logToFile(TAG, "mConnectionErrorCounter = " + mConnectionErrorCounter);

            logToFile(TAG, "MAX_CONNECTION_ERROR = " + MAX_CONNECTION_ERROR);

            if (mConnectionErrorCounter >= MAX_CONNECTION_ERROR) {
                logToFile(TAG, "Connection Timeout!!");
                mTimerForCheckProfile.cancel();
                mTimerForCheckProfile = null;
                for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
                    if (listener != null) {
                        listener.OnConnectionTimeout();
                    }
                }
            }

            logToFile(TAG, "checking profile");

            int state = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);

            logToFile(TAG, "profile state: " + state);

            if (state == BluetoothProfile.STATE_CONNECTED) {
                SystemClock.sleep(1000); // BTA-3202, don't connect to SPP right after A2DP

                mIsConnected = reConnect();

                logToFile(TAG, "mIsConnected: " + mIsConnected);

                if (mIsConnected) {
                    mTimerForCheckProfile.cancel();
                    mTimerForCheckProfile = null;
                    startUnexpectedDisconnectTimer();
                } else {
                    ++mConnectionErrorCounter;
                }
            }
        }
    }

    /**
     * Constructor
     *
     * @param ctx Application Context
     */
    public AirohaLink(Context ctx) {
        mCtx = ctx;

        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mCtx.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

        mQueuedCmdsCommander = new QueuedCmdsCommander();

        mRacePacketByH4Dispatcher = new RacePacketByH4Dispatcher(this);

        mConnStateListenerMap = new ConcurrentHashMap<>();

        mRespTimoutListenerMap = new ConcurrentHashMap<>();
    }

    /**
     * Connect SPP or GATT by the target device type
     *
     * @param address BT Addr
     * @return true: Success, false: fail
     * @see OnAirohaConnStateListener
     */
    public boolean connect(String address) throws IllegalArgumentException {
        logToFile(TAG, "connect()");
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }

        // 2018.08.22 Daniel: add some protection
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        if (mIsConnected)
            return true;

        synchronized (mLockConnectApi) {
            mAddressReconnect = address;

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            String deviceName = device.getName();

//        if (BuildConfig.DEBUG) {
            mLogger = new AirorhaLinkDbgLog(deviceName, true, true);
//        }
            logToFile(TAG, "Ver:" + BuildConfig.VERSION_NAME);
            logToFile(TAG, "prepare connect");

            mQueuedCmdsCommander.clearQueue();

            int deviceType = device.getType();

            logToFile(TAG, "device type:" + deviceType);

            if (mPhysical != null) {
                mPhysical.disconnect();
                mPhysical = null;
                mIsConnected = false;
            }

            if (deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC || deviceType == BluetoothDevice.DEVICE_TYPE_DUAL) {
                mPhysical = new AirohaSppController(this);
            }

            if (deviceType == BluetoothDevice.DEVICE_TYPE_LE) {
                mPhysical = new AirohaBleController(this);
            }

            if (mPhysical == null) {
                throw new IllegalArgumentException(address + "device type: " + deviceType + " can't be connected");
            }

            for (OnAirohaConnStateListener connStateListener : mConnStateListenerMap.values()) {
                if (connStateListener != null) {
                    connStateListener.OnConnecting();
                }
            }

            return mPhysical.connect(address);
        }
    }

    /**
     * Connect Spp
     *
     * @param address BT Addr
     * @return true: Success, false: fail
     * @see OnAirohaConnStateListener
     */
    public boolean connectSpp(String address) throws IllegalArgumentException {
        logToFile(TAG, "connectSpp");

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }

        mQueuedCmdsCommander.clearQueue();

        mPhysical = new AirohaSppController(this);

        return mPhysical.connect(address);
    }

    /**
     * Connect GATT
     *
     * @param address BT Addr
     * @return true: Success, false: fail
     * @see OnAirohaConnStateListener
     */
    public boolean connectBle(String address) {
        logToFile(TAG, "connectBle");

        mQueuedCmdsCommander.clearQueue();

        mPhysical = new AirohaBleController(this);

        return mPhysical.connect(address);
    }

    public boolean reConnect() throws IllegalArgumentException {
        logToFile(TAG, "reConnect()");
        return connect(mAddressReconnect);
    }

    /**
     * Multiple SPP connection. (for debugging)
     *
     * @param address BT Addr
     * @param channel SPP connection index
     * @return true: Success, false: fail
     * @see OnAirohaConnStateListener
     */
    public boolean connect(String address, int channel) throws IllegalArgumentException {
        logToFile(TAG, "connect()");
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }

        mAddressReconnect = address;

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        String deviceName = device.getName();

//        if (BuildConfig.DEBUG) {
//        mLogger = new AirorhaLinkDbgLog(deviceName);
//        }

        mLoggerRaw = new AirorhaLinkDbgLogRaw(deviceName);

        mQueuedCmdsCommander.clearQueue();

        int deviceType = device.getType();

        logToFile(TAG, "device type:" + deviceType);

        if (mPhysical != null) {
            mPhysical.disconnect();
            mPhysical = null;
            mIsConnected = false;
        }

        switch (channel) {
            case 2:
                mPhysical = new AirohaSppControllerCh2(this);
                break;
            case 3:
                mPhysical = new AirohaSppControllerCh3(this);
                break;
        }


        return (mPhysical).connect(address);
    }

    /**
     * disconnect from SPP
     *
     * @see OnAirohaConnStateListener
     */
    public void disconnect() {
        logToFile(TAG, "disconnect()");
        synchronized (mLockDisconnectApi) {
            mIsActiveDisconnectTriggered = true;

            if (mTimerForCmdResp != null) {
                mTimerForCmdResp.cancel();
                mTimerForCmdResp = null;
            }

            if (mTimerForConnectionError != null) {
                mTimerForConnectionError.cancel();
                mTimerForConnectionError = null;
            }

            if (mTimerForCheckProfile != null) {
                mTimerForCheckProfile.cancel();
                mTimerForCheckProfile = null;
            }

            if (mPhysical != null) {

                for (OnAirohaConnStateListener connStateListener : mConnStateListenerMap.values()) {
                    if (connStateListener != null) {
                        connStateListener.OnDisConnecting();
                    }
                }

                mPhysical.disconnect();

                logToFile(TAG, "mPhysical.disconnect");

                mPhysical = null;
            }

            if (mQueuedCmdsCommander != null) {
                mQueuedCmdsCommander.clearQueue();
                mQueuedCmdsCommander.isResponded = true;
            }

            mIsConnected = false;

            if (mLogger != null) {
                mLogger.stop();
                mLogger = null;
            }
        }
    }

    /**
     * get the connection state of SPP
     *
     * @return true: connected, false: disconnected
     */
    public boolean isConnected() {
        return this.mIsConnected;
    }

    /**
     * for extended feature
     *
     * @param cmd
     * @return
     */
    public boolean sendCommand(byte[] cmd) {
        logToFile(TAG, "Tx packet: " + Converter.byte2HexStr(cmd));

        if (cmd[0] == RACE_BY_H4_START && cmd[1] == RaceType.CMD_NEED_RESP) {
            // start timeout timer
            logToFile(TAG, "Cmd needs Resp start count down");

            if (mTimerForCmdResp != null) {
                mTimerForCmdResp.cancel();
            }

            mTimerForCmdResp = new Timer();
            mTimerForCmdResp.schedule(new CmdTimeoutTask(), mTimeoutRaceCmdNotResp);
        }

        return mPhysical.write(cmd);
    }

    /**
     * MMI api should use this, Queue mechanism is invoked
     *
     * @param cmd
     */
    public synchronized void sendOrEnqueue(byte[] cmd) {

        if (mQueuedCmdsCommander.isQueueEmpty() && mQueuedCmdsCommander.isResponded) {
            logToFile(TAG, "soe: cmd send");
            sendCommand(cmd);
            mQueuedCmdsCommander.isResponded = false;
        } else {
            logToFile(TAG, "soe: cmd enqueue " + Converter.byte2HexStr(cmd));
            mQueuedCmdsCommander.enqueneCmd(cmd);
        }
    }

    /**
     * check MMI API send messages
     */
    private synchronized void checkQueuedActions() {

        logToFile(TAG, "checkQueuedActions set responded");
        mQueuedCmdsCommander.isResponded = true;

        byte[] nextCmd = mQueuedCmdsCommander.getNextCmd();

        if (nextCmd != null)
            sendCommand(nextCmd);
    }


    /**
     * Need to set a listener implementing  {@link OnAirohaConnStateListener} interface for handling SPP/BLE state changes
     *
     * @param subscriberName arbitrary name for the subscriber
     * @param listener
     */
    public void registerOnConnStateListener(String subscriberName, OnAirohaConnStateListener listener) {
        mConnStateListenerMap.put(subscriberName, listener);
    }

    public void unregisterOnConnStateListener(String subscriberName) {
        mConnStateListenerMap.remove(subscriberName);
    }

    public void registerOnRespTimeoutListener(String subsriberName, OnAirohaRespTimeoutListener listener) {
        mRespTimoutListenerMap.put(subsriberName, listener);
    }

    /**
     * set the timeout of receiving response
     *
     * @param timeoutMs timeout value in milliseconds
     */
    public void setResponseTimeout(int timeoutMs) {
        if (timeoutMs > 0) {
            mTimeoutRaceCmdNotResp = timeoutMs;
        }
    }

    /**
     * get SDK version name
     *
     * @return SDK version name
     */
    public String getSdkVer() {
        return BuildConfig.VERSION_NAME;
    }


    /**
     * Share the context to other Airoha modules
     *
     * @return Context of AirohaLink is related
     */
    @Override
    public Context getContext() {
        return mCtx;
    }

    public Object doEngineeringCmd(String cmd, Object param) {
        if (cmd.equalsIgnoreCase("SPP_SERVER_START_LISTEN")) {

            mLogger = new AirorhaLinkDbgLog("SPP_Server", true, true);

            if (mPhysical != null) {
                mPhysical.disconnect();
                mPhysical = null;
                mIsConnected = false;
            }

            mPhysical = new AirohaSppController(this);

            if (mPhysical == null) {
                throw new IllegalArgumentException("Failed to init AirohaSppController");
            }

            return ((AirohaSppController) mPhysical).listen();
        }
        return this.mIsConnected;
    }

    @Override
    public void handlePhysicalPacket(byte[] packet) {
        logToFile(TAG, "handlePhysicalPacket Rx packet: " + Converter.byte2HexStr(packet));

        logRawToBin(packet);


        // check for RACE_BY_H4
        if (RacePacketByH4Dispatcher.isRackeByH4Collected(packet)) {
            // stop timeout timer

            if (mTimerForCmdResp != null) {
                mTimerForCmdResp.cancel();
            }

            mRacePacketByH4Dispatcher.parseSend(packet);

            if (RacePacketByH4Dispatcher.isRaceResponse(packet)) {
                checkQueuedActions();
            }
        }
    }

    @Override
    public void OnPhysicalConnected(String type) {
        mIsConnected = true;

        mIsActiveDisconnectTriggered = false;

        if (mTimerForCheckProfile != null)
            mTimerForCheckProfile.cancel();

        logToFile(TAG, "physical connected");

        for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
            if (listener != null) {
                listener.OnConnected(type);
            }
        }
    }

    @Override
    public void OnPhysicalDisconnected(String type) {
        mIsConnected = false;

        logToFile(TAG, "physical disconnected");

        for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
            if (listener != null) {
                listener.OnDisconnected();
            }
        }

        if (!mIsActiveDisconnectTriggered) {
            if (mTimerForCheckProfile != null)
                mTimerForCheckProfile.cancel();

            logToFile(TAG, "non active disconnect");

            if (mTimerForConnectionError != null) {
                logToFile(TAG, "mConnectionErrorCounter = " + mConnectionErrorCounter);
                ++mConnectionErrorCounter;
            }

            if (mConnectionErrorCounter < MAX_CONNECTION_ERROR) {
                mTimerForCheckProfile = new Timer();
                mTimerForCheckProfile.schedule(new CheckProfileTask(), TIME_CHECK_PROFILE, TIME_CHECK_PROFILE);
            } else {
                for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
                    if (listener != null) {
                        listener.OnUnexpectedDisconnected();
                    }
                }
            }
        }

    }

    @Override
    public void logToFile(String tag, String content) {

        if (mLogger == null)
            return;

//        mLogger.logToFile(tag, content);

        mLogger.outputLogI(tag, content);
        mLogger.addStringToQueue(tag, content);
    }

    private void logRawToBin(byte[] raw) {
        if (mLoggerRaw == null)
            return;

        mLoggerRaw.addRawBytesToQueue(raw);
    }

    public void registerOnRacePacketListener(String subscriber, OnRacePacketListener listener) {
        mRacePacketByH4Dispatcher.registerRacePacketListener(subscriber, listener);
    }

    public void registerOnRaceMmiPacketListener(String subscriber, OnRaceMmiPacketListener listener) {
        mRacePacketByH4Dispatcher.registerRaceMmiPacketListener(subscriber, listener);
    }

    public void registerOnRaceMmiRoleSwitchIndListener(String subscriber, OnRaceMmiRoleSwitchIndListener listener) {
        mRacePacketByH4Dispatcher.registerRaceMmiRoleSwitchIndLisener(subscriber, listener);
    }

    void startUnexpectedDisconnectTimer() {
        logToFile(TAG, "startUnexpectedDisconnectTimer");

        if (mTimerForConnectionError != null) {
            mTimerForConnectionError.cancel();
            mTimerForConnectionError = null;
        }
        mTimerForConnectionError = new Timer(true);
        mTimerForConnectionError.schedule(new TimerTask() {
            @Override
            public void run() {
                mConnectionErrorCounter = 0;
                mTimerForConnectionError.cancel();
                mTimerForConnectionError = null;
            }
        }, TIME_CHECK_PROFILE * 2);
    }

    @Override
    public void stopTimerForCheckProfile() {
        logToFile(TAG, "stopTimerForCheckProfile");
        if (null != mTimerForCheckProfile) {
            mTimerForCheckProfile.cancel();
            mTimerForCheckProfile = null;
        }
    }

}
