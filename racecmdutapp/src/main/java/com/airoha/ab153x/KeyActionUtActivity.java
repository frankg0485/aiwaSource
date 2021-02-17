package com.airoha.ab153x;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airoha.android.lib.KeyActionUT.AirohaKeyMapManager;
import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.airoha.ab153x.MenuActivity.EXTRAS_DEVICE_ADDRESS;

public class KeyActionUtActivity extends AppCompatActivity {

    private static final String TAG = "KeyActionUtActivity";

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

    // Interaction
    private Button mBtnCheckChannel;
    private TextView mTextAgentChannel;
    private TextView mTextPartnerChannel;

    private Spinner mSpinLeftDoubleClickKeyAction;
    private Spinner mSpinLeftTripleClickKeyAction;
    private Spinner mSpinLeftLongPressKeyAction;
    private Button mBtnSetLeftKepMap;

    private Spinner mSpinRightDoubleClickKeyAction;
    private Spinner mSpinRightTripleClickKeyAction;
    private Spinner mSpinRightLongPressKeyAction;
    private Button mBtnSetRightKepMap;
    private List<View> mViewList = new ArrayList<>();

    // Mmi Mgr
    private AirohaMmiMgr mMmiMgr;
    private AirohaKeyMapManager mKeyMapMgr;
    private AlertDialog mAlertDialog = null;

    //log list
    protected ListView mLogView;
    public static ArrayAdapter<String> gLogAdapter;

    private HashMap<String, String> mLeftKeyMap;
    private HashMap<String, String> mRightKeyMap;
    private boolean mAgentIsRight = true;
    private boolean mIsPartnerExist = false;

    private static String FORWARD_HEXSTR = "05005A0008000000";
    private static String ANC_AND_PASSTHROUGH_HEXSTR = "050094000E000000";
    private static String BACKWARD_HEXSTR = "06005B0008000000";
    private static String VOICE_ASSISTANT_HEXSTR = "0300330008000001";

    private int mRetryCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keymaput);
        this.setTitle("Key Action UT");

        mCtx = this;

        mAirohaLink = new AirohaLink(this);
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);

        mKeyMapMgr = new AirohaKeyMapManager(mAirohaLink, mOnStatusUiListener);

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

    private AirohaKeyMapManager.OnStatusUiListener mOnStatusUiListener = new AirohaKeyMapManager.OnStatusUiListener() {
        @Override
        public synchronized void OnActionCompleted(final int raceId, final byte[] packet, final int raceType, final boolean isAgent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(raceId ==0x0A00)
                    {
                        byte[] val = mKeyMapMgr.getReadNvKeyEvent("0xF2B5", isAgent);
                        if(val != null){
                            if(isAgent && val[9] == 0x01){
                                mTextAgentChannel.setText("Left");
                                addLogMsg("Agent is Left.");
                            }
                            else if(isAgent && val[9] == 0x02){
                                mTextAgentChannel.setText("Right");
                                addLogMsg("Agent is Right.");
                            }
                            else if(!isAgent && val[9] == 0x01){
                                mTextPartnerChannel.setText("Left");
                                addLogMsg("Partner is Left.");
                            }else if(!isAgent && val[9] == 0x02){
                                mTextPartnerChannel.setText("Right");
                                addLogMsg("Partner is Right.");
                            }
                            mKeyMapMgr.removeReadNvKeyEvent("0xF2B5", isAgent);
                            return;
                        }

                        val = mKeyMapMgr.getReadNvKeyEvent("0xF2E7", isAgent);
                        if(val != null){
                            byte[] pkt = new byte[packet.length - 8];
                            System.arraycopy(packet, 8, pkt, 0, packet.length - 8);
                            String unmatch_key = parseKeyMap(Converter.byteArrayToHexString(pkt), !(isAgent ^ mAgentIsRight));

                            if (unmatch_key.length() > 0) {
                                addLogMsg("There exists unmatch key action: " + unmatch_key);
                            }
                            mKeyMapMgr.removeReadNvKeyEvent("0xF2E7", isAgent);
                            return;
                        }
                    }
                    else if(raceId ==0x0A01)
                    {
                        if(packet.length < 7){
                            return;
                        }
                        if(packet[6] == 0){
                            addLogMsg("Write nvkey completed.");
                        }
                        else{
                            addLogMsg("Write nvkey failed, status=" + String.format("%02X",packet[6]));
                        }
                    }
                    else if(raceId ==0x0A09)
                    {
                        if(packet.length < 7){
                            return;
                        }
                        if(packet[6] == 0){
                            addLogMsg("Reload nvkey completed.");
                        }
                        else{
                            addLogMsg("Reload nvkey failed, status=" + String.format("%02X",packet[6]));
                        }
                    }
                    else if(raceId ==0x0D00){
                        List<Dst> dstList = new ArrayList<>();
                        for (int i = RacePacket.IDX_PAYLOAD_START; i < packet.length - 1; i = i + 2) {
                            Dst dst = new Dst();
                            dst.Type = packet[i];
                            dst.Id = packet[i + 1];
                            dstList.add(dst);
                        }

                        Dst awsPeerDst = null;
                        for (Dst dst : dstList) {
                            if (dst.Type == AvailabeDst.RACE_CHANNEL_TYPE_AWSPEER) {
                                awsPeerDst = dst;
                                break;
                            }
                        }

                        if (awsPeerDst == null) {
                            mIsPartnerExist = false;
                            mTextPartnerChannel.setText("NA");
                            mRetryCnt++;
                            if(mRetryCnt >= 4){
                                mRetryCnt = 0;
                                addLogMsg("Partner doesn't exist.");
                                mKeyMapMgr.checkChannelNgetKeyMap(true, mIsPartnerExist);
                            }
                        }
                        else {
                            // pass to manager
                            mKeyMapMgr.setAwsPeerDst(awsPeerDst);
                            mIsPartnerExist = true;
                            mKeyMapMgr.checkChannelNgetKeyMap(true, mIsPartnerExist);
                        }
                        Log.d(TAG, "mIsPartnerExist = " + mIsPartnerExist);
                    }
                }
            });
        }
    };

    private String parseKeyMap(String pkt, boolean is_right){
        String str = pkt.toUpperCase();
        HashMap<String, String> map = new HashMap<>();

        if(str.contains(FORWARD_HEXSTR)){
            str = str.replace(FORWARD_HEXSTR, "");
            map.put("Double_Click", "FORWARD");
        }
        else if(str.contains(ANC_AND_PASSTHROUGH_HEXSTR)){
            str = str.replace(ANC_AND_PASSTHROUGH_HEXSTR, "");
            map.put("Double_Click", "ANC_AND_PASSTHROUGH");
        }
        if(str.contains(BACKWARD_HEXSTR)){
            str = str.replace(BACKWARD_HEXSTR, "");
            map.put("Triple_Click", "BACKWARD");
        }
        if(str.contains(VOICE_ASSISTANT_HEXSTR)){
            str = str.replace(VOICE_ASSISTANT_HEXSTR, "");
            map.put("Long_Press", "VOICE_ASSISTANT");
        }
        if(is_right){
            mRightKeyMap = map;
        }
        else{
            mLeftKeyMap = map;
        }
        updateKeyMapUI(is_right);
        return str;
    }

    private void updateKeyMapUI(boolean is_right) {
        for (HashMap.Entry<String, String> entry : ((is_right)?mRightKeyMap:mLeftKeyMap).entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Double_Click")) {
                if (entry.getValue().equalsIgnoreCase("FORWARD")) {
                    ((is_right) ? mSpinRightDoubleClickKeyAction : mSpinLeftDoubleClickKeyAction).setSelection(0);
                } else if (entry.getValue().equalsIgnoreCase("ANC_AND_PASSTHROUGH")) {
                    ((is_right) ? mSpinRightDoubleClickKeyAction : mSpinLeftDoubleClickKeyAction).setSelection(1);
                } else if (entry.getValue().equalsIgnoreCase("GSOUND_NOTIFY")) {
                    ((is_right) ? mSpinRightDoubleClickKeyAction : mSpinLeftDoubleClickKeyAction).setSelection(2);
                }
            } else if (entry.getKey().equalsIgnoreCase("Triple_Click")) {
                if (entry.getValue().equalsIgnoreCase("BACKWARD")) {
                    ((is_right) ? mSpinRightTripleClickKeyAction : mSpinLeftTripleClickKeyAction).setSelection(0);
                } else if (entry.getValue().equalsIgnoreCase("GSOUND_CANCEL")) {
                    ((is_right) ? mSpinRightTripleClickKeyAction : mSpinLeftTripleClickKeyAction).setSelection(1);
                }
            } else if (entry.getKey().equalsIgnoreCase("Long_Press")) {
                if (entry.getValue().equalsIgnoreCase("VOICE_ASSISTANT")) {
                    ((is_right) ? mSpinRightLongPressKeyAction : mSpinLeftLongPressKeyAction).setSelection(0);
                } else if (entry.getValue().equalsIgnoreCase("GSOUND_VOICE_QUERY")) {
                    ((is_right) ? mSpinRightLongPressKeyAction : mSpinLeftLongPressKeyAction).setSelection(1);
                }
            }
        }
        if(is_right){
            checkMapMatch(mRightKeyMap);
            mBtnSetRightKepMap.setEnabled(true);
            printKeyMapLog("Get Right Channel Key Action: ", mRightKeyMap);
        }
        else if(!is_right){
            checkMapMatch(mLeftKeyMap);
            mBtnSetLeftKepMap.setEnabled(true);
            printKeyMapLog("Get Left Channel Key Action: ", mLeftKeyMap);
        }
    }

    private void checkMapMatch(HashMap<String, String> map){
        if(!map.containsKey("Double_Click")){
            map.put("Double_Click", "N/A");
        }
        if(!map.containsKey("Triple_Click")){
            map.put("Triple_Click", "N/A");
        }
        if(!map.containsKey("Long_Press")){
            map.put("Long_Press", "N/A");
        }
    }

    private void printKeyMapLog(String Title, HashMap<String, String> map){
        String log = Title;
        log = log + "Double_Click - " + map.get("Double_Click") + ", "
                + "Triple_Click - " + map.get("Triple_Click") + ", "
                + "Long_Press - " + map.get("Long_Press");
        addLogMsg(log);
    }

    private byte[] genKeyStruct(HashMap<String, String> map) {
        String rtn_str = "";

        for(HashMap.Entry<String, String> entry: map.entrySet()){
            if(entry.getKey().equalsIgnoreCase("Double_Click")) {
                if(entry.getValue().equalsIgnoreCase("FORWARD")){
                    rtn_str += FORWARD_HEXSTR;
                }
                else if(entry.getValue().equalsIgnoreCase("ANC_AND_PASSTHROUGH")){
                    rtn_str += ANC_AND_PASSTHROUGH_HEXSTR;
                }
            }
            else if(entry.getKey().equalsIgnoreCase("Triple_Click")) {
                if(entry.getValue().equalsIgnoreCase("BACKWARD")){
                    rtn_str += BACKWARD_HEXSTR;
                }
            }
            else if(entry.getKey().equalsIgnoreCase("Long_Press")) {
                if(entry.getValue().equalsIgnoreCase("VOICE_ASSISTANT")){
                    rtn_str += VOICE_ASSISTANT_HEXSTR;
                }
            }
        }
        return Converter.hexStringToByteArray(rtn_str);
    }

    void initUImember() {
        mBtnConSpp = findViewById(R.id.buttonConSpp);
        mBtnDisConSpp = findViewById(R.id.buttonDisConSPP);
        mEditSppAddr = findViewById(R.id.editTextSppAddr);
        mTextConSppResult = findViewById(R.id.textViewConSppResult);
        mTextConSppState = findViewById(R.id.textViewConSppState);
//
//        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String btaddr = mEditSppAddr.getText().toString();
//
//                try {
//                    Boolean result = mAirohaLink.connect(btaddr);
//                    mTextConSppResult.setText(result.toString());
//                } catch (Exception e) {
//                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mAirohaLink.disconnect();
//            }
//        });

        mBtnCheckChannel = findViewById(R.id.btnCheckAgentChannel);
        addToViewList(mBtnCheckChannel);
        mBtnCheckChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSetRightKepMap.setEnabled(false);
                mBtnSetLeftKepMap.setEnabled(false);
                mKeyMapMgr.checkPartnerStatus();
            }
        });

        mTextAgentChannel = findViewById(R.id.textAgentChannel);
        mTextPartnerChannel = findViewById(R.id.textPartnerChannel);

        mSpinLeftDoubleClickKeyAction = findViewById(R.id.spinLeftDoubleClickKeyAction);
        mSpinLeftDoubleClickKeyAction.setSelection(1);
        mSpinLeftTripleClickKeyAction = findViewById(R.id.spinLeftTripleClickKeyAction);
        mSpinLeftLongPressKeyAction = findViewById(R.id.spinLeftLongPressKeyAction);

        mBtnSetLeftKepMap = findViewById(R.id.buttonSetLeftKeyMap);
        mBtnSetLeftKepMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpinnerSelection(false);
                printKeyMapLog("Set Left Channel Key Action: ", mLeftKeyMap);
                byte[] nv_value = genKeyStruct(mLeftKeyMap);
                mKeyMapMgr.setKeyMap(nv_value, !mAgentIsRight);
            }
        });

        mSpinRightDoubleClickKeyAction = findViewById(R.id.spinRightDoubleClickKeyAction);
        mSpinRightTripleClickKeyAction = findViewById(R.id.spinRightTripleClickKeyAction);
        mSpinRightLongPressKeyAction = findViewById(R.id.spinRightLongPressKeyAction);

        mBtnSetRightKepMap = findViewById(R.id.buttonSetRightKeyMap);
        mBtnSetRightKepMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpinnerSelection(true);
                printKeyMapLog("Set Right Channel Key Action: ", mRightKeyMap);
                byte[] nv_value = genKeyStruct(mRightKeyMap);
                mKeyMapMgr.setKeyMap(nv_value, mAgentIsRight);
            }
        });

        mLeftKeyMap = new HashMap<>();
        mRightKeyMap = new HashMap<>();

        setLogView();

        disableButtonsAsDisconnected();
    }

    private void getSpinnerSelection(boolean is_right){
        if(is_right){
            mRightKeyMap.clear();
            mRightKeyMap.put("Double_Click", mSpinRightDoubleClickKeyAction.getSelectedItem().toString());
            mRightKeyMap.put("Triple_Click", mSpinRightTripleClickKeyAction.getSelectedItem().toString());
            mRightKeyMap.put("Long_Press", mSpinRightLongPressKeyAction.getSelectedItem().toString());
        }
        else{
            mLeftKeyMap.clear();
            mLeftKeyMap.put("Double_Click", mSpinLeftDoubleClickKeyAction.getSelectedItem().toString());
            mLeftKeyMap.put("Triple_Click", mSpinLeftTripleClickKeyAction.getSelectedItem().toString());
            mLeftKeyMap.put("Long_Press", mSpinLeftLongPressKeyAction.getSelectedItem().toString());
        }
    }

    private void setLogView()
    {
        mLogView = (ListView) findViewById(R.id.listView_log);
        gLogAdapter = new ArrayAdapter<>(mCtx, R.layout.message);
        mLogView.setAdapter(gLogAdapter);
    }

    void addLogMsg(String msg) {
        Log.d(TAG, msg);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS    ");
        String timeStr = sdf.format(new Date());
        gLogAdapter.add(timeStr + msg);
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connected", Toast.LENGTH_SHORT).show();
                    addLogMsg("Connected.");
                    mTextConSppState.setText("Conn. :" + type);

                    enableButtonsAsConnected();
                }
            });

            mKeyMapMgr.checkPartnerStatus();
        }

        @Override
        public void OnConnectionTimeout() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "Connection Timeout", Toast.LENGTH_SHORT).show();
                    addLogMsg("Connection Timeout.");
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "DisConnected", Toast.LENGTH_SHORT).show();
                    addLogMsg("DisConnected.");
                    mTextConSppState.setText("DisConn.");

                    resetUiText();

                    disableButtonsAsDisconnected();
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
        public void OnUnexpectedDisconnected() {

        }
    };

    private void resetUiText() {
        mTextAgentChannel.setText("");
        mTextPartnerChannel.setText("");
        mBtnSetRightKepMap.setEnabled(false);
        mBtnSetLeftKepMap.setEnabled(false);
    }

    private void addToViewList(Button button){
        mViewList.add(button);
    }

    private void enableButtonsAsConnected(){
        for(View v : mViewList){
            v.setEnabled(true);
        }
    }

    private void disableButtonsAsDisconnected(){
        for(View v : mViewList){
            v.setEnabled(false);
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
                mAirohaLink.logToFile(TAG, "clicked:" + info);
                String addr = info.split("\n")[1];
                mAirohaLink.logToFile(TAG, addr);

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
            mAirohaLink.logToFile("DeviceActivity", "Device : address : " + device.getAddress() + " name :"
                    + device.getName());

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }

        try {
            BluetoothDevice lastdevice = (BluetoothDevice) pairedDevices.toArray()[pairedDevices.size() - 1];
            mEditSppAddr.setText(lastdevice.getAddress());

            ParcelUuid[] parcelUuids = lastdevice.getUuids();

            for (ParcelUuid parcelUuid : parcelUuids) {
                mAirohaLink.logToFile(TAG, parcelUuid.toString());

                if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                    mAirohaLink.logToFile(TAG, "found Airoha device");

                    Toast.makeText(this, "Found Airoha Device:" + lastdevice.getName(), Toast.LENGTH_SHORT).show();

                    Boolean result = mAirohaLink.connect(lastdevice.getAddress());
                    mTextConSppResult.setText(result.toString());
                    return;
                }
            }
        } catch (Exception e) {
            mAirohaLink.logToFile(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAirohaLink.disconnect();
    }
}
