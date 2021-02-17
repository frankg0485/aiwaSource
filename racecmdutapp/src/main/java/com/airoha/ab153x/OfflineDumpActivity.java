package com.airoha.ab153x;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.mmi.OnAirohaStatusUiListener;
import com.airoha.android.lib.offlinedump.AirohaOfflineDumpMgr;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class OfflineDumpActivity extends AppCompatActivity {

    private static final String TAG = "OfflineDumpActivity";

    private AirohaLink mAirohaLink = null;

    private AirohaOfflineDumpMgr mAirohaOfflineDumpMgr = null;
    private Context mCtx;

    private Button mBtnGetDumpInfo;
    private Button mBtnStartDump;
    private Button mBtnAssert;
    private TextView mTxtDumpLog;

    // paired list
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private int LOG_MAXLENGTH = 0;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == AirohaRaceOtaMgr.DUMP_INFO){
                if(LOG_MAXLENGTH > 200)
                {
                    mTxtDumpLog.setText("");
                    LOG_MAXLENGTH = 0;
                }
                mTxtDumpLog.append(msg.obj.toString() + "\n");
                LOG_MAXLENGTH++;
                mBtnStartDump.setEnabled(true);
            }
            if(msg.what == AirohaRaceOtaMgr.DUMP_COMPLETE){
                Toast.makeText(mCtx, "Dump complete", Toast.LENGTH_SHORT).show();
                mBtnGetDumpInfo.setEnabled(true);
                mBtnAssert.setEnabled(true);
                mBtnStartDump.setEnabled(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offlinedump);
        this.setTitle("Offline Log");

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaOfflineDumpMgr = new AirohaOfflineDumpMgr(mAirohaLink, handler);

        initUImember();
        updatePairedList();

        final Intent intent = getIntent();
        if(intent != null){
            final String address = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAirohaLink.connect(address);
                }
            }).start();
        }
    }

    void initUImember() {
        mBtnGetDumpInfo = findViewById(R.id.buttonGetDumpInfo);
        mBtnStartDump = findViewById(R.id.buttonStartDump);
        mBtnAssert = findViewById(R.id.buttonAssert);
        mTxtDumpLog = findViewById(R.id.txtDumpLog);

        mBtnAssert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnStartDump.setEnabled(false);
                mBtnGetDumpInfo.setEnabled(false);
                mBtnAssert.setEnabled(false);
                mAirohaOfflineDumpMgr.makeAssert();
            }
        });

        mBtnStartDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnStartDump.setEnabled(false);
                mBtnGetDumpInfo.setEnabled(false);
                mBtnAssert.setEnabled(false);
                mAirohaOfflineDumpMgr.startDump();
            }
        });

        mBtnGetDumpInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTxtDumpLog.setText("");
                mAirohaOfflineDumpMgr.getDumpAddress();
            }
        });

        mBtnAssert.setEnabled(false);
        mBtnStartDump.setEnabled(false);
        mBtnGetDumpInfo.setEnabled(false);
    }

    private void startConnectThread(final String btaddr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final Boolean result = mAirohaLink.connect(btaddr);

                    ((Activity)mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(!result){

                            }
                        }
                    });

                }catch (final Exception e){

                    ((Activity)mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnAssert.setEnabled(true);
                    mBtnGetDumpInfo.setEnabled(true);
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnConnectionTimeout() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connection Timeout", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnAssert.setEnabled(false);
                    mBtnGetDumpInfo.setEnabled(false);
                    mBtnStartDump.setEnabled(false);
                    Toast.makeText(mCtx, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnDisConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnecting", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnUnexpectedDisconnected() {

        }
    };

    private void updatePairedList() {
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        // Remove all element from the list
        mPairedDevicesArrayAdapter.clear();
        // Get a set of currently paired devices
        BluetoothAdapter mBlurAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Log.e("DeviceActivity ",
                    "Device not founds");
            mPairedDevicesArrayAdapter.add("No Device");
            return;
        }

        for (BluetoothDevice device : pairedDevices) {
            mAirohaLink.logToFile("DeviceActivity", "Device : address : " + device.getAddress() + " name :"
                    + device.getName());

            ParcelUuid[] parcelUuids = device.getUuids();

            for (ParcelUuid uuid : parcelUuids) {
                mAirohaLink.logToFile(TAG, device.getName() + "uuid:" + uuid.toString());
            }

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }

        try {
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];

            ParcelUuid[] parcelUuids = lastdevice.getUuids();

            for (ParcelUuid parcelUuid : parcelUuids) {
                mAirohaLink.logToFile(TAG, parcelUuid.toString());

                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                    mAirohaLink.logToFile(TAG, "found Airoha device");

                    Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_LONG).show();

                    startConnectThread(lastdevice.getAddress());
                    return;
                }
            }
        } catch (Exception e) {
            mAirohaLink.logToFile(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAirohaLink.disconnect();
    }
}
