package com.airoha.ab153x;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.mmi.RaceCmdSuspendDsp;
import com.airoha.android.lib.fota.Airoha153xMceRaceOtaMgr;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.util.Set;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "AirohaUT";

    private AirohaLink mAirohaLink = null;
    private AirohaLink mAirohaLink2 = null;
    private AirohaLink mAirohaLink3 =  null;

    private Airoha153xMceRaceOtaMgr mAirohaRaceOtaMgr = null;
    private Context mCtx;

    private TextView mTextLocalBtMac;

    // Connect
    private EditText mEditSppAddr;
    private Button mBtnConSpp;
    private Button mBtnDisConSpp;
    private TextView mTextConSppResult;
    private TextView mTextConSppState;

    private Button mBtnConSpp2;
    private Button mBtnDisConSpp2;
    private TextView mTextConSppResult2;
    private TextView mTextConSppState2;

    private Button mBtnConSpp3;
    private Button mBtnDisConSpp3;
    private TextView mTextConSppResult3;
    private TextView mTextConSppState3;

    // test case
    private Button mBtnTestActiveFotaPreparation;
    private Button mBtnTestGetFotaVersion;
    private Button mBtnTestSuspendDspOnConn2;

    private Button mBtnTxConn1;
    private EditText mEditTxConn1;
    
    private Button mBtnTxConn3;
    private EditText mEditTxConn3;

    private Button mBtnGetAvaDst;
    private Button mBtnTestRelay;


    // paired list
    private ListView mPairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);

        initUImember();

        updatePairedList();

        mAirohaRaceOtaMgr = new Airoha153xMceRaceOtaMgr(mAirohaLink);

        mAirohaLink2 = new AirohaLink(this);
        mAirohaLink2.registerOnConnStateListener(TAG, mSppStateListener2);

        mAirohaLink3 = new AirohaLink(this);
        mAirohaLink3.registerOnConnStateListener(TAG, mSppStateListener3);
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mEditSppAddr = findViewById(R.id.editTextSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);

        mBtnConSpp2 = findViewById(R.id.buttonConSpp2);
        mBtnDisConSpp2 = findViewById(R.id.buttonDisConSPP2);
        mTextConSppResult2 = findViewById(R.id.textViewConSppResult2);
        mTextConSppState2 = findViewById(R.id.textViewConSppState2);

        mBtnConSpp3 = findViewById(R.id.buttonConSpp3);
        mBtnDisConSpp3 = findViewById(R.id.buttonDisConSPP3);
        mTextConSppResult3 = findViewById(R.id.textViewConSppResult3);
        mTextConSppState3 = findViewById(R.id.textViewConSppState3);
        

        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String btaddr = mEditSppAddr.getText().toString();

                startConnectThread(btaddr);
            }
        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.disconnect();
            }
        });

        mBtnTestActiveFotaPreparation = findViewById(R.id.buttonTestActiveFotaPreparation);
        mBtnTestActiveFotaPreparation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAirohaRaceOtaMgr.test_ActiveFotaPrepartion();
            }
        });

        mBtnConSpp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String btaddr = mEditSppAddr.getText().toString();

                startConnectThread2(btaddr);
            }
        });

        mBtnDisConSpp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink2.disconnect();
            }
        });

        mBtnConSpp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String btaddr = mEditSppAddr.getText().toString();

                startConnectThread3(btaddr);
            }
        });

        mBtnDisConSpp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink3.disconnect();
            }
        });

        mBtnTestGetFotaVersion = findViewById(R.id.buttonTestGetFotaVersion);
        mBtnTestGetFotaVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAirohaRaceOtaMgr.test_GetFotaVersion();
            }
        });

        mBtnTestSuspendDspOnConn2 = findViewById(R.id.buttonTestSuspendDsp);
        mBtnTestSuspendDspOnConn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RacePacket cmd = new RaceCmdSuspendDsp();
                mAirohaLink2.sendCommand(cmd.getRaw());
            }
        });

        mEditTxConn1 = findViewById(R.id.editTxConn1);
        mBtnTxConn1 = findViewById(R.id.buttonTxConn1);
        mBtnTxConn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hex = mEditTxConn1.getText().toString();

                byte[] raw = Converter.hexStringToByteArray(hex);

                mAirohaLink.sendCommand(raw);
            }
        });
        
        mEditTxConn3 = findViewById(R.id.editTxConn3);
        mBtnTxConn3 = findViewById(R.id.buttonTxConn3);
        mBtnTxConn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hex = mEditTxConn3.getText().toString();

                byte[] raw = Converter.hexStringToByteArray(hex);

                mAirohaLink3.sendCommand(raw);
            }
        });


        mTextLocalBtMac = findViewById(R.id.textViewLocalBtMac);
        mTextLocalBtMac.setText(BluetoothAdapter.getDefaultAdapter().getAddress());


        mBtnGetAvaDst = findViewById(R.id.buttonTestGetAvaDst);
        mBtnGetAvaDst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAirohaRaceOtaMgr.getRelayAvaDst();
            }
        });

        mBtnTestRelay = findViewById(R.id.buttonTestRelayCmd);
        mBtnTestRelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
                            mTextConSppResult.setText(result.toString());

                            if(!result){
                                mBtnConSpp.setEnabled(true);
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

    private void startConnectThread2(final String btaddr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final Boolean result = mAirohaLink2.connect(btaddr, 2);

                    ((Activity)mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextConSppResult2.setText(result.toString());

                            if(!result){
                                mBtnConSpp2.setEnabled(true);
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

    private void startConnectThread3(final String btaddr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final Boolean result = mAirohaLink3.connect(btaddr, 3);

                    ((Activity)mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextConSppResult3.setText(result.toString());

                            if(!result){
                                mBtnConSpp3.setEnabled(true);
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
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connected");
                    mBtnDisConSpp.setEnabled(true);

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
                    mBtnConSpp.setEnabled(true);
                }
            });
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connecting");

                    mBtnConSpp.setEnabled(false);
                    mBtnDisConSpp.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Disconnecting");

                    mBtnDisConSpp.setEnabled(true);
                    mBtnConSpp.setEnabled(false);
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };

    private final OnAirohaConnStateListener mSppStateListener2 = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState2.setText("Connected");

                    mBtnConSpp2.setEnabled(false);
                    mBtnDisConSpp2.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState2.setText("Disconnected.");
                    mBtnConSpp2.setEnabled(true);
                }
            });
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState2.setText("Connecting");

                    mBtnConSpp2.setEnabled(false);
                    mBtnDisConSpp2.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState2.setText("Disconnecting");

                    mBtnDisConSpp2.setEnabled(true);
                    mBtnConSpp2.setEnabled(false);
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };

    private final OnAirohaConnStateListener mSppStateListener3 = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState3.setText("Connected");

                    mBtnConSpp3.setEnabled(false);
                    mBtnDisConSpp3.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState3.setText("Disconnected.");
                    mBtnConSpp3.setEnabled(true);
                }
            });
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState3.setText("Connecting");

                    mBtnConSpp3.setEnabled(false);
                    mBtnDisConSpp3.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Disconnecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState3.setText("Disconnecting");

                    mBtnDisConSpp3.setEnabled(true);
                    mBtnConSpp3.setEnabled(false);
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };


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

            ParcelUuid[] parcelUuids = device.getUuids();

            for (ParcelUuid uuid : parcelUuids) {
                Log.d(TAG, device.getName() + "uuid:" + uuid.toString());
            }

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }

        try {
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];
            mEditSppAddr.setText(lastdevice.getAddress());

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
