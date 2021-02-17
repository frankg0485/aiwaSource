package com.airoha.ab153x;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airoha.android.lib.transport.AirohaLink;

import java.util.Set;

public class MenuActivity extends AppCompatActivity {
    private String TAG = MenuActivity.class.getName();

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // paired list
    protected ListView mPairedListView;
    protected ArrayAdapter<String> mPairedDevicesArrayAdapter;

    // Connect
    protected TextView mTextViewSppAddr;

    private Button mBtn153xMceSingle;
    private Button mBtn153xMceTws;
    private Button mBtnPeqUxUt;
    private Button mBtnServer;
    private Button mBtnClient;
    private Button mBtnAirDump;
    private Button mBtnAntennaUT;
    private Button mBtnMmiUT;
    private Button mBtnMiniDump;
    private Button mBtnOfflineLog;
    private Button mBtnOnlineLog;
    private Button mBtnKeyMapUt;
    private TextView mTvVer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        mBtn153xMceSingle = findViewById(R.id.btn153xMceSingle);
        mBtn153xMceSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, Single153xMceActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);

            }
        });

        mBtn153xMceTws = findViewById(R.id.btn153xMceTws);
        mBtn153xMceTws.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, Tws153xMceActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnPeqUxUt = findViewById(R.id.btnPeqUxUt);
        mBtnPeqUxUt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, PeqUtActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnServer = findViewById(R.id.btnServer);
        mBtnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, ServerActivity.class));
            }
        });

        mBtnClient = findViewById(R.id.btnClient);
        mBtnClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, ClientActivity.class));
            }
        });

        mBtnAirDump = findViewById(R.id.btnAirDump);
        mBtnAirDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, AirDumpActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnAntennaUT = findViewById(R.id.btnAntennaUT);
        mBtnAntennaUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, AntennaUTActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnMmiUT = findViewById(R.id.btnMmiUt);
        mBtnMmiUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, MmiUtActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnKeyMapUt = findViewById(R.id.btnKeyMapUT);
        mBtnKeyMapUt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, KeyActionUtActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnMiniDump = findViewById(R.id.btnMiniDump);
        mBtnMiniDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, MiniDumpActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnOfflineLog = findViewById(R.id.btnOfflineLog);
        mBtnOfflineLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, OfflineDumpActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mBtnOnlineLog = findViewById(R.id.btnOnlineLog);
        mBtnOnlineLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MenuActivity.this, OnlineDumpActivity.class);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mTextViewSppAddr.getText().toString());
                startActivity(intent);
            }
        });

        mTvVer = findViewById(R.id.txtVer);
        mTvVer.setText(BuildConfig.VERSION_NAME);

        requestExternalStoragePermission();

        mTextViewSppAddr = findViewById(R.id.editTextSppAddr);

        updatePairedList();
    }

    private void requestExternalStoragePermission(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.INTERNET},
                    0
            );
        }
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

                mTextViewSppAddr.setText(addr);
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
            enableAllButtons();
            
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];
            mTextViewSppAddr.setText(lastdevice.getAddress());

            ParcelUuid[] parcelUuids = lastdevice.getUuids();

            enableAllButtons();

            for (ParcelUuid parcelUuid : parcelUuids) {
                Log.d(TAG, parcelUuid.toString());

                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                    Log.d(TAG, "found Airoha device");

                    Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_LONG).show();

//                    Boolean result = mAirohaLink.connect(lastdevice.getAddress());
//                    mTextConSppResult.setText(result.toString());
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void enableAllButtons() {
        mBtn153xMceSingle.setEnabled(true);
        mBtn153xMceTws.setEnabled(true);
        mBtnPeqUxUt.setEnabled(true);
        mBtnAntennaUT.setEnabled(true);
        mBtnMmiUT.setEnabled(true);
        mBtnAirDump.setEnabled(true);
        mBtnKeyMapUt.setEnabled(true);
        mBtnMiniDump.setEnabled(true);
        mBtnOfflineLog.setEnabled(true);
        mBtnOnlineLog.setEnabled(true);
    }
}
