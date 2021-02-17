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
import com.airoha.android.lib.fota.fotaSetting.FotaDualSettings;
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

public class Tws153xMceActivity extends AppCompatActivity {

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
    private FilePickerDialog mLeftChannelFwFilePickerDialog;
    private FilePickerDialog mRightChannelFwFilePickerDialog;
    private EditText mEditLeftChannelFwBinPath;
    private EditText mEditRightChannelFwBinPath;
    private Button mBtnLeftChannelFwBinFilePicker;
    private Button mBtnRightChannelFwBinFilePicker;
    private FilePickerDialog mFileSystemBinFilePickerDialog;
    private EditText mEditFileSystemBinPath;
    private Button mBtnFileSystemBinFilePicker;
    private FilePickerDialog mLeftChannelNvrBinFilePickerDialog;
    private FilePickerDialog mRightChannelNvrBinFilePickerDialog;
    private EditText mEditLeftChannelNvrBinPath;
    private EditText mEditRightChannelNvrBinPath;
    private Button mBtnLeftChannelNvrBinFilePicker;
    private Button mBtnRightChannelNvrBinFilePicker;
    private Button mBtnDownloadFromInternet;
    private CheckBox mChkDownloadFromInternet;
    private Button mBtnQueryPartitionAndState;
    private TextView mTextCallBackState;
    private Button mBtn_153X_RestoreOldFileSystem;
    private Button mBtn_153X_RestoreNewFileSystem;
    private Button mBtn_153X_UpdateReconnectNvKey;
    private Button mBtnUpdateNvr;
    private TextView mTextStateEnum;
    private TextView mTextAudioChannel;
    private Button mBtn_153X_155x_StartResumableEraseFota_V2;
//    private Button mBtnTwsUpdateNvKey;
    private Button mBtn_Cancel;
    private Button mBtnDualCommit;
    private Button mBtnDualReset;
    // for debug not for normal SQA testing
    private Button mBtnDebugGetBatteryRelay;
    private Button mBtnDebugFlashControlRelay;
    private Button mBtnDebugIntegrityCheck;
    //    private long mBinFileSize;
    private String mLeftChannelSelectedFotaBinFileName;
    private String mRightChannelSelectedFotaBinFileName;
    private String mSelectedFileSystemBinFileName;
    private String mLeftChannelSelectedNvrBinFileName;
    private String mRightChannelSelectedNvrBinFileName;
    private String mSubStrLeftChannelFileFota;
    private String mSubStrRightChannelFileFota;
    private String mSubStrFileFileSystem;
    private String mSubStrLeftChannelNvr;
    private String mSubStrRightChannelNvr;

    private int mDownloadedCounter = 0;

    private CheckBox mChkSwitchLongPacketMode;
    private EditText mEditLongPacketCmdCount;
    private EditText mEditLongPacketCmdDelay;

    private EditText mEditRespTimeout;

    private Button mBtnSetPrePollSize;
    private EditText mEditPrePollSize;

    private EditText mEditBatteryThreshold;

    private ListView mPairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private TextView mTextAgentCompany;
    private TextView mTextAgentModel;
    private TextView mTextAgentDate;
    private TextView mTextAgentVersion;

    private TextView mTextPartnerCompany;
    private TextView mTextPartnerModel;
    private TextView mTextPartnerDate;
    private TextView mTextPartnerVersion;

    private Context mCtx = this;

    private boolean mIsAgentLeftPartnerRight;
    private boolean mHasDetectAudioChannel = false;

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connected");

                    mBtnQueryPartitionAndState.setEnabled(true);

                    mBtnDisConSpp.setEnabled(true);
                    mBtn_Cancel.setEnabled(true);
                }
            });

            // 2018.05.24 Daniel: 2811 can't use RACE command right after A2DP connected.
            // 2018.11.26 Daniel: Not for 153X
            mAirohaOtaMgr.queryDualFotaInfo();
        }

        @Override
        public void OnConnecting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connecting", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Connecting");

                    mBtnConSpp.setEnabled(false);
                    mBtnDisConSpp.setEnabled(false);
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

                    mBtnDisConSpp.setEnabled(false);
                    mBtnConSpp.setEnabled(false);
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
                    mBtn_153X_RestoreOldFileSystem.setEnabled(false);
                    mBtn_153X_UpdateReconnectNvKey.setEnabled(false);
                    //mBtn_155X_UpdateNvKey.setEnabled(false);
//                    mBtnRoleSwitch.setEnabled(false);
//                    mBtnTwsUpdateNvKey.setEnabled(false);

                    mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                    mBtnUpdateNvr.setEnabled(false);
                    mBtn_Cancel.setEnabled(false);

                    mBtnConSpp.setEnabled(true);
                }
            });
        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };
    private FotaDualSettings mFotaSettings = new FotaDualSettings();
    private BroadcastReceiver mA2dpListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
//                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);

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
                    mTextCallBackState.setText("Battery level is low, not allowed for FOTA");
                    mBtnQueryPartitionAndState.setEnabled(true);
                }
            });
        }

        @Override
        public void notifyClientExistence(final boolean isClientExisting) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextCallBackState.setText("notifyClientExistence: " + isClientExisting);
                    Toast.makeText(mCtx, "notifyRoleSwitch", Toast.LENGTH_SHORT).show();
                }
            });
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
//            if (DualActionEnum.RoleSwitch == actionEnum) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextCallBackState.setText("notifyRoleSwitch");
//                        Toast.makeText(mCtx, "notifyRoleSwitch", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }

            if (DualActionEnum.StartFota == actionEnum) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextCallBackState.setText("notifyStartFota");
                        mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(true);
                        mBtnUpdateNvr.setEnabled(true);
                    }
                });
            }

            if (DualActionEnum.RestoreNewFileSystem == actionEnum) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextCallBackState.setText("RestoreNewFileSystem");
                        mBtn_153X_RestoreNewFileSystem.setEnabled(true);
                    }
                });
            }

//            if (DualActionEnum.RestoreOldFileSystem == actionEnum) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextCallBackState.setText("RestoreOldFileSystem");
//                        mBtn_153X_RestoreOldFileSystem.setEnabled(true);
//                    }
//                });
//            }

            if (DualActionEnum.TwsCommit == actionEnum) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextCallBackState.setText("TwsCommit");
                    }
                });
            }
        }

        @Override
        public void onAvailableSingleActionUpdated(SingleActionEnum actionEnum) {

        }

        @Override
        public void onDualFotaInfoUpdated(final DualFotaInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextAgentVersion.setText(
                            String.format("V%s", info.agentVersion)
                    );
                    mTextPartnerVersion.setText(
                            String.format("V%s", info.partnerVersion)
                    );

                    mTextAgentCompany.setText(info.agentCompanyName);
                    mTextPartnerCompany.setText(info.partnerCompanyName);

                    mTextAgentModel.setText(info.agentModelName);
                    mTextPartnerModel.setText(info.partnerModelName);

                    mTextAgentDate.setText(info.agentReleaseDate);
                    mTextPartnerDate.setText(info.partnerReleaseDate);

                    mTextStateEnum.setText(
                            String.format("Agent State: %s, Partner State: %s", info.agentFotaState, info.partnerFotaState));

                    if (info.agentAudioChannelSetting == 0x01 && info.partnerAudioChannelSetting == 0x02) {
                        mIsAgentLeftPartnerRight = true;
                        mHasDetectAudioChannel = true;
                    } else if(info.agentAudioChannelSetting == 0x02 && info.partnerAudioChannelSetting == 0x01){
                        mIsAgentLeftPartnerRight = false;
                        mHasDetectAudioChannel = true;
                    }
                    if(mHasDetectAudioChannel) {
                        mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(true);
                        mBtnUpdateNvr.setEnabled(true);
                        mTextAudioChannel.setText(
                                String.format("Agent (%s), Partner (%s)", getChannelSettingStr(info.agentAudioChannelSetting), getChannelSettingStr(info.partnerAudioChannelSetting)));
                        mTextOtaError.setText("");
                    }
                    else {
                        mTextAudioChannel.setText(
                                String.format("Agent (%s), Partner (%s)", getChannelSettingStr((byte)0xFF), getChannelSettingStr((byte)0xFF)));

                        mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                        mBtnUpdateNvr.setEnabled(false);
                        mTextOtaError.setText("Detect mode should be in sw mode, and sw setting of agent and partner should have right side and left side each.");
                        mTextOtaError.setTextColor(Color.RED);
                    }
                }
            });

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
        public void onSingleFotaInfoUpdated(SingleFotaInfo info) {

        }
    };

    private String getChannelSettingStr(byte val)
    {
        String rtn = "NOT DEFINED";
        switch(val)
        {
            case 0x00:
                rtn = "AU_DSP_CH_LR";
                break;
            case 0x01:
                rtn = "AU_DSP_CH_L";
                break;
            case 0x02:
                rtn = "AU_DSP_CH_R";
                break;
            case 0x03:
                rtn = "AU_DSP_CH_SWAP";
                break;
            case 0x04:
                rtn = "AU_DSP_CH_MIX";
                break;
            case 0x05:
                rtn = "AU_DSP_CH_MIX_SHIFT";
                break;
        }
        return rtn;

    }

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

    protected void cleanDebugUiMessage() {
        mTextCallBackState.setText("");
        mTextStateEnum.setText("");
        mTextOtaError.setText("");
        mTextAgentVersion.setText("");
        mTextAudioChannel.setText("");
    }

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

    private void initLeftChannelFwFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "ext"};
        mLeftChannelFwFilePickerDialog = new FilePickerDialog(this, properties);
        mLeftChannelFwFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mLeftChannelSelectedFotaBinFileName = files[0].toString();

                    mEditLeftChannelFwBinPath.setText(mLeftChannelSelectedFotaBinFileName);
                }
            }
        });
    }

    private void initRightChannelFwFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "ext"};
        mRightChannelFwFilePickerDialog = new FilePickerDialog(this, properties);
        mRightChannelFwFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mRightChannelSelectedFotaBinFileName = files[0].toString();

                    mEditRightChannelFwBinPath.setText(mRightChannelSelectedFotaBinFileName);
                }
            }
        });
    }

    private void initLeftChannelNvrFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"nvr"};
        mLeftChannelNvrBinFilePickerDialog = new FilePickerDialog(this, properties);
        mLeftChannelNvrBinFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mLeftChannelSelectedNvrBinFileName = files[0].toString();

                    mEditLeftChannelNvrBinPath.setText(mLeftChannelSelectedNvrBinFileName);
                }
            }
        });
    }

    private void initRightChannelNvrFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"nvr"};
        mRightChannelNvrBinFilePickerDialog = new FilePickerDialog(this, properties);
        mRightChannelNvrBinFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {

                    mRightChannelSelectedNvrBinFileName = files[0].toString();

                    mEditRightChannelNvrBinPath.setText(mRightChannelSelectedNvrBinFileName);
                }
            }
        });
    }

    private void initPreferences() {
        SharedPreferences preference = this.getPreferences(Context.MODE_PRIVATE);
//        mTextViewSppAddr.setText(preference.getString("SppAddr", "66:55:44:33:22:11"));
        mEditRespTimeout.setText(preference.getString("RespTimeout", "30000"));
        mEditLongPacketCmdDelay.setText(preference.getString("LongPacketCmdDelay", "50"));
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);

        mBtnLeftChannelFwBinFilePicker = findViewById(R.id.buttonLeftChannelFwBinFilePicker);
        mBtnRightChannelFwBinFilePicker = findViewById(R.id.buttonRightChannelFwBinFilePicker);
        mEditLeftChannelFwBinPath = findViewById(R.id.editTextLeftChannelFwBinPath);
        mEditRightChannelFwBinPath = findViewById(R.id.editTextRightChannelFwBinPath);

        mBtnFileSystemBinFilePicker = findViewById(R.id.buttonFileSystemBinFilePicker);
        mEditFileSystemBinPath = findViewById(R.id.editTextFileSystemBinPath);

        mBtnLeftChannelNvrBinFilePicker = findViewById(R.id.buttonLeftChannelNvrBinFilePicker);
        mBtnRightChannelNvrBinFilePicker = findViewById(R.id.buttonRightChannelNvrBinFilePicker);
        mEditLeftChannelNvrBinPath = findViewById(R.id.editTextLeftChannelNvrBinPath);
        mEditRightChannelNvrBinPath = findViewById(R.id.editTextRightChannelNvrBinPath);

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


        mBtnLeftChannelFwBinFilePicker = findViewById(R.id.buttonLeftChannelFwBinFilePicker);
        mBtnLeftChannelFwBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnLeftChannelFwBinFilePicker");
                mLeftChannelFwFilePickerDialog.show();
            }
        });

        mBtnRightChannelFwBinFilePicker = findViewById(R.id.buttonRightChannelFwBinFilePicker);
        mBtnRightChannelFwBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnRightChannelFwBinFilePicker");
                mRightChannelFwFilePickerDialog.show();
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

        mBtnLeftChannelNvrBinFilePicker = findViewById(R.id.buttonLeftChannelNvrBinFilePicker);
        mBtnLeftChannelNvrBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnLeftChannelNvrBinFilePicker");
                mLeftChannelNvrBinFilePickerDialog.show();
            }
        });

        mBtnRightChannelNvrBinFilePicker = findViewById(R.id.buttonRightChannelNvrBinFilePicker);
        mBtnRightChannelNvrBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: BtnRightChannelNvrBinFilePicker");
                mRightChannelNvrBinFilePickerDialog.show();
            }
        });

        mBtnDownloadFromInternet = findViewById(R.id.buttonDownloadFromInternet);
        mBtnDownloadFromInternet = findViewById(R.id.buttonDownloadFromInternet);
        mBtnDownloadFromInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)view).getText());
                // Make a download request
                configDownloader();

                String urlLeftChannelFota = mEditLeftChannelFwBinPath.getText().toString();
                String urlRightChannelFota = mEditRightChannelFwBinPath.getText().toString();
                String urlFileSystem = mEditFileSystemBinPath.getText().toString();
                String urlLeftChannelNvrBin = mEditLeftChannelNvrBinPath.getText().toString();
                String urlRightChannelNvrBin = mEditRightChannelNvrBinPath.getText().toString();

                if (urlLeftChannelFota != null && !urlLeftChannelFota.isEmpty()) {
                    mSubStrLeftChannelFileFota = urlLeftChannelFota.substring(urlLeftChannelFota.lastIndexOf('/') + 1);
                    mLeftChannelSelectedFotaBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrLeftChannelFileFota).toString();

                    mDownloadedCounter = 1;
                }

                if (urlRightChannelFota != null && !urlRightChannelFota.isEmpty()) {
                    mSubStrRightChannelFileFota = urlRightChannelFota.substring(urlRightChannelFota.lastIndexOf('/') + 1);
                    mRightChannelSelectedFotaBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrRightChannelFileFota).toString();

                    mDownloadedCounter = 2;
                }

                if (urlFileSystem != null && !urlFileSystem.isEmpty()) {
                    mSubStrFileFileSystem = urlFileSystem.substring(urlFileSystem.lastIndexOf('/') + 1);
                    mSelectedFileSystemBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrFileFileSystem).toString();

                    mDownloadedCounter = 3;
                }

                if (urlLeftChannelNvrBin != null && !urlLeftChannelNvrBin.isEmpty()) {
                    mSubStrLeftChannelNvr = urlLeftChannelNvrBin.substring(urlLeftChannelNvrBin.lastIndexOf('/') + 1);
                    mLeftChannelSelectedNvrBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrLeftChannelNvr).toString();

                    mDownloadedCounter = 4;
                }

                if (urlRightChannelNvrBin != null && !urlRightChannelNvrBin.isEmpty()) {
                    mSubStrRightChannelNvr = urlRightChannelNvrBin.substring(urlRightChannelNvrBin.lastIndexOf('/') + 1);
                    mRightChannelSelectedNvrBinFileName = new File(Environment.getExternalStorageDirectory().getPath(), mSubStrRightChannelNvr).toString();

                    mDownloadedCounter = 5;
                }

                switch (mDownloadedCounter) {
                    case 1:
                        makeDownloadRequest(urlLeftChannelFota, mSubStrLeftChannelFileFota);
                        break;
                    case 2:
                        makeDownloadRequest(urlLeftChannelFota, mSubStrLeftChannelFileFota);
                        makeDownloadRequest(urlRightChannelFota, mSubStrRightChannelFileFota);
                        break;
                    case 3:
                        makeDownloadRequest(urlLeftChannelFota, mSubStrLeftChannelFileFota);
                        makeDownloadRequest(urlRightChannelFota, mSubStrRightChannelFileFota);
                        makeDownloadRequest(urlFileSystem, mSubStrFileFileSystem);
                        break;
                    case 4:
                        makeDownloadRequest(urlLeftChannelFota, mSubStrLeftChannelFileFota);
                        makeDownloadRequest(urlRightChannelFota, mSubStrRightChannelFileFota);
                        makeDownloadRequest(urlFileSystem, mSubStrFileFileSystem);
                        makeDownloadRequest(urlLeftChannelNvrBin, mSubStrLeftChannelNvr);
                        break;
                    case 5:
                        makeDownloadRequest(urlLeftChannelFota, mSubStrLeftChannelFileFota);
                        makeDownloadRequest(urlRightChannelFota, mSubStrRightChannelFileFota);
                        makeDownloadRequest(urlFileSystem, mSubStrFileFileSystem);
                        makeDownloadRequest(urlLeftChannelNvrBin, mSubStrLeftChannelNvr);
                        makeDownloadRequest(urlRightChannelNvrBin, mSubStrRightChannelNvr);
                        break;
                }

            }
        });

        mChkDownloadFromInternet = findViewById(R.id.chkDownFromInternet);
        mChkDownloadFromInternet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                mLeftChannelSelectedFotaBinFileName = null;
                mRightChannelSelectedFotaBinFileName = null;
                mSelectedFileSystemBinFileName = null;

                if (!isChecked) {
                    mBtnFileSystemBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnLeftChannelFwBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnRightChannelFwBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnLeftChannelNvrBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnRightChannelNvrBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnDownloadFromInternet.setVisibility(View.GONE);
//                    mBtnFotaStartAll.setVisibility(View.VISIBLE);

                    mEditLeftChannelFwBinPath.setText("");
                    mEditRightChannelFwBinPath.setText("");
                    mEditFileSystemBinPath.setText("");
                    mEditLeftChannelNvrBinPath.setText("");
                    mEditRightChannelNvrBinPath.setText("");

                } else {
                    mBtnFileSystemBinFilePicker.setVisibility(View.GONE);
                    mBtnLeftChannelFwBinFilePicker.setVisibility(View.GONE);
                    mBtnRightChannelFwBinFilePicker.setVisibility(View.GONE);
                    mBtnLeftChannelNvrBinFilePicker.setVisibility(View.GONE);
                    mBtnRightChannelNvrBinFilePicker.setVisibility(View.GONE);
                    mBtnDownloadFromInternet.setVisibility(View.VISIBLE);
//                    mBtnFotaStartAll.setVisibility(View.GONE);

                    mEditLeftChannelFwBinPath.setText(URL_AIROHA_FOTA);
                    mEditRightChannelFwBinPath.setText(URL_AIROHA_FOTA);
                    mEditFileSystemBinPath.setText(URL_AIROHA_FILESYSTEM);
                    mEditLeftChannelNvrBinPath.setText(URL_AIROHA_NVR);
                    mEditRightChannelNvrBinPath.setText(URL_AIROHA_NVR);
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
                    Toast.makeText(Tws153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                cleanDebugUiMessage();


                mBtnQueryPartitionAndState.setEnabled(false);
                mHasDetectAudioChannel = false;

//                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
//                mBtn_153X_RestoreOldFileSystem.setEnabled(false);
//                mBtn_153X_UpdateReconnectNvKey.setEnabled(false);

//                mBtnRoleSwitch.setEnabled(false);
//                mBtnTwsUpdateNvKey.setEnabled(false);

                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                mBtnUpdateNvr.setEnabled(false);

                try {
//                    configOtaFilesPath();
                    int batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mAirohaOtaMgr.queryDualFotaInfo(batteryThreshold);
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
                    mBtnQueryPartitionAndState.setEnabled(true);
                }

            }
        });

        mTextCallBackState = findViewById(R.id.textViewCallBackState);

        mTextStateEnum = findViewById(R.id.textViewStateEnum);
        //mTextVersion = findViewById(R.id.textViewVersion);
        mTextAudioChannel = findViewById(R.id.textViewAudioChannel);

        mTextAgentCompany = findViewById(R.id.textAgentCompany);
        mTextAgentModel = findViewById(R.id.textAgentModel);
        mTextAgentDate = findViewById(R.id.textAgentDate);
        mTextAgentVersion = findViewById(R.id.textAgentVersion);

        mTextPartnerCompany = findViewById(R.id.textPartnerCompany);
        mTextPartnerModel = findViewById(R.id.textPartnerModel);
        mTextPartnerDate = findViewById(R.id.textPartnerDate);
        mTextPartnerVersion = findViewById(R.id.textPartnerVersion);

//        mBtnTwsUpdateNvKey = findViewById(R.id.btn_UpdateNvKey);
//        mBtnTwsUpdateNvKey.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBtnQueryPartitionAndState.setEnabled(false);
////                mAirohaOtaMgr.startTwsUpdateNvkey();
//            }
//        });


        mBtn_153X_155x_StartResumableEraseFota_V2 = findViewById(R.id.btn_153X_155X_StartResumableFota_V2);
        mBtn_153X_155x_StartResumableEraseFota_V2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Tws153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);

                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                mBtnUpdateNvr.setEnabled(false);

                try {
                    mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mFotaSettings.partitionType = PartitionType.Fota;
                    mFotaSettings.actionEnum = DualActionEnum.StartFota;
//                    mAirohaOtaMgr.updatePartnerFotaPartition(mSelectedFotaBinFileName);
                    if(mIsAgentLeftPartnerRight) {
                        mAirohaOtaMgr.startDualFota(mLeftChannelSelectedFotaBinFileName, mRightChannelSelectedFotaBinFileName, mFotaSettings);
                    }
                    else {
                        mAirohaOtaMgr.startDualFota(mRightChannelSelectedFotaBinFileName, mLeftChannelSelectedFotaBinFileName, mFotaSettings);
                    }
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                    mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(true);
                    mBtnUpdateNvr.setEnabled(true);
                }
            }
        });


        mBtn_153X_RestoreOldFileSystem = findViewById(R.id.btn_153X_RestoreOldFileSystem);
        mBtn_153X_RestoreOldFileSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Tws153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);
                mBtn_153X_RestoreOldFileSystem.setEnabled(false);

            }
        });

        mBtn_153X_RestoreNewFileSystem = findViewById(R.id.btn_153X_RestoreNewFileSystem);
        mBtn_153X_RestoreNewFileSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Tws153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnQueryPartitionAndState.setEnabled(false);
                mBtn_153X_RestoreNewFileSystem.setEnabled(false);
                mBtn_153X_155x_StartResumableEraseFota_V2.setEnabled(false);
                mBtnUpdateNvr.setEnabled(false);

                try {
                    mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mFotaSettings.partitionType = PartitionType.FileSystem;
                    mFotaSettings.actionEnum = DualActionEnum.RestoreNewFileSystem;
//                    mAirohaOtaMgr.updatePartnerFotaPartition(mSelectedFotaBinFileName);
                    mAirohaOtaMgr.startDualFota(mSelectedFileSystemBinFileName, mSelectedFileSystemBinFileName, mFotaSettings);
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
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
//                mBtn_153X_UpdateReconnectNvKey.setEnabled(false);
//
//                mAirohaOtaMgr.updateReconnectNvKeyDual();
//            }
//        });

        mBtnUpdateNvr = findViewById(R.id.btnUpdateNvr);
        mBtnUpdateNvr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());

                if(mEditBatteryThreshold.getText().toString().isEmpty()) {
                    Toast.makeText(Tws153xMceActivity.this, "Battery Threshold is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }
//                mAirohaOtaMgr.startUpdateDualNvr(mSelectedNvrBinFileName);

                try {
                    mFotaSettings.batteryThreshold = Integer.parseInt(mEditBatteryThreshold.getText().toString());
                    mFotaSettings.actionEnum = DualActionEnum.UpdateNvr;
                    if(mIsAgentLeftPartnerRight) {
                        mAirohaOtaMgr.startDualFota(mLeftChannelSelectedNvrBinFileName, mRightChannelSelectedNvrBinFileName, mFotaSettings);
                    }
                    else {
                        mAirohaOtaMgr.startDualFota(mRightChannelSelectedNvrBinFileName, mLeftChannelSelectedNvrBinFileName, mFotaSettings);
                    }
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                    mBtnUpdateNvr.setEnabled(true);
                }
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
//                mAirohaOtaMgr.enableLongPacketMode(isChecked);

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
                    mFotaSettings.programInterval = prePollSize;
                } catch (Exception e) {
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        mBtn_Cancel = findViewById(R.id.btn_Cancel);
        mBtn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                mAirohaOtaMgr.cancelDualFota();
            }
        });

        mBtnDebugGetBatteryRelay = findViewById(R.id.btnDebugGetBatteryRelay);
        mBtnDebugGetBatteryRelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                cleanDebugUiMessage();
//                mAirohaOtaMgr.debugGetBatterRelay();
            }
        });

        mBtnDebugFlashControlRelay = findViewById(R.id.btnDebugFlashControlRelay);
        mBtnDebugFlashControlRelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                cleanDebugUiMessage();
//                mAirohaOtaMgr.debugFlashControlRelay();
            }
        });

        mBtnDebugIntegrityCheck = findViewById(R.id.btnDebugIntegrityCheck);
        mBtnDebugIntegrityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
                cleanDebugUiMessage();
                mAirohaOtaMgr.testDualIntegrityCheck();
            }
        });

        mBtnDualCommit = findViewById(R.id.btn_153X_DualCommit);
        mBtnDualCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
//                mAirohaOtaMgr.dualCommit();
            }
        });

        mBtnDualReset = findViewById(R.id.btn_153X_DualReset);
        mBtnDualReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.logToFile(TAG, "onClick: " + ((Button)v).getText());
//                mAirohaOtaMgr.dualSoftReset();
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

                mTextOtaStatus.setText("Download Complete:" + fileName);

                mDownloadedCounter++;

                if (mDownloadedCounter == 1) {
                    mTextOtaStatus.setText("Download Complete. Start to query parition and state");

                    mDownloadedCounter = 0;

//                    configOtaFilesPath();
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
        setContentView(R.layout.activity_tws_main);

        this.setTitle("FOTA - MCSync");

        initUImember();

        requestExternalStoragePermission();

        initLeftChannelFwFileDialog();
        initRightChannelFwFileDialog();
        initFileSystemFileDialog();
        initLeftChannelNvrFileDialog();
        initRightChannelNvrFileDialog();

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnRespTimeoutListener(TAG, mOnAirohaRespTimeoutListener);

        mAirohaOtaMgr = new Airoha153xMceRaceOtaMgr(mAirohaLink);
        mAirohaOtaMgr.registerListener(TAG, mAppListener);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mA2dpListener, intentFilter);

        initPreferences();

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

    @Override
    protected void onResume() {
        super.onResume();

        updatePairedList();
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
        editor.putString("SppAddr", mTextViewSppAddr.getText().toString());
        editor.putString("RespTimeout", mEditRespTimeout.getText().toString());
        editor.putString("LongPacketCmdDelay", mEditLongPacketCmdDelay.getText().toString());
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
