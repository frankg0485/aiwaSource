package com.airoha.ab153x;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.peq.AirohaPeqMgr;
import com.airoha.android.lib.peq.PeqBandInfo;
import com.airoha.android.lib.peq.PeqUiDataStru;
import com.airoha.android.lib.peq.UserInputConstraint;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.PacketParser.OnAirohaRespTimeoutListener;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.util.List;
import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class PeqUtActivity extends AppCompatActivity {

    private static final String TAG = "AirohaUT";

    private AirohaLink mAirohaLink = null;
    private Context mCtx;

    private AirohaPeqMgr mAirohaPeqMgr;

    // Connect
    private TextView mTextViewSppAddr;
    protected Button mBtnConSpp;
    protected Button mBtnDisConSpp;
    protected TextView mTextConSppResult;
    protected TextView mTextConSppState;

    private TextView mTextErrMsg;

    // paired list
    protected ListView mPairedListView;
    protected ArrayAdapter<String> mPairedDevicesArrayAdapter;

    protected SeekBar mSeekBarGain0;
    protected SeekBar mSeekBarGain1;
    protected SeekBar mSeekBarGain2;
    protected SeekBar mSeekBarGain3;
    protected SeekBar mSeekBarGain4;
    protected SeekBar mSeekBarGain5;
    protected SeekBar mSeekBarGain6;
    protected SeekBar mSeekBarGain7;
    protected SeekBar mSeekBarGain8;
    protected SeekBar mSeekBarGain9;

    protected EditText mEditTextGain0;
    protected EditText mEditTextGain1;
    protected EditText mEditTextGain2;
    protected EditText mEditTextGain3;
    protected EditText mEditTextGain4;
    protected EditText mEditTextGain5;
    protected EditText mEditTextGain6;
    protected EditText mEditTextGain7;
    protected EditText mEditTextGain8;
    protected EditText mEditTextGain9;

    protected EditText mEditTextFreq0;
    protected EditText mEditTextFreq1;
    protected EditText mEditTextFreq2;
    protected EditText mEditTextFreq3;
    protected EditText mEditTextFreq4;
    protected EditText mEditTextFreq5;
    protected EditText mEditTextFreq6;
    protected EditText mEditTextFreq7;
    protected EditText mEditTextFreq8;
    protected EditText mEditTextFreq9;

    protected EditText mEditTextBw0;
    protected EditText mEditTextBw1;
    protected EditText mEditTextBw2;
    protected EditText mEditTextBw3;
    protected EditText mEditTextBw4;
    protected EditText mEditTextBw5;
    protected EditText mEditTextBw6;
    protected EditText mEditTextBw7;
    protected EditText mEditTextBw8;
    protected EditText mEditTextBw9;

    // interaction
    protected Button mBtnLoadUiData;

    protected Button mBtnUpdateRealTimePEQ;

    protected Button mBtnSavePeqCoef;

    protected Button mBtnUpdatePeqUiData;

    protected Button mBtnLazyForTest;

    private Button mBtnBandTotal1;
    private Button mBtnBandTotal2;
    private Button mBtnBandTotal3;
    private Button mBtnBandTotal4;
    private Button mBtnBandTotal5;
    private Button mBtnBandTotal6;
    private Button mBtnBandTotal7;
    private Button mBtnBandTotal8;
    private Button mBtnBandTotal9;
    private Button mBtnBandTotal10;


    private EditText[] mEditFreqs;
    private SeekBar[] mSeekBars;
    private EditText[] mEditGains;
    private EditText[] mEditBws;
    private Button[] mBtnBandTotals;

    private final double PROGRESS_STEP = 0.01;

    private int mBandTotals = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pequt);
        setTitle("PEQ UT");

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnRespTimeoutListener(TAG, mOnAirohaRespTimeoutListener);

        mAirohaPeqMgr = new AirohaPeqMgr(mAirohaLink, mOnPeqStatusUiListener);

        initUImember();

        updatePairedList();


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

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mAirohaPeqMgr.preInitToSpeedUp();
//            }
//        }).start();
    }

    private void setUIMessage(String msg) {
        mTextErrMsg.setText(msg);
    }

    private OnAirohaRespTimeoutListener mOnAirohaRespTimeoutListener = new OnAirohaRespTimeoutListener() {
        @Override
        public void OnRespTimeout() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(mCtx, "Timeout. FW not responding", Toast.LENGTH_SHORT).show();
                    setUIMessage("Timeout. FW not responding.");
                }
            });
        }
    };

    private AirohaPeqMgr.OnPeqStatusUiListener mOnPeqStatusUiListener = new AirohaPeqMgr.OnPeqStatusUiListener() {
        @Override
        public void OnActionCompleted(final AirohaPeqMgr.Action action) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUIMessage("Action Completed: " + action.name());
                    //Toast.makeText(mCtx, "Action Completed: " + action.name(), Toast.LENGTH_SHORT).show();

                    if(action == AirohaPeqMgr.Action.LoadUiData){
                        mBtnUpdateRealTimePEQ.setEnabled(true);
                    }

                    if(action == AirohaPeqMgr.Action.RealTimeUpdate){
                        mBtnSavePeqCoef.setEnabled(true);
                        mBtnUpdatePeqUiData.setEnabled(true);
                    }
                }
            });

            if(action == AirohaPeqMgr.Action.SaveCoef) {
                // auto save ui data
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mAirohaPeqMgr.savePeqUiData(1, genRealTimeUiData(), AirohaPeqMgr.TargetDeviceEnum.DUAL);
                        } catch (IllegalArgumentException e) {
                            setUIMessage(e.getMessage());
                        }
                    }
                }).start();
            }
        }

        @Override
        public void OnActionError(final AirohaPeqMgr.Action action) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(mCtx, "Action Error: " + action.name(), Toast.LENGTH_SHORT).show();
                    setUIMessage("Action Error: " + action.name());
                    if(action == AirohaPeqMgr.Action.SaveCoef){
                        setUIMessage("Action " + AirohaPeqMgr.Action.RealTimeUpdate + "should be executed first");
                        //Toast.makeText(mCtx, "Action " + AirohaPeqMgr.Action.RealTimeUpdate + "should be executed first", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void OnLoadPeqUiData(final PeqUiDataStru peqUiDataStru) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    giveDefaultInputParam();

                    int enabledBandNum = 0;

                    if(peqUiDataStru == null){
                        setUIMessage("No User-defined data has been set, loading default");
                        //Toast.makeText(mCtx, "No User-defined data has been set, loading default", Toast.LENGTH_SHORT).show();
                        //return;
                        enabledBandNum = 10;
                    } else {

                        List<PeqBandInfo> peqBandInfoList = peqUiDataStru.getPeqBandInfoList();

                        mAirohaLink.logToFile(TAG, "peqBandInfoList size: " + peqBandInfoList.size());

                        // set from the packet
                        for (int i = 0; i < peqBandInfoList.size(); i++) {
                            PeqBandInfo peqBandInfo = peqBandInfoList.get(i);

                            mEditFreqs[i].setText(String.valueOf(peqBandInfo.getFreq()));
                            mEditGains[i].setText(String.valueOf(peqBandInfo.getGain()));
                            mEditBws[i].setText(String.valueOf(peqBandInfo.getBw()));

                            if (peqBandInfo.isEnable()) {
                                enabledBandNum += 1;
                            }
                        }
                    }

                    // clear UI
                    for(int i = 0; i< enabledBandNum; i++){
//                        mEditFreqs[i].setText("");
                        mEditFreqs[i].setEnabled(true);
//                        mEditGains[i].setText("");
                        mEditGains[i].setEnabled(true);
//                        mEditBws[i].setText("");
                        mEditBws[i].setEnabled(true);
                        mSeekBars[i].setEnabled(true);
                    }

                    // enable band total control
                    for (int i = 0; i< 10; i++) {
                        mBtnBandTotals[i].setEnabled(true);
                    }

                    for (int i = 0; i< 10; i++) {
                        mBtnBandTotals[i].setTextColor(Color.BLACK);
                    }

                    mBtnBandTotals[enabledBandNum - 1].setTextColor(Color.RED);

                    mBandTotals = enabledBandNum;
                }
            });
        }
    };


    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);
        mTextViewSppAddr = findViewById(R.id.textViewSppAddr);
        mTextErrMsg = findViewById(R.id.textErrMsg);

        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mTextViewSppAddr.getText().toString();

                try {
                    Boolean result = mAirohaLink.connect(btaddr);
                    mTextConSppResult.setText(result.toString());
                } catch (Exception e) {
                    setUIMessage(e.getMessage());
                    //Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.disconnect();
            }
        });


        mBtnLoadUiData = findViewById(R.id.buttonLoadUiData);
        mBtnLoadUiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            mAirohaPeqMgr.loadPeqUiData(1, AirohaPeqMgr.TargetDeviceEnum.AGENT);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        mBtnUpdateRealTimePEQ = findViewById(R.id.buttonUpdateRealtimePEQ);
        mBtnUpdateRealTimePEQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try{
                            mAirohaPeqMgr.startRealtimeUpdate(genRealTimeUiData());
                        } catch (final Exception e){
                            e.printStackTrace();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    setUIMessage(e.getMessage());
                                    mBtnSavePeqCoef.setEnabled(false);
                                }
                            });
                        }

                    }
                }).start();

            }
        });

        mBtnSavePeqCoef = findViewById(R.id.buttonSavePeqCoef);
        mBtnSavePeqCoef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAirohaPeqMgr.savePeqCoef(1, AirohaPeqMgr.TargetDeviceEnum.DUAL);
                } catch (IllegalArgumentException e) {

                }
            }
        });

        mBtnUpdatePeqUiData = findViewById(R.id.buttonUpdatePeqUiData);
        mBtnUpdatePeqUiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAirohaPeqMgr.savePeqUiData(1, genRealTimeUiData(), AirohaPeqMgr.TargetDeviceEnum.DUAL);
                } catch (IllegalArgumentException e) {

                }
            }
        });

        configUiInputGroup();

        configOnClickBtnLazeForTest();

        configFreqsTextChangedListener();
        configSeekBarChangedListener();
        configGainsTextChangedListener();
        configBandTotalsChangedListener();

        disableAllButtons();
    }

    private void disableAllButtons() {
        mBtnLoadUiData.setEnabled(false);
        mBtnUpdateRealTimePEQ.setEnabled(false);
        mBtnSavePeqCoef.setEnabled(false);
        mBtnUpdatePeqUiData.setEnabled(false);

        for(EditText editText : mEditFreqs) {
            editText.setEnabled(false);
            editText.setText("");
        }

        for(EditText editText : mEditGains) {
            editText.setEnabled(false);
            editText.setText("");
        }

        for(EditText editText : mEditBws) {
            editText.setEnabled(false);
            editText.setText("");
        }

        for(SeekBar seekBar : mSeekBars) {
            seekBar.setEnabled(false);
        }

        for(Button button : mBtnBandTotals) {
            button.setEnabled(false);
        }
    }

    private void configSeekBarChangedListener() {
        for (int i = 0; i < mSeekBars.length; i++) {

            final EditText editGain = mEditGains[i];

            mSeekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    double value = convertProgressBar(progress);

                    editGain.setText(String.format("%2.2f", value));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    private void configGainsTextChangedListener() {
        for (int i = 0; i < mEditGains.length; i++) {
            final SeekBar seekBar = mSeekBars[i];
            mEditGains[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        double val = Double.valueOf(editable.toString());
                        seekBar.setMax(getGainProgressMax());
                        seekBar.setProgress(covertToProgress(val));
                    } catch (Exception e) {

                    }

                }
            });
        }
    }

    private void configBandTotalsChangedListener() {
        for(int i = 0; i < mBtnBandTotals.length; i++) {

            final int idx = i;
            mBtnBandTotals[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(Button button : mBtnBandTotals){
                        button.setTextColor(Color.BLACK);
                    }

                    ((Button)v).setTextColor(Color.RED);

                    mBandTotals = idx + 1;
                    for(int j = 0; j< mEditFreqs.length; j++){
                        mEditFreqs[j].setEnabled(false);
                        mEditGains[j].setEnabled(false);
                        mSeekBars[j].setEnabled(false);
                        mEditBws[j].setEnabled(false);
                    }

                    for(int j = 0; j<=idx; j++){
                        mEditFreqs[j].setEnabled(true);
                        mEditGains[j].setEnabled(true);
                        mSeekBars[j].setEnabled(true);
                        mEditBws[j].setEnabled(true);
                    }
                }
            });
        }
    }

    private double convertProgressBar(int progress) {
        return UserInputConstraint.GAIN_MIN + progress * PROGRESS_STEP;
    }

    private int covertToProgress(double d){
        return (int) ((d - UserInputConstraint.GAIN_MIN)/ PROGRESS_STEP);
    }

    private void configFreqsTextChangedListener() {
        for (int i = 0; i < mEditFreqs.length; i++) {
            final SeekBar seekBar = mSeekBars[i];
            final EditText editBw = mEditBws[i];
            mEditFreqs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int v, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int v, int i1, int i2) {
                    try {
                        double val = Double.valueOf(charSequence.toString());
                        editBw.setText(String.format("%1.2f", val / 2));
                    } catch (NumberFormatException e) {
                        // don't care
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    private int getGainProgressMax() {

        int max = UserInputConstraint.GAIN_MAX;//UserInputConstraint.getGainDbRangeMax(inputFreq);
        int min = UserInputConstraint.GAIN_MIN;//UserInputConstraint.getGainDbRangeMin(inputFreq);

        return (int) ((max - min) / PROGRESS_STEP);
    }

    private void configUiInputGroup() {
        mEditTextFreq0 = findViewById(R.id.editTextFreq0);
        mEditTextFreq1 = findViewById(R.id.editTextFreq1);
        mEditTextFreq2 = findViewById(R.id.editTextFreq2);
        mEditTextFreq3 = findViewById(R.id.editTextFreq3);
        mEditTextFreq4 = findViewById(R.id.editTextFreq4);
        mEditTextFreq5 = findViewById(R.id.editTextFreq5);
        mEditTextFreq6 = findViewById(R.id.editTextFreq6);
        mEditTextFreq7 = findViewById(R.id.editTextFreq7);
        mEditTextFreq8 = findViewById(R.id.editTextFreq8);
        mEditTextFreq9 = findViewById(R.id.editTextFreq9);

        mEditTextGain0 = findViewById(R.id.editTextGain0);
        mEditTextGain1 = findViewById(R.id.editTextGain1);
        mEditTextGain2 = findViewById(R.id.editTextGain2);
        mEditTextGain3 = findViewById(R.id.editTextGain3);
        mEditTextGain4 = findViewById(R.id.editTextGain4);
        mEditTextGain5 = findViewById(R.id.editTextGain5);
        mEditTextGain6 = findViewById(R.id.editTextGain6);
        mEditTextGain7 = findViewById(R.id.editTextGain7);
        mEditTextGain8 = findViewById(R.id.editTextGain8);
        mEditTextGain9 = findViewById(R.id.editTextGain9);

        mSeekBarGain0 = findViewById(R.id.seekBar0);
        mSeekBarGain1 = findViewById(R.id.seekBar1);
        mSeekBarGain2 = findViewById(R.id.seekBar2);
        mSeekBarGain3 = findViewById(R.id.seekBar3);
        mSeekBarGain4 = findViewById(R.id.seekBar4);
        mSeekBarGain5 = findViewById(R.id.seekBar5);
        mSeekBarGain6 = findViewById(R.id.seekBar6);
        mSeekBarGain7 = findViewById(R.id.seekBar7);
        mSeekBarGain8 = findViewById(R.id.seekBar8);
        mSeekBarGain9 = findViewById(R.id.seekBar9);

        mEditTextBw0 = findViewById(R.id.editTextBw0);
        mEditTextBw1 = findViewById(R.id.editTextBw1);
        mEditTextBw2 = findViewById(R.id.editTextBw2);
        mEditTextBw3 = findViewById(R.id.editTextBw3);
        mEditTextBw4 = findViewById(R.id.editTextBw4);
        mEditTextBw5 = findViewById(R.id.editTextBw5);
        mEditTextBw6 = findViewById(R.id.editTextBw6);
        mEditTextBw7 = findViewById(R.id.editTextBw7);
        mEditTextBw8 = findViewById(R.id.editTextBw8);
        mEditTextBw9 = findViewById(R.id.editTextBw9);

        mBtnBandTotal1 = findViewById(R.id.btnBandTotal1);
        mBtnBandTotal2 = findViewById(R.id.btnBandTotal2);
        mBtnBandTotal3 = findViewById(R.id.btnBandTotal3);
        mBtnBandTotal4 = findViewById(R.id.btnBandTotal4);
        mBtnBandTotal5 = findViewById(R.id.btnBandTotal5);
        mBtnBandTotal6 = findViewById(R.id.btnBandTotal6);
        mBtnBandTotal7 = findViewById(R.id.btnBandTotal7);
        mBtnBandTotal8 = findViewById(R.id.btnBandTotal8);
        mBtnBandTotal9 = findViewById(R.id.btnBandTotal9);
        mBtnBandTotal10 = findViewById(R.id.btnBandTotal10);

        mEditFreqs = new EditText[]{
                mEditTextFreq0, mEditTextFreq1, mEditTextFreq2, mEditTextFreq3, mEditTextFreq4,
                mEditTextFreq5, mEditTextFreq6, mEditTextFreq7, mEditTextFreq8, mEditTextFreq9
        };

        mSeekBars = new SeekBar[]{
                mSeekBarGain0, mSeekBarGain1, mSeekBarGain2, mSeekBarGain3, mSeekBarGain4,
                mSeekBarGain5, mSeekBarGain6, mSeekBarGain7, mSeekBarGain8, mSeekBarGain9
        };

        mEditGains = new EditText[]{
                mEditTextGain0, mEditTextGain1, mEditTextGain2, mEditTextGain3, mEditTextGain4,
                mEditTextGain5, mEditTextGain6, mEditTextGain7, mEditTextGain8, mEditTextGain9
        };

        mEditBws = new EditText[]{
                mEditTextBw0, mEditTextBw1, mEditTextBw2, mEditTextBw3, mEditTextBw4,
                mEditTextBw5, mEditTextBw6, mEditTextBw7, mEditTextBw8, mEditTextBw9
        };

        mBtnBandTotals = new Button[]{
                mBtnBandTotal1, mBtnBandTotal2, mBtnBandTotal3, mBtnBandTotal4, mBtnBandTotal5,
                mBtnBandTotal6, mBtnBandTotal7, mBtnBandTotal8, mBtnBandTotal9, mBtnBandTotal10,
        };
    }

    private void configOnClickBtnLazeForTest() {
        mBtnLazyForTest = findViewById(R.id.buttonLazyForTest);
        mBtnLazyForTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveDefaultInputParam();
            }
        });
    }

    private void giveDefaultInputParam() {
        mEditTextFreq0.setText("26");
        mEditTextFreq1.setText("50");
        mEditTextFreq2.setText("110");
        mEditTextFreq3.setText("200");
        mEditTextFreq4.setText("400");
        mEditTextFreq5.setText("800");
        mEditTextFreq6.setText("1600");
        mEditTextFreq7.setText("3200");
        mEditTextFreq8.setText("6400");
        mEditTextFreq9.setText("12800");

        for (int i = 0; i < mEditGains.length; i++) {
            mEditGains[i].setText("0.00");
        }
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    setUIMessage("Connected");
                    mTextConSppState.setText("Conn. :" + type);

                    mBtnLoadUiData.setEnabled(true);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(100);
                        mAirohaPeqMgr.loadPeqUiData(1, AirohaPeqMgr.TargetDeviceEnum.AGENT);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "DisConnected", Toast.LENGTH_SHORT).show();
                    setUIMessage("DisConnected");
                    mTextConSppState.setText("DisConn.");

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
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];
            mTextViewSppAddr.setText(lastdevice.getAddress());

            ParcelUuid[] parcelUuids = lastdevice.getUuids();

            for (ParcelUuid parcelUuid : parcelUuids) {
                Log.d(TAG, parcelUuid.toString());

                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                    Log.d(TAG, "found Airoha device");

                    setUIMessage("Found Airoha Device:" + lastdevice.getName());
                    //Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_LONG).show();

//                    Boolean result = mAirohaLink.connect(lastdevice.getAddress());
//                    mTextConSppResult.setText(result.toString());
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            setUIMessage(e.getMessage());
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private PeqUiDataStru genRealTimeUiData() {
        // currently only support 7 bands
        PeqBandInfo[] bandInfoStrus = new PeqBandInfo[mBandTotals];//new BandInfoStru[10];

        Log.d(TAG, "bandInfoStrus length: " + bandInfoStrus.length);

        for (int i = 0; i < bandInfoStrus.length; i++) {
            float freq = Float.valueOf(mEditFreqs[i].getText().toString());
            float bw = Float.valueOf(mEditBws[i].getText().toString());
            float gain = Float.valueOf(mEditGains[i].getText().toString());

            bandInfoStrus[i] = new PeqBandInfo(freq, bw, gain);
        }

        PeqUiDataStru peqUserInputStru2018 = new PeqUiDataStru(bandInfoStrus);

        return peqUserInputStru2018;
    }

    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();

        super.onDestroy();
    }
}