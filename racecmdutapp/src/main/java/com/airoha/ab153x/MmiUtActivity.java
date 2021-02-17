package com.airoha.ab153x;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.airoha.android.lib.fota.AgentClientEnum;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.OnAirohaMmiClientAppListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.util.ArrayList;
import java.util.List;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class MmiUtActivity extends AppCompatActivity {

    private static final String TAG = "MmiUtActivity";

    private AirohaLink mAirohaLink = null;
    private Context mCtx;

    private List<View> mViewList = new ArrayList<>();

    private RadioButton mRadioButtonNormalMode;
    private RadioButton mRadioButtonGameMode;
    private TextView mTextViewSppAddr;
    private Button mBtnReset;
    private boolean mIsPartnerExist = false;

    // Mmi Mgr
    private AirohaMmiMgr mMmiMgr;

    private AlertDialog mAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmi);
        this.setTitle("MMI UT");

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);

        mMmiMgr = new AirohaMmiMgr(mAirohaLink);
        mMmiMgr.registerMmiClientAppListener(TAG, mMmiClientAppListener);

        initUImember();

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

    private OnAirohaMmiClientAppListener mMmiClientAppListener = new OnAirohaMmiClientAppListener() {
        @Override
        public void OnRespSuccess(final String stageName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx,stageName + ", Success.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnFindMeState(final byte state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "OnFindMeState:" + String.format("%02X", state), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnBattery(final byte role, final byte level) {

        }

        @Override
        public void OnAncTurnOn(final byte status) {
            if (status != 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(mCtx, "Error", "OnAncTurnOn status = " + status);
                    }
                });
            }
        }

        @Override
        public void OnPassThrough(final byte status) {
            if (status != 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(mCtx, "Error", "OnPassThrough status = " + status);
                    }
                });
            }
        }

        @Override
        public void OnAncTurnOff(final byte status) {
            if (status != 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(mCtx, "Error", "OnAncTurnOff status = " + status);
                    }
                });
            }
        }

        @Override
        public void OnGameModeStateChanged(final boolean isEnabled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isEnabled) {
                        mRadioButtonGameMode.setChecked(true);
                    } else {
                        mRadioButtonNormalMode.setChecked(true);
                    }
                    enableAllButtons();
                }
            });
        }

        @Override
        public void notifyAgentIsRight(final boolean isRight) {

        }

        @Override
        public void notifyPartnerIsExisting(final boolean isExisting) {
            mIsPartnerExist = isExisting;

            Handler handler = new Handler(mAirohaLink.getContext().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMmiMgr.getGameModeState();
                }
            }, 100);
        }

        @Override
        public void notifyQueryVpLanguage(final List<String> vpList) {

        }

        @Override
        public void notifyGetVpIndex(final byte index) {

        }

        @Override
        public void notifySetVpIndex(final boolean status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(status)
                        Toast.makeText(mCtx, "Pass", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mCtx, "Fail", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void notifyAncStatus(final byte status) {

        }

        @Override
        public void notifyGameModeState(final byte state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean isGameMode = state == 0x01? true:false;
                    if (isGameMode) {
                        mRadioButtonGameMode.setChecked(true);
                    } else {
                        mRadioButtonNormalMode.setChecked(true);
                    }
                    enableAllButtons();
                }
            });
        }

        @Override
        public void notifyResetState(final byte role, byte state) {
        }
    };

    void initUImember() {
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);

        mRadioButtonNormalMode = findViewById(R.id.radioButton_mmi_normal_mode);
        mRadioButtonGameMode = findViewById(R.id.radioButton_mmi_game_mode);
        mRadioButtonNormalMode.setOnClickListener(mOnClickListener);
        mRadioButtonGameMode.setOnClickListener(mOnClickListener);
        addToViewList(mRadioButtonNormalMode);
        addToViewList(mRadioButtonGameMode);

        mBtnReset = findViewById(R.id.btnReset);
        mBtnReset.setOnClickListener(mOnClickListener);
        addToViewList(mBtnReset);

        disableAllButtons();
    }

    private CompoundButton.OnClickListener mOnClickListener = new CompoundButton.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.radioButton_mmi_normal_mode:
                    if (((RadioButton)v).isChecked()){
                        disableAllButtons();
                        mMmiMgr.setGameModeState(false);
                    }
                    break;
                case R.id.radioButton_mmi_game_mode:
                    if (((RadioButton)v).isChecked()){
                        disableAllButtons();
                        mMmiMgr.setGameModeState(true);
                    }
                    break;
                case R.id.btnReset:
                    mMmiMgr.reset(true, mIsPartnerExist);
                    break;
            }
        }
    };

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    mMmiMgr.checkPartnerExistence();
                    enableAllButtons();
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "DisConnected", Toast.LENGTH_SHORT).show();

                    resetUiText();

                    disableAllButtons();
                }
            });
        }

        @Override
        public void OnConnecting() {

        }

        @Override
        public void OnDisConnecting() {

        }

        @Override
        public void OnConnectionTimeout() { }

        @Override
        public void OnUnexpectedDisconnected() { }
    };

    private void resetUiText() {

    }

    private void addToViewList(Button button){
        mViewList.add(button);
    }

    private void enableAllButtons(){
        for(View v : mViewList){
            v.setEnabled(true);
        }
    }

    private void disableAllButtons(){
        for(View v : mViewList){
            v.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();
        super.onDestroy();
    }

    public void showAlertDialog(final Context context, final String title, final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        });
    }
}
