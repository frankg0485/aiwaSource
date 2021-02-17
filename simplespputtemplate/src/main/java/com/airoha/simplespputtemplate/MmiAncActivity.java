package com.airoha.simplespputtemplate;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.mmi.anc.RaceCmdAncOff;
import com.airoha.android.lib.RaceCommand.packet.mmi.anc.RaceCmdAncOn;
import com.airoha.android.lib.RaceCommand.packet.mmi.anc.RaceCmdAncReadParamFromNvKey;
import com.airoha.android.lib.RaceCommand.packet.mmi.anc.RaceCmdAncSetGain;
import com.airoha.android.lib.RaceCommand.packet.mmi.anc.RaceCmdAncWriteGainToNvKey;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnRaceMmiPacketListener;
import com.airoha.android.lib.transport.PacketParser.OnRacePacketListener;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.util.Set;

public class MmiAncActivity extends AppCompatActivity {

    private static final String TAG = "AirohaUT";

    private AirohaLink mAirohaLink = null;
    private Context mCtx;

    // Connect
    private EditText mEditSppAddr;
    private Button mBtnConSpp;
    private Button mBtnDisConSpp;
    private TextView mTextConSppResult;
    private TextView mTextConSppState;

    // paired list
    private ListView mPairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    // Added after Template members

    private Button mBtnAncOn;
    private Button mBtnAncOff;
    private Button mBtnAncSetGain;
    private Button mBtnAncReadParamFromNvKey;
    private Button mBtnAcnWriteGainToNvKey;
    private Button mBtnSendRacePacket;

    private EditText mEditAncSetGain;
    private EditText mEditAncGainToNvKey;
    private EditText mEditRaceId;
    private EditText mEditRacePayload;

    private TextView mTextAncOnResp;
    private TextView mTextAncOffResp;
    private TextView mTextAncSetGainResp;
    private TextView mTextAncReadParamFromNvKeyResp;
    private TextView mTextAncReadParamFromNvKeyInd;
    private TextView mTextAncWriteGainToNvKeyResp;

    private TextView mTextRxRacePacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmi);

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnRaceMmiPacketListener(TAG, mMmiListener);
        mAirohaLink.registerOnRacePacketListener(TAG, mPacketListener);

        initUImember();

        requestExternalStoragePermission();

        updatePairedList();
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mEditSppAddr = findViewById(R.id.editTextSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);


        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mEditSppAddr.getText().toString();

                try {
                    Boolean result = mAirohaLink.connect(btaddr);
                    mTextConSppResult.setText(result.toString());
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.disconnect();
            }
        });

        // added after Template

        mBtnAncOn = findViewById(R.id.btnAncOn);
        mBtnAncOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.sendOrEnqueue(new RaceCmdAncOn().getRaw());
            }
        });

        mBtnAncOff = findViewById(R.id.btnAncOff);
        mBtnAncOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.sendOrEnqueue(new RaceCmdAncOff().getRaw());
            }
        });

        mEditAncSetGain = findViewById(R.id.editAncSetGain);

        mBtnAncSetGain = findViewById(R.id.btnAncSetGain);
        mBtnAncSetGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String cmd = mEditAncSetGain.getText().toString().replace(" ", "");
                    if (cmd.isEmpty()) {
                        return;
                    }

                    byte[] payload = Converter.hexStringToByteArray(cmd);

                    mAirohaLink.sendOrEnqueue(new RaceCmdAncSetGain(payload[0], payload[1]).getRaw());

                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnAncReadParamFromNvKey = findViewById(R.id.btnAncReadParamFromNvKey);
        mBtnAncReadParamFromNvKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.sendOrEnqueue(new RaceCmdAncReadParamFromNvKey().getRaw());
            }
        });

        mEditAncGainToNvKey = findViewById(R.id.editAncGainToNvKey);
        mBtnAcnWriteGainToNvKey = findViewById(R.id.btnAncWriteGainToNvKey);
        mBtnAcnWriteGainToNvKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String cmd = mEditAncGainToNvKey.getText().toString().replace(" ", "");
                    if (cmd.isEmpty()) {
                        return;
                    }

                    byte[] payload = Converter.hexStringToByteArray(cmd);

                    mAirohaLink.sendOrEnqueue(new RaceCmdAncWriteGainToNvKey(payload[0], payload[1]).getRaw());

                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mTextAncOnResp = findViewById(R.id.textAncOnResp);
        mTextAncOffResp = findViewById(R.id.textAncOffResp);
        mTextAncSetGainResp = findViewById(R.id.textAncSetGainResp);
        mTextAncReadParamFromNvKeyResp = findViewById(R.id.textAncReadParamFromNvKeyResp);
        mTextAncReadParamFromNvKeyInd = findViewById(R.id.textAncReadParamFromNvKeyInd);
        mTextAncWriteGainToNvKeyResp = findViewById(R.id.textAncWriteGainToNvKeyResp);

        mTextRxRacePacket = findViewById(R.id.textRxRacePacket);
        mEditRaceId = findViewById(R.id.editRaceId);
        mEditRacePayload = findViewById(R.id.editRacePayload);

        mBtnSendRacePacket = findViewById(R.id.btnSendAnyHex);
        mBtnSendRacePacket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strId = mEditRaceId.getText().toString().replace(" ", "");
                    byte[] id = Converter.hexStringToByteArray(strId);

                    byte[] payload = null;

                    String strPayload = mEditRacePayload.getText().toString().replace(" ", "");
                    if(!strPayload.isEmpty()){
                        payload = Converter.hexStringToByteArray(strPayload);
                    }

                    RacePacket racePacket = new RacePacket(RaceType.CMD_NEED_RESP, id, payload);

                    mAirohaLink.sendOrEnqueue(racePacket.getRaw());
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Conn. :" + type);
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "DisConnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("DisConn.");
                }
            });
        }

        @Override
        public void OnConnecting() {

        }

        @Override
        public void OnDisConnecting() {

        }
    };

    private final OnRacePacketListener mPacketListener = new OnRacePacketListener() {
        @Override
        public void handleRespOrInd(int raceId, final byte[] packet, int raceType) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextRxRacePacket.setText(Converter.byte2HexStr(packet));
                }
            });
        }
    };

    private final OnRaceMmiPacketListener mMmiListener = new OnRaceMmiPacketListener() {
        @Override
        public void OnAncSetOnResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncOnResp.setText(String.format("%02X", resp));
                }
            });
        }

        @Override
        public void OnAncSetOffResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncOffResp.setText(String.format("%02X", resp));
                }
            });
        }

        @Override
        public void OnAncSetGainResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncSetGainResp.setText(String.format("%02X", resp));
                }
            });
        }

        @Override
        public void OnAncGetStatusResp(byte resp) {

        }

        @Override
        public void OnAncReadParamFromNvKeyResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncReadParamFromNvKeyResp.setText(String.format("%02X", resp));
                }
            });
        }

        @Override
        public void OnAncWriteGainToNvKeyResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncWriteGainToNvKeyResp.setText(String.format("%02X", resp));
                }
            });
        }

        @Override
        public void OnAncReadParamFromNvKeyInd(final byte[] payload) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAncReadParamFromNvKeyInd.setText(Converter.byte2HexStr(payload));
                }
            });
        }
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

                    Boolean result = mAirohaLink.connect(lastdevice.getAddress());
                    mTextConSppResult.setText(result.toString());
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestExternalStoragePermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.INTERNET},
                    0
            );
        }
    }
}
