package com.airoha.ab153x;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.airdump.AirohaAirDumpMgr;
import com.airoha.android.lib.mmi.OnAirohaStatusUiListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class AirDumpActivity extends AppCompatActivity {

    private static final String TAG = "AirohaAirDump";

    private AirohaLink mAirohaLink = null;

    private AirohaAirDumpMgr mAirohaAirDumpMgr = null;
    private Context mCtx;

    private Button mBtnStartAirDump;
    private Button mBtnStopAirDump;
    private TextView mTxtDumpLog;
    private TextView mTextViewSppAddr;

    // paired list
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private int LOG_MAXLENGTH = 0;
    private OnAirohaStatusUiListener mOnUiListener = new OnAirohaStatusUiListener() {
        @Override
        public void OnActionCompleted(final String log) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(LOG_MAXLENGTH > 200)
                    {
                        mTxtDumpLog.setText("");
                        LOG_MAXLENGTH = 0;
                    }
                    mTxtDumpLog.append(log + "\n");
                    LOG_MAXLENGTH++;
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airdump);
        this.setTitle("AirDump");
        Log.d(TAG, "onCreate");
        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);

        initUImember();
        //updatePairedList();

        mAirohaAirDumpMgr = new AirohaAirDumpMgr(mAirohaLink, mOnUiListener);

        final Intent intent = getIntent();
        if(intent != null){
            final String address = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

            mTextViewSppAddr.setText(address);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAirohaLink.connect(address);
                }
            }).start();
        }
    }

    void initUImember() {
        Log.d(TAG, "initUImember");
        mBtnStartAirDump = findViewById(R.id.buttonStartAirDump);
        mBtnStopAirDump = findViewById(R.id.buttonStopAirDump);
        mTxtDumpLog = findViewById(R.id.txtDumpLog);
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);

        mBtnStartAirDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaAirDumpMgr.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                mAirohaAirDumpMgr.startAirDump();
                mTxtDumpLog.setText("");
                mBtnStopAirDump.setEnabled(true);
                mBtnStartAirDump.setEnabled(false);
            }
        });

        mBtnStopAirDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaAirDumpMgr.stopAirDump();
                mBtnStartAirDump.setEnabled(true);
                mBtnStopAirDump.setEnabled(false);
            }
        });

        mBtnStopAirDump.setEnabled(false);
        mBtnStartAirDump.setEnabled(false);

    }

    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();

        super.onDestroy();
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
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mBtnStartAirDump.setEnabled(true);
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
                    Toast.makeText(mCtx, "Disconnected", Toast.LENGTH_SHORT).show();
                    mBtnStartAirDump.setEnabled(false);
                    mBtnStopAirDump.setEnabled(false);
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
            Log.d("DeviceActivity", "Device : address : " + device.getAddress() + " name :"
                    + device.getName());

            ParcelUuid[] parcelUuids = device.getUuids();

            for (ParcelUuid uuid : parcelUuids) {
                mAirohaLink.logToFile(TAG, device.getName() + "uuid:" + uuid.toString());
                Log.d(TAG, device.getName() + "uuid:" + uuid.toString());
            }

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }

        try {
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];

            ParcelUuid[] parcelUuids = lastdevice.getUuids();

            for (ParcelUuid parcelUuid : parcelUuids) {
                Log.d(TAG, parcelUuid.toString());
                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                    Log.d(TAG, "found Airoha device");
                    Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_LONG).show();
                    startConnectThread(lastdevice.getAddress());
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
