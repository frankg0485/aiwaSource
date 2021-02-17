package com.airoha.ab153x;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelUuid;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airoha.android.lib.fota.Airoha153xMceRaceOtaMgr;
import com.airoha.android.lib.fota.OnAirohaFotaStatusClientAppListener;
import com.airoha.android.lib.fota.actionEnum.DualActionEnum;
import com.airoha.android.lib.fota.actionEnum.SingleActionEnum;
import com.airoha.android.lib.fota.fotaInfo.DualFotaInfo;
import com.airoha.android.lib.fota.fotaInfo.SingleFotaInfo;
import com.airoha.android.lib.fota.fotaSetting.FotaSingleSettings;
import com.airoha.android.lib.fota.fotaSetting.PartitionType;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnAirohaRespTimeoutListener;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class Single153xMceActivity extends AppCompatActivity {

    public static final String URL_AIROHA_FOTA = "http://www.airoha.com.tw/ota-upload/1530/FOTA/1109/1109fotapackage.bin";
    public static final String URL_AIROHA_FILESYSTEM = "http://www.airoha.com.tw/ota-upload/1530/FOTA/1109/1109filesystem.bin";
    public static final String URL_AIROHA_NVR = "http://www.airoha.com.tw/ota-upload/1530/FOTA/1109/AB153x_MceFota_20181109_nvr.bin";
    private static final String TAG = "AirohaRaceFotaUT";
    private AirohaLink mAirohaLink = null;
    private Airoha153xMceRaceOtaMgr mAirohaOtaMgr = null;
    // Connect
    private TextView mTextViewSppAddr;
    private Button mBtnConSpp;
    private Button mBtnDisConSpp;
    private TextView mTextConSppResult;
    private TextView mTextConSppState;
    private TextView mTextOtaStatus;
    private TextView mTextOtaError;
    private TextView mTextOtaWarning;
    // Interaction UI
    private FilePickerDialog mFwFilePickerDialog;
    private EditText mEditFwBinPath;
    private Button mBtnFwBinFilePicker;
    private FilePickerDialog mFileSystemBinFilePickerDialog;
    private EditText mEditFileSystemBinPath;
    private Button mBtnFileSystemBinFilePicker;
    private FilePickerDialog mNvrBinFilePickerDialog;
    private EditText mEditNvrBinPath;
    private Button mBtnNvrBinFilePicker;
    private Button mBtnDownloadFromInternet;
    private CheckBox mChkDownloadFromInternet;
    private Button mBtnQueryPartitionAndState;
    private TextView mTextCallBackState;
    private Button mBtn_153X_RestoreNewFileSystem;
    private Button mBtn_153X_UpdateReconnectNvKey;
    private Button mBtnUpdateNvr;
    private TextView mTextStateEnum;
    private TextView mTextVersion;
    private Button mBtn_153X_155x_StartResumableEraseFota_V2;
    private Button mBtn_Cancel;
    private Spinner mSpinnerRole;

    private EditText mEditBatteryThreshold;

    //    private long mBinFileSize;
    private String mSelectedFotaBinFileName;
    private String mSelectedFileSystemBinFileName;
    private String mSelectedNvrBinFileName;
    private String mSubStrFileFota;
    private String mSubStrFileFileSystem;
    private String mSubStrNvr;

    private byte mRole;

    private int mDownloadedCounter = 1;

    private CheckBox mChkSwitchLongPacketMode;
    private EditText mEditLongPacketCmdCount;
    private EditText mEditLongPacketCmdDelay;

    private EditText mEditRespTimeout;

    private Button mBtnSetPrePollSize;
    private EditText mEditPrePollSize;

    private ListView mPairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private TextView mTextCompany;
    private TextView mTextModel;
    private TextView mTextDate;
    //private TextView mTextVersion;

    private Context mCtx = this;
    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connected");

                    mBtnQueryPartitionAndState.setEnabled(true);
                    mBtn_Cancel.setEnabled(true);
//                    mBtnDisConSpp.setEnabled(true);
                }
            });

            // 2018.05.24 Daniel: 2811 can't use RACE command right after A2DP connected.
            // 2018.11.26 Daniel: Not for 153X
            mAirohaOtaMgr.querySingleFotaInfo(mRole);
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connecting");

                    mBtnConSpp.setEnabled(false);
//                    mBtnDisConSpp.setEnabled(false);
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

//                    mBtnConSpp.setEnabled(false);
//                    mBtnDisConSpp.setEnabled(false);
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

                    mBtnQueryPartitionAndState.setEnabled(false);
                    mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                    mBtn_153X_UpdateReconnectNvKey.setEnabled(false);
                    mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                    mBtnUpdateNvr.setEnabled(false);

                    mBtnConSpp.setEnabled(true);
                    mBtn_Cancel.setEnabled(false);
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };
    private FotaSingleSettings mFotaSettings = new FotaSingleSettings();
    private BroadcastReceiver mA2dpListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
//                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
//
//                if (state == BluetoothA2dp.STATE_CONNECTED) {
//                    if (!mAirohaLink.isConnected()) {
//                        startConnectThread(mEditSppAddr.getText().toString());
//                    }
//                }
            }
        }
    };
    private OnAirohaRespTimeoutListener mOnAirohaRespTimeoutListener = new OnAirohaRespTimeoutListener() {
        @Override
        public void OnRespTimeout() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Timeout. FW not responding", Toast.LENGTH_SHORT).show();

//                    String lastStatus = mTextOtaStatus.getText().toString();

                    mTextOtaStatus.setText("Timeout. FW not responding.");
                }
            });
        }
    };
    private OnAirohaFotaStatusClientAppListener mAppListener = new OnAirohaFotaStatusClientAppListener() {
        @Override
        public void notifyBatterLevelLow() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                    mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                    mBtnUpdateNvr.setEnabled(false);
                    mTextCallBackState.setText("Battery Level is low, not allowed for FOTA");
                }
            });
        }

        @Override
        public void notifyClientExistence(boolean isClientExisting) {

        }

        @Override
        public void notifyWarning(final String errorMsg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaWarning.setText(errorMsg);
                    mTextOtaWarning.setTextColor(Color.BLUE);
                }
            });
        }

        @Override
        public void notifyCompleted(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaStatus.setText(msg);

                    mBtnQueryPartitionAndState.setEnabled(true);
                }
            });
        }

        @Override
        public void notifyError(final String error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaError.setText(error);
                    mTextOtaError.setTextColor(Color.RED);

                    mBtnQueryPartitionAndState.setEnabled(true); // if error let user try to query
                }
            });
        }

        @Override
        public void notifyInterrupted(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaStatus.setText(msg);
                }
            });
        }

        @Override
        public void notifyStateEnum(final String state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextStateEnum.setText(state);
                }
            });
        }

        @Override
        public void notifyStatus(final String status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaStatus.setText(status);
                }
            });
        }

        @Override
        public void onAvailableDualActionUpdated(DualActionEnum actionEnum) {

        }

        @Override
        public void onAvailableSingleActionUpdated(SingleActionEnum actionEnum) {
            if (SingleActionEnum.StartFota == actionEnum) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextCallBackState.setText("notifyStartFota");
//                        mBtn_153X_155X_StartFota.setEnabled(true);
//
//                        mBtn_153X_155x_StartResumableEraseFota.setEnabled(true);
                        mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(true);
                        mBtnUpdateNvr.setEnabled(true);
                    }
                });
            }

//            if (SingleActionEnum.RestoreOldFileSystem == actionEnum) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextCallBackState.setText("notifyRestoreOldFileSystem");
//                        mBtn_153X_RestoreOldFileSystem.setEnabled(true);
//                    }
//                });
//            }

            if (SingleActionEnum.RestoreNewFileSystem == actionEnum) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextCallBackState.setText("notifyRestoreNewFileSystem");
                        mBtn_153X_RestoreNewFileSystem.setEnabled(true);
                    }
                });
            }

//            if (SingleActionEnum.NeedToUpdateReconnectNvKey == actionEnum) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextCallBackState.setText("notifyNeedToUpdateReconnectNvKey");
//                        mBtn_153X_UpdateReconnectNvKey.setEnabled(true);
//                    }
//                });
//            }

        }

        @Override
        public void onDualFotaInfoUpdated(DualFotaInfo info) {

        }

        @Override
        public void onProgressUpdated(final String current_stage, int completed_stage_count, final int total_stage_count, final int completed_task_count, final int total_task_count) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextOtaStatus.setText(String.format("%s(%d/%d)", current_stage, completed_task_count, total_task_count));
                }
            });
        }

        @Override
        public void onSingleFotaInfoUpdated(final SingleFotaInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextStateEnum.setText(info.agentFotaState);

                    mTextVersion.setText(
                            String.format("V%s", info.agentVersion)
                    );

                    mTextCompany.setText(info.agentCompanyName);
                    mTextModel.setText(info.agentModelName);
                    mTextDate.setText(info.agentReleaseDate);
                }
            });
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothA2dp mA2dpProfileProxy;
    private BluetoothProfile.ServiceListener mServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dpProfileProxy = (BluetoothA2dp) proxy;

                try {
                    BluetoothDevice connectedDevice = mA2dpProfileProxy.getConnectedDevices().get(0);

                    Log.d(TAG, "a2dp connected device: " + connectedDevice.getName() + connectedDevice.getUuids().toString());
//                    mTextViewSppAddr.setText(connectedDevice.getAddress());

                    ParcelUuid[] parcelUuids = connectedDevice.getUuids();

                    for (ParcelUuid parcelUuid : parcelUuids) {
                        Log.d(TAG, parcelUuid.toString());

                        if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                            Log.d(TAG, "found Airoha device");

                            Toast.makeText(mCtx, "Found Airoha Device:" + connectedDevice.getName(), Toast.LENGTH_LONG).show();

                            startConnectThread(connectedDevice.getAddress());
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
//                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    private void configDownloader() {
        // Setting timeout globally for the download network requests:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
    }

    private void configLongPacketMode() {
        if (mChkSwitchLongPacketMode.isChecked()) {
            int delay = Integer.valueOf(mEditLongPacketCmdDelay.getText().toString());
            mFotaSettings.programInterval = delay;
            mEditLongPacketCmdDelay.setEnabled(false);
        } else {
            mEditLongPacketCmdDelay.setEnabled(true);
        }
    }

    private void connectProfile() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothAdapter.getProfileProxy(mCtx, mServiceListener, BluetoothProfile.A2DP);
    }

    private void disconnectProfile() {
        if (mA2dpProfileProxy != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dpProfileProxy);
        }
    }

    private void initFileSystemFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "ext"};
        mFileSystemBinFilePickerDialog = new FilePickerDialog(this, properties);
        mFileSystemBinFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mSelectedFileSystemBinFileName = files[0].toString();

                    mEditFileSystemBinPath.setText(mSelectedFileSystemBinFileName);
                }
            }
        });
    }

    private void initFwFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "ext"};
        mFwFilePickerDialog = new FilePickerDialog(this, properties);
        mFwFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mSelectedFotaBinFileName = files[0].toString();

                    mEditFwBinPath.setText(mSelectedFotaBinFileName);
                }
            }
        });
    }

    private void initNvrFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"nvr"};
        mNvrBinFilePickerDialog = new FilePickerDialog(this, properties);
        mNvrBinFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mSelectedNvrBinFileName = files[0].toString();

                    mEditNvrBinPath.setText(mSelectedNvrBinFileName);
                }
            }
        });
    }

    private void initPreferences() {
        SharedPreferences preference = this.getPreferences(Context.MODE_PRIVATE);
        mEditRespTimeout.setText(preference.getString("RespTimeout", "30000"));
//        mEditLongPacketCmdDelay.setText(preference.getString("LongPacketCmdDelay", "50"));
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);

//        mBtnFwBinFilePicker = findViewById(R.id.buttonFwBinFilePicker);
        mEditFwBinPath = findViewById(R.id.editTextFwBinPath);

//        mBtnFileSystemBinFilePicker = findViewById(R.id.buttonFileSystemBinFilePicker);
        mEditFileSystemBinPath = findViewById(R.id.editTextFileSystemBinPath);

//        mBtnNvrBinFilePicker = findViewById(R.id.buttonNvrBinFilePicker);
        mEditNvrBinPath = findViewById(R.id.editTextNvrBinPath);

        mEditBatteryThreshold = findViewById(R.id.editText_batteryThreshold);

        mTextOtaStatus = findViewById(R.id.textViewStatus);
        mTextOtaError = findViewById(R.id.textViewError);
        mTextOtaWarning = findViewById(R.id.textViewWarning);

        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                final String btaddr = mTextViewSppAddr.getText().toString();

                startConnectThread(btaddr);
            }
        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)view).getText());
                mAirohaLink.disconnect();
            }
        });

        mBtnFwBinFilePicker = findViewById(R.id.buttonFwBinFilePicker);
        mBtnFwBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnFwBinFilePicker");
                mFwFilePickerDialog.show();
            }
        });

        mBtnFileSystemBinFilePicker = findViewById(R.id.buttonFileSystemBinFilePicker);
        mBtnFileSystemBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.logToFile(TAG, "onClick: BtnFileSystemBinFilePicker");
                mFileSystemBinFilePickerDialog.show();
            }
        });

        mBtnNvrBinFilePicker = findViewById(R.id.buttonNvrBinFilePicker);
        mBtnNvrBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnNvrBinFilePicker");
                mNvrBinFilePickerDialog.show();
            }
        });


        mBtnDownloadFromInternet = findViewById(R.id.buttonDownloadFromInternet);
        mBtnDownloadFromInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)view).getText());
                // Make a download request
                configDownloader();

                String urlFota = mEditFwBinPath.getText().toString();
                String urlFileSystem = mEditFileSystemBinPath.getText().toString();
                String urlNvrBin = mEditNvrBinPath.getText().toString();

                if (urlFota != null && !urlFota.isEmpty()) {
                    mSubStrFileFota = urlFota.substring(urlFota.lastIndexOf('/') + 1);
                    mSelectedFotaBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrFileFota).toString();

                    mDownloadedCounter = 1;
                }

                if (urlFileSystem != null && !urlFileSystem.isEmpty()) {
                    mSubStrFileFileSystem = urlFileSystem.substring(urlFileSystem.lastIndexOf('/') + 1);
                    mSelectedFileSystemBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrFileFileSystem).toString();

                    mDownloadedCounter = 2;
                }

                if (urlNvrBin != null && !urlNvrBin.isEmpty()) {
                    mSubStrNvr = urlNvrBin.substring(urlNvrBin.lastIndexOf('/') + 1);
                    mSelectedNvrBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrNvr).toString();

                    mDownloadedCounter = 3;
                }

                switch (mDownloadedCounter) {
                    case 1:
                        makeDownloadRequest(urlFota, mSubStrFileFota);
                        break;
                    case 2:
                        makeDownloadRequest(urlFota, mSubStrFileFota);
                        makeDownloadRequest(urlFileSystem, mSubStrFileFileSystem);
                        break;
                    case 3:
                        makeDownloadRequest(urlFota, mSubStrFileFota);
                        makeDownloadRequest(urlFileSystem, mSubStrFileFileSystem);
                        makeDownloadRequest(urlNvrBin, mSubStrNvr);
                        break;
                }

            }
        });

        mChkDownloadFromInternet = findViewById(R.id.chkDownFromInternet);
        mChkDownloadFromInternet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                mSelectedFotaBinFileName = null;
                mSelectedFileSystemBinFileName = null;

                if (!isChecked) {
                    mBtnFileSystemBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnFwBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnNvrBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnDownloadFromInternet.setVisibility(View.GONE);
//                    mBtnFotaStartAll.setVisibility(View.VISIBLE);

                    mEditFwBinPath.setText("");
                    mEditFileSystemBinPath.setText("");
                    mEditNvrBinPath.setText("");

                } else {
                    mBtnFileSystemBinFilePicker.setVisibility(View.GONE);
                    mBtnFwBinFilePicker.setVisibility(View.GONE);
                    mBtnNvrBinFilePicker.setVisibility(View.GONE);
                    mBtnDownloadFromInternet.setVisibility(View.VISIBLE);
//                    mBtnFotaStartAll.setVisibility(View.GONE);

                    mEditFwBinPath.setText(URL_AIROHA_FOTA);
                    mEditFileSystemBinPath.setText(URL_AIROHA_FILESYSTEM);
                    mEditNvrBinPath.setText(URL_AIROHA_NVR);
                }
            }
        });
        mChkDownloadFromInternet.setChecked(false); // set for internet

        mBtnQueryPartitionAndState = findViewById(R.id.btnQueryPartitionAndState);
        mBtnQueryPartitionAndState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Single153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);

//                mBtn_153X_155x_ReStartFota.setEnabled(false);
//                mBtn_153X_155X_StartFota.setEnabled(false);
                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
//                mBtn_153X_UpdateReconnectNvKey.setEnabled(false);

//                mBtn_153X_155x_StartResumableEraseFota.setEnabled(false);
                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                mBtnUpdateNvr.setEnabled(false);
//                mBtn_153X_155x_ReStartResumableFota.setEnabled(false);

                mTextOtaStatus.setText("");
                mTextOtaError.setText("");
                mTextStateEnum.setText("");
                mTextCallBackState.setText("");
                mTextVersion.setText("");

                int batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                mAirohaOtaMgr.querySingleFotaInfo(mRole, batteryThreshold);
            }
        });

        mTextCallBackState = findViewById(R.id.textViewCallBackState);

        mTextStateEnum = findViewById(R.id.textViewStateEnum);
        //mTextVersion = findViewById(R.id.textViewVersion);

        mTextCompany = findViewById(R.id.textCompany);
        mTextModel = findViewById(R.id.textModel);
        mTextDate = findViewById(R.id.textDate);
        mTextVersion = findViewById(R.id.textVersion);

//        mBtn_153X_RestoreOldFileSystem = findViewById(R.id.btn_153X_RestoreOldFileSystem);
//        mBtn_153X_RestoreOldFileSystem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mBtnQueryPartitionAndState.setEnabled(false);
//                mBtn_153X_RestoreOldFileSystem.setEnabled(false);
//
//                try {
//                    mFotaSettings.partitionType = PartitionType.FileSystem;
//                    mFotaSettings.actionEnum = SingleActionEnum.RestoreOldFileSystem;
//                    mAirohaOtaMgr.startSingleFota(mSelectedFileSystemBinFileName, mFotaSettings, mRole);
//                } catch (Exception e) {
//                    Toast.makeText(Single153xMceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    mBtnQueryPartitionAndState.setEnabled(true);
//                    mBtn_153X_RestoreOldFileSystem.setEnabled(true);
//                }
//            }
//        });

        mBtn_153X_RestoreNewFileSystem = findViewById(R.id.btn_153X_RestoreNewFileSystem);
        mBtn_153X_RestoreNewFileSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Single153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);
                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);

                try {
                    mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mFotaSettings.partitionType = PartitionType.FileSystem;
                    mFotaSettings.actionEnum = SingleActionEnum.RestoreNewFileSystem;
                    mAirohaOtaMgr.startSingleFota(mSelectedFileSystemBinFileName, mFotaSettings, mRole);
                } catch (Exception e) {
                    Toast.makeText(Single153xMceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    mBtnQueryPartitionAndState.setEnabled(true);
                    mBtn_153X_RestoreNewFileSystem.setEnabled(true);
                }
            }
        });

        mBtn_153X_UpdateReconnectNvKey = findViewById(R.id.btn_153X_UpdateReconnectNvKey);
//        mBtn_153X_UpdateReconnectNvKey.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mBtnQueryPartitionAndState.setEnabled(false);
////                mBtn_153X_UpdateReconnectNvKey.setEnabled(false);
//
//                mAirohaOtaMgr.updateReconnectNvKeySingle();
//            }
//        });

        mBtnUpdateNvr = findViewById(R.id.btnUpdateNvr);
        mBtnUpdateNvr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Single153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnUpdateNvr.setEnabled(false);
                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
//                mAirohaOtaMgr.startUpdateSingleNvr(mSelectedNvrBinFileName);
                mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                mFotaSettings.actionEnum = SingleActionEnum.UpdateNvr;
                mAirohaOtaMgr.startSingleFota(mSelectedNvrBinFileName, mFotaSettings, mRole);
            }
        });

        mEditLongPacketCmdCount = findViewById(R.id.editLongPacketCmdCount);
        mEditLongPacketCmdDelay = findViewById(R.id.editLongPacketCmdDelay);
        mEditRespTimeout = findViewById(R.id.editRespTimeout);
        mEditRespTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String timeout = mEditRespTimeout.getText().toString();
                if (timeout != null && timeout.length() > 0) {
                    mAirohaLink.setResponseTimeout(Integer.parseInt(timeout));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mChkSwitchLongPacketMode = findViewById(R.id.chkSwitchLongPacketMode);
        mChkSwitchLongPacketMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configLongPacketMode();
            }
        });


        mEditPrePollSize = findViewById(R.id.editPrePollSize);

        mBtnSetPrePollSize = findViewById(R.id.btnSetPrePollSize);
        mBtnSetPrePollSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                try {
                    int prePollSize = Integer.valueOf(mEditPrePollSize.getText().toString());

                    mFotaSettings.slidingWindow = prePollSize;
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        mBtn_153X_155x_StartResumableEraseFota_V2 = findViewById(R.id.btn_153X_155X_StartResumableFota_V2);
        mBtn_153X_155x_StartResumableEraseFota_V2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Single153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);
                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                mBtnUpdateNvr.setEnabled(false);

                try {
                    mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mFotaSettings.partitionType = PartitionType.Fota;
                    mFotaSettings.actionEnum = SingleActionEnum.StartFota;
                    mAirohaOtaMgr.startSingleFota(mSelectedFotaBinFileName, mFotaSettings, mRole);
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                    mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(true);
                    mBtn_153X_RestoreNewFileSystem.setEnabled(true);
                    mBtnUpdateNvr.setEnabled(true);
                }
            }
        });

        mBtn_Cancel = findViewById(R.id.btn_Cancel);
        mBtn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                mAirohaOtaMgr.cancelSingleFota(mRole);
            }
        });


        setupSpinner();
    }

    private void setupSpinner() {
        mSpinnerRole = findViewById(R.id.spinnerRole);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerRole.setAdapter(adapter);

        mSpinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "selected pos: " + position);

                mRole = (byte) position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void makeDownloadRequest(String url, final String fileName) {
        PRDownloader.download(url, Environment.getExternalStorageDirectory().getPath(), fileName)
                .build().setOnProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(Progress progress) {
                Log.d(TAG, fileName + " down progress:" + String.format("%d/%d", progress.currentBytes, progress.totalBytes));

                mTextOtaStatus.setText(fileName + " down progress:" + String.format("%d/%d", progress.currentBytes, progress.totalBytes));

            }
        }).start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Log.d(TAG, "Download Complete:" + fileName);

                mTextOtaStatus.setText("Download Completed:" + fileName);

                mDownloadedCounter--;

                if (mDownloadedCounter == 0) {
                    mTextOtaStatus.setText("Download Completed. ");
                }
            }

            @Override
            public void onError(Error error) {

                mTextOtaStatus.setText("Error during download from internet:" + error.toString());
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_main);
        setTitle("FOTA - Single Mode");

        initUImember();

        requestExternalStoragePermission();

        initFwFileDialog();
        initFileSystemFileDialog();
        initNvrFileDialog();

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnRespTimeoutListener(TAG, mOnAirohaRespTimeoutListener);

        mAirohaOtaMgr = new Airoha153xMceRaceOtaMgr(mAirohaLink);
        mAirohaOtaMgr.registerListener(TAG, mAppListener);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mA2dpListener, intentFilter);

        initPreferences();

        updatePairedList();

        connectProfile();

        mEditPrePollSize.setText(String.valueOf(mAirohaOtaMgr.getFotaStagePrePollSize()));

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

    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();
        unregisterReceiver(mA2dpListener);
        disconnectProfile();

        savePreferences();

        super.onDestroy();
    }

    private void requestExternalStoragePermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.INTERNET},
                    0
            );
        }
    }

    private void savePreferences() {
        SharedPreferences preference = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("RespTimeout", mEditRespTimeout.getText().toString());
//        editor.putString("LongPacketCmdDelay", mEditLongPacketCmdDelay.getText().toString());
        editor.commit();
    }

    private void startConnectThread(final String btaddr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Boolean result = mAirohaLink.connect(btaddr);

                    ((Activity) mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextConSppResult.setText(result.toString());

                            if (!result) {
                                mBtnConSpp.setEnabled(true);
                            }
                        }
                    });

                } catch (final Exception e) {

                    ((Activity) mCtx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
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

//                mTextViewSppAddr.setText(addr);
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
    }
}
