package com.airoha.android.lib.physical.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.physical.IPhysical;
import com.airoha.android.lib.physical.PhysicalType;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.ITransport;
import com.airoha.android.lib.util.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by MTK60279 on 2017/11/30.
 */

public class AirohaSppController implements IPhysical {
    private static final String TAG = "AirohaSppController";

    private static final byte SPP_EVENT_START = 0x04;
    private static final byte ACL_VCMD_START = 0x02;
    private static final byte RACE_BY_H4 = 0x05;
    private static final int HEADER_SIZE = 3;

    private BluetoothManager mBluetoothManager;
    /* Get Default Adapter */
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket mbsSocket = null;
    private final Object mLock = new Object();

    private BluetoothServerSocket mServerSocket = null;

    protected InputStream mInStream = null;
    protected OutputStream mOutStream = null;

    private Context mCtx;
    private boolean mIsConnected = false;

    private ConnectedThread mConnectedThread;

    protected ITransport mAirohaLink;

    public AirohaSppController(AirohaLink airohaLink){

        mAirohaLink = airohaLink;

        mCtx = mAirohaLink.getContext();

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
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
    }

    protected UUID getConnUUID() {
        return ((AirohaLink)mAirohaLink).UUID_AIROHA_SPP;
    }

    public boolean listen() {
        mAirohaLink.logToFile(TAG, "start socket server listen...");

        this.disconnect();

        if (!mBluetoothAdapter.isEnabled())
            return false;

        try {
            mServerSocket = listenRfcomm("SPP_Server");
            mbsSocket = mServerSocket.accept();
            /// Note that: the BluetoothSocket will Not be closed when closes the BluetoothServerSocket.
            mOutStream = mbsSocket.getOutputStream();
            mInStream = mbsSocket.getInputStream();
            mIsConnected = true;

            mAirohaLink.logToFile(TAG, "mIsConnectOK true");

            startConnectedThread();

        } catch (IOException e) {
            mAirohaLink.logToFile(TAG, "IOException" + e.getMessage());
            this.disconnect();
            return false;
        } catch (Exception e) {
            mAirohaLink.logToFile(TAG, "Exception" + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean connect(String address) {
        mAirohaLink.logToFile(TAG, "createConn");

        if(mIsConnected){
            return true;
            //this.disconnect();
        }

        if (!mBluetoothAdapter.isEnabled())
            return false;

        try {

            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
                    .getRemoteDevice(address);

//            // 2018.09.05 TODO verifying
//            mAirohaLink.logToFile(TAG, "call fetchUuidsWithSdp");
//            device.fetchUuidsWithSdp();

            mbsSocket = createRfcomm(device);

            this.mbsSocket.connect();
            this.mOutStream = this.mbsSocket.getOutputStream();
            this.mInStream = this.mbsSocket.getInputStream();
            this.mIsConnected = true;

            mAirohaLink.logToFile(TAG, "mIsConnectOK true");


            startConnectedThread();

        } catch (IOException e) {
            mAirohaLink.logToFile(TAG, "IOException" + e.getMessage());
            this.disconnect();
            return false;
        } catch (Exception e) {
            mAirohaLink.logToFile(TAG, "Exception" + e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public void disconnect() {
        mAirohaLink.logToFile(TAG, "disconnect()");

        synchronized (mLock) {
            try {
                // 2016.5.3 Daniel commented
                stopConnectedThread();

                if (null != this.mInStream) {
                    this.mInStream.close();
                    this.mInStream = null;
                }

                if (null != this.mOutStream) {
                    this.mOutStream.close();
                    this.mOutStream = null;
                }

                if (null != this.mbsSocket) {
                    mAirohaLink.logToFile(TAG, "BluetoothSocket closing");
                    this.mbsSocket.close();
                    mAirohaLink.logToFile(TAG, "BluetoothSocket closed");
                    this.mbsSocket = null;

                    // 2018.08.07
                    notifyDisconnected();
                }

                if (null != mServerSocket) {
                    mServerSocket.close();
                    mServerSocket = null;

                    // 2018.08.07
                    notifyDisconnected();
                }

                this.mIsConnected = false;
                mAirohaLink.logToFile(TAG, "mIsConnectOK false, normal");
            } catch (IOException e) {
                mAirohaLink.logToFile(TAG, "IOException" + e.getMessage());

                this.mInStream = null;
                this.mOutStream = null;
                this.mbsSocket = null;
                this.mIsConnected = false;
                mAirohaLink.logToFile(TAG, "mIsConnectOK false, exception");
            }
        }
    }

    @Override
    public boolean write(byte[] cmd) {
        synchronized (mLock) {
            if (this.mIsConnected) {
                try {
//                mAirohaLink.logToFile(TAG, "write:" + Converter.byte2HexStr(cmd));
                    mOutStream.write(cmd);
                    mOutStream.flush();
                    return true;
                } catch (IOException e) {
                    mAirohaLink.logToFile(TAG, "IOException" + e.getMessage());

                    this.disconnect();
//                notifyDisconnected();

                    return false;
                }
            } else
                return false;
        }
    }



    @Override
    public void notifyConnected() {
        mAirohaLink.OnPhysicalConnected(typeName());
    }

    @Override
    public void notifyDisconnected() {
        mAirohaLink.OnPhysicalDisconnected(typeName());
    }

    @Override
    public String typeName() {
        return PhysicalType.SPP.toString();
    }


    private static UUID getUuidFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    private BluetoothSocket createRfcomm(BluetoothDevice device) {
        final UUID uuid1520 = getConnUUID(); // 2016.1.14 Daniel, this makes the app only for 1520

        mAirohaLink.logToFile(TAG, "createRfcomm: " + uuid1520.toString());

        BluetoothSocket bluetoothSocket = null;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid1520);
        } catch (IOException e) {
            return null;
        }
        return bluetoothSocket;
    }

    private BluetoothServerSocket listenRfcomm(String name) {
        final UUID uuid1520 = getConnUUID(); // 2016.1.14 Daniel, this makes the app only for 1520

        BluetoothServerSocket bluetoothServerSocket = null;
        try {
            bluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid1520);
        } catch (IOException e) {
            return null;
        }
        return bluetoothServerSocket;
    }

    /**
     * Thread to connectSpp the Bluetooth socket and start the thread that reads from the socket.
     */
    private class ConnectedThread extends Thread {

        private final RespIndPacketBuffer mmRespIndCmr;

        private final Context mCtx;

        private boolean mmIsRunning = false;

        public ConnectedThread(Context ctx) {
            mCtx = ctx;
            mmRespIndCmr = new RespIndPacketBuffer();
            mmIsRunning = true;
        }

        public void run() {
            mAirohaLink.logToFile(TAG, "ConnectedThread running");
            notifyConnected();

            while (mmIsRunning) {
                try {
                    handleInputStream(mmRespIndCmr);


                } catch (IOException ioe) {
                    if (mmIsRunning) {
                        mAirohaLink.logToFile(TAG, "ConnectedT io exec: " + ioe.getMessage());
                        // 2016.08.18 Daniel: Mantis#7882, on Nexus 5X, not sending ACL_DISCONNECTED to upper layer, this is a workaround
                        // 2017.04.07 Daniel: remove above, use notifyDisconnected
                    } else {
                        mAirohaLink.logToFile(TAG, "ConnectedT io exec: " + ioe.getMessage() + "--by user");
                    }

                    disconnect();
//                    mAirohaLink.stopTimerForCheckProfile();

                    return;
                } catch (IndexOutOfBoundsException ioobe) {
//                    mAirohaLink.logToFile(TAG, "Connected thread ioobe");
                    ioobe.printStackTrace();
                } catch (Exception e) {
                    //mAirohaLink.logToFile(TAG, "Connected thread Except: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }


        public void cancel() {
            mmIsRunning = false;

            mAirohaLink.logToFile(TAG, "ConnectedThread cancel");
        }
    }

    protected void handleInputStream(RespIndPacketBuffer mmRespIndCmr) throws IOException {
        // read byte
        byte[] completePacket = new byte[1200];
        byte[] bAryTmp = new byte[1200];
        byte b = (byte) mInStream.read(); // get command header type 0x02 or 0x04

        int completePacketLength = 0;

        if(b == RACE_BY_H4) {
            completePacket[0] = b;
            completePacket[1] = (byte) mInStream.read(); // type: could be 0x5B, 0x5D
            completePacket[2] = (byte) mInStream.read(); // length[0]
            completePacket[3] = (byte) mInStream.read(); // length[1]

            int leng = Converter.BytesToU16(completePacket[3], completePacket[2]);

            mInStream.read(bAryTmp, 0, leng);

            System.arraycopy(bAryTmp, 0, completePacket, 4, leng);

            completePacketLength = 4 + leng;
        }

        mmRespIndCmr.addArrayToPacket(completePacket, completePacketLength);

        byte[] packet = mmRespIndCmr.getPacket();
        mmRespIndCmr.resetPacket();

        mAirohaLink.handlePhysicalPacket(packet);
    }

    private void startConnectedThread() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(mCtx);
        mConnectedThread.start();
    }

    private void stopConnectedThread() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
}
