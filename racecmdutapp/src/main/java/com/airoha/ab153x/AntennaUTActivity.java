package com.airoha.ab153x;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.AntennaUT.AntennaUtLogUiListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.text.DecimalFormat;
import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class AntennaUTActivity extends AppCompatActivity {

    private static final String TAG = "AirohaAntennaUT";
    private Context mCtx;

    // Connect
    protected EditText mEditSppAddr;
    protected Button mBtnConSpp;
    protected Button mBtnDisConSpp;
    protected TextView mTextConSppResult;
    protected TextView mTextConSppState;
    protected Button mBtnStartAntennaUT;
    protected Button mBtnStopAntennaUT;
    protected Spinner mSpinnerRptTime;
    protected Spinner mSpinnerTestRole;
    protected CheckBox mCbEnableStatistics;
    protected EditText mEditStatisticsCount;
    private TextView mTextViewSppAddr;

    // paired list
    protected ListView mPairedListView;
    protected ArrayAdapter<String> mPairedDevicesArrayAdapter;

    //log list
    protected ListView mLogAgentView;
    protected ListView mLogPartnerView;
    public static ArrayAdapter<String> gAgentLogAdapter;
    public static ArrayAdapter<String> gPartnerLogAdapter;
    final Handler mHandlerTime = new Handler();
    protected int mLogMaxCount = 100;

    //Service
    protected AntennaUTService mAntennaUTService;
    protected Intent mServiceIntent = null;
    protected ServiceConnection mServiceConnection = null;

    private String mAddress;

    class myServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mAntennaUTService = ((AntennaUTService.LocalBinder) (service)).getService();
            setUiStatus();
            setLogView();
            mAntennaUTService.addLogUiListerner(((AntennaUTActivity)mCtx).getLocalClassName(), mOnLogUiListener);
            if(AntennaUTService.gAgentLogAdapter == null) {
                AntennaUTService.gAgentLogAdapter = new ArrayAdapter<>(mCtx, R.layout.message);
            }
            if(AntennaUTService.gPartnerLogAdapter == null) {
                AntennaUTService.gPartnerLogAdapter = new ArrayAdapter<>(mCtx, R.layout.message);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAntennaUTService.getAirohaLink().registerOnConnStateListener(TAG, mSppStateListener);
                    mAntennaUTService.getAirohaLink().connect(mAddress);
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected");
            mAntennaUTService = null;
            finish();
        }
    }

    private final Runnable getAntennaUTLog = new Runnable() {
        public void run() {
            if (mAntennaUTService.getReportStatus()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLogView();
                    }
                });
                mHandlerTime.postDelayed(getAntennaUTLog, 2000);
            } else {
                setUiStatus();
            }
        }
    };

    private AntennaUtLogUiListener mOnLogUiListener = new AntennaUtLogUiListener() {
        @Override
        public void OnAddLog(final boolean is_agent, final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (is_agent) {
                        if (gAgentLogAdapter.getCount() >= mLogMaxCount) {
                            gAgentLogAdapter.remove(gAgentLogAdapter.getItem(0));
                        }
                        gAgentLogAdapter.add(msg);
                    } else {
                        if (gPartnerLogAdapter.getCount() >= mLogMaxCount) {
                            gPartnerLogAdapter.remove(gPartnerLogAdapter.getItem(0));
                        }
                        gPartnerLogAdapter.add(msg);
                    }
                }
            });
        }

        @Override
        public void OnReportStop() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mAntennaUTService != null) {
                        mAntennaUTService.setReportStatus(false);
                        setUiStatus();
                    }
                }
            });
        }

        @Override
        public void OnStatisticsReport(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlertDialog(AntennaUTActivity.this, "Statistics Report", msg);

                }
            });
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antenna_ut);
        setTitle("Antenna UT");

        mCtx = this;
        gAgentLogAdapter = new ArrayAdapter<>(mCtx, R.layout.message);
        gPartnerLogAdapter = new ArrayAdapter<>(mCtx, R.layout.message);
        mServiceIntent = new Intent(this, AntennaUTService.class);
        startService(mServiceIntent);
        initUImember();

        final Intent intent = getIntent();
        if(intent != null) {
            final String address = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            mAddress = address;
            mTextViewSppAddr.setText(address);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceConnection = new myServiceConnection();
        bindService(mServiceIntent, mServiceConnection, Context.BIND_NOT_FOREGROUND);
        updatePairedList();
    }

    @Override
    protected void onPause() {
        if (mAntennaUTService != null) {
            mAntennaUTService.getAirohaLink().unregisterOnConnStateListener(TAG);
            unbindService(mServiceConnection);
            mAntennaUTService = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAntennaUTService != null) {
            unbindService(mServiceConnection);
            mAntennaUTService = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(!mAntennaUTService.getReportStatus()) {
                if(mAntennaUTService.getConnectionStatus()) {
                    mBtnDisConSpp.callOnClick();
                }
                if (mAntennaUTService != null) {
                    unbindService(mServiceConnection);
                    mAntennaUTService = null;
                }
                finish();
                return true;
            }
            new AlertDialog.Builder(mCtx)
                    .setTitle("Exit AntennaUT")
                    .setMessage("Run AntennaUT in background?")
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mAntennaUTService.RemoveLogUiListerner(((AntennaUTActivity)mCtx).getLocalClassName());
                                    mAntennaUTService.setForeground("AntennaUT running...");
                                    Intent home = new Intent(Intent.ACTION_MAIN);
                                    home.addCategory(Intent.CATEGORY_HOME);
                                    startActivity(home);
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    if(mAntennaUTService.getConnectionStatus()) {
                                        mBtnDisConSpp.callOnClick();
                                    }
                                    if (mAntennaUTService != null) {
                                        unbindService(mServiceConnection);
                                        mAntennaUTService = null;
                                    }
                                    finish();
//                                     TODO Auto-generated method stub
                                }
                            })
                    .setNeutralButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    //TODO Auto-generated method stub
                                }
                            }).show();
        }
        return true;
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mEditSppAddr = findViewById(R.id.editTextSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);
        mBtnStartAntennaUT = findViewById(R.id.buttonStartAntennaUT);
        mBtnStopAntennaUT = findViewById(R.id.buttonStopAntennaUT);
        mSpinnerRptTime = findViewById(R.id.rpt_time_spinner);
        mSpinnerTestRole = findViewById(R.id.test_role_spinner);
        mCbEnableStatistics = findViewById(R.id.cb_enable_statistics);
        mEditStatisticsCount = findViewById(R.id.edit_statistics_count);
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);

        setLogView();

        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mEditSppAddr.getText().toString();
                try {
                    mAntennaUTService.getAirohaLink().registerOnConnStateListener(TAG, mSppStateListener);
                    Boolean result = mAntennaUTService.getAirohaLink().connect(btaddr);
                    mTextConSppResult.setText(result.toString());
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        mBtnDisConSpp.setEnabled(false);
        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAntennaUTService.getAirohaLink().disconnect();
//                mReportState = false;
            }
        });

        mBtnStartAntennaUT.setEnabled(false);
        mBtnStartAntennaUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAntennaUTService.setReportStatus(true);
                mAntennaUTService.setReportTimeIndex(mSpinnerRptTime.getSelectedItemPosition());
                mAntennaUTService.setTestRoleIndex(mSpinnerTestRole.getSelectedItemPosition());
                setUiStatus();

                if(mCbEnableStatistics.isChecked()){
                    int count = Integer.parseInt(mEditStatisticsCount.getText().toString());
                    mAntennaUTService.startTest(count);
                }
                else{
                    mAntennaUTService.startTest(0);
                }
            }
        });

        mBtnStopAntennaUT.setEnabled(false);
        mBtnStopAntennaUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAntennaUTService.setReportStatus(false);
                setUiStatus();
            }
        });

        mSpinnerRptTime.setEnabled(false);
        mSpinnerTestRole.setEnabled(false);

        setUiStatus();
    }

    private void setLogView()
    {
        mLogAgentView = (ListView) findViewById(R.id.listView_agent_log);
        mLogAgentView.setAdapter(gAgentLogAdapter);
        mLogPartnerView = (ListView) findViewById(R.id.listView_partner_log);
        mLogPartnerView.setAdapter(gPartnerLogAdapter);
    }

    private void updatePairedList() {
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        mPairedListView = (ListView) findViewById(R.id.list_devices);
        mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
        mPairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView) view).getText().toString();
                Log.d(TAG, "clicked:" + info);
                String addr = info.split("\n")[1];
                Log.d(TAG, addr);

                mEditSppAddr.setText(addr);
            }
        });
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
            Log.d("DeviceActivity", "Device : address : " + device.getAddress() + " name :"
                    + device.getName());

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
            mEditSppAddr.setText(device.getAddress());
        }

//        try {
//            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];
//            mEditSppAddr.setText(lastdevice.getAddress());
//
//            ParcelUuid[] parcelUuids = lastdevice.getUuids();
//
//            for (ParcelUuid parcelUuid : parcelUuids) {
//                Log.d(TAG, parcelUuid.toString());
//
//                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
//                    Log.d(TAG, "found Airoha device");
//
//                    Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_LONG).show();
//
//                    Boolean result = mAntennaUTService.getAirohaLink().connect(lastdevice.getAddress());
//                    mTextConSppResult.setText(result.toString());
//                    return;
//                }
//            }
//        } catch (Exception e) {
//            Log.d(TAG, e.getMessage());
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connected");
                    mAntennaUTService.setConnectionStatus(true);
                    setUiStatus();

//                    mConnected = true;
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Disconnected.");

                    mAntennaUTService.setConnectionStatus(false);
                    mAntennaUTService.setReportStatus(false);
                    setUiStatus();
                    mAntennaUTService.initFlagsNParameters();
//                    initFlagsNParameters();
                }
            });
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connecting");
                }
            });
        }

        @Override
        public void OnDisConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(mCtx, "Disconnecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Disconnecting");
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };

    private void setUiStatus()
    {
        if(mAntennaUTService == null)
            return;

        if(!mAntennaUTService.getConnectionStatus()) {
            mBtnConSpp.setEnabled(true);
            mBtnDisConSpp.setEnabled(false);
            mBtnStartAntennaUT.setEnabled(false);
            mBtnStopAntennaUT.setEnabled(false);
            mSpinnerRptTime.setEnabled(false);
            mSpinnerTestRole.setEnabled(false);
        }
        else if(!mAntennaUTService.getReportStatus()) {
            mBtnConSpp.setEnabled(false);
            mBtnDisConSpp.setEnabled(true);
            mBtnStartAntennaUT.setEnabled(true);
            mBtnStopAntennaUT.setEnabled(false);
            mSpinnerRptTime.setEnabled(true);
            mSpinnerTestRole.setEnabled(true);
        }
        else{
            mBtnConSpp.setEnabled(false);
            mBtnDisConSpp.setEnabled(true);
            mBtnStartAntennaUT.setEnabled(false);
            mBtnStopAntennaUT.setEnabled(true);
            mSpinnerRptTime.setEnabled(false);
            mSpinnerTestRole.setEnabled(false);
            mSpinnerRptTime.setSelection(mAntennaUTService.getReportTimeIndex());
            mSpinnerTestRole.setSelection(mAntennaUTService.getTestRoleIndex());
        }
    }

    private void showAlertDialog(final Context context, String title, String message){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}
