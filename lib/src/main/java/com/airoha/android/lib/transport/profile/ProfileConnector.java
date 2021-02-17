package com.airoha.android.lib.transport.profile;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Daniel.Lee on 2016/5/31.
 */
public class ProfileConnector {
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBtDevice = null;
    private static BluetoothA2dp mBluetoothA2dp;
    private static BluetoothHeadset mBluetoothHeadset;
    private static Context mContext;
    private static ProfileConnector mProfileConnector;

    public static ProfileConnector getInst(Context context) {
        if (mProfileConnector == null) {
            mProfileConnector = new ProfileConnector(context);
        }
        return mProfileConnector;
    }

    public static void clearBluetoothAdapter() {
        mBluetoothAdapter = null;
    }

    private ProfileConnector(Context context) {
        mContext = context;
    }

    public int isA2dpConnected(BluetoothDevice btdevice) {
        int ret = -1;
        if (mBluetoothA2dp != null) {
            int state = mBluetoothA2dp.getConnectionState(btdevice);

            if (state == BluetoothA2dp.STATE_CONNECTED)
                ret = 1;
            else
                ret = 0;
        }

        return ret;
    }

    public int isScoConnected(BluetoothDevice btdevice) {
        int ret = -1;
        if (mBluetoothHeadset != null) {
            if(mBluetoothHeadset.isAudioConnected(btdevice)){
                ret = 1;
            } else {
                ret = 0;
            }
        }

        return ret;
    }

    public int isHfpConnected(BluetoothDevice btdevice) {
        int ret = -1;
        if (mBluetoothHeadset != null) {

            int state = mBluetoothHeadset.getConnectionState(btdevice);

            if (state == BluetoothHeadset.STATE_CONNECTED)
                ret = 1;
            else
                ret = 0;
        }

        return ret;
    }

    public int isA2dpPlaying(BluetoothDevice btdevice) {
        int ret = -1;
        if (mBluetoothA2dp != null) {
            if (mBluetoothA2dp.isA2dpPlaying(btdevice)){
                ret = 1;
            } else {
                ret = 0;
            }
        }

        return ret;
    }

    public void connectProfileProxy() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBluetoothA2dp == null) {
            mBluetoothAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.A2DP);
        }
        if (mBluetoothHeadset == null) {
            mBluetoothAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.HEADSET);
        }
    }

    public void connectA2dp(BluetoothDevice btdevice) {
        mBtDevice = btdevice;

        if (mBluetoothA2dp != null) {
            connectBluetoothA2dpProfile();
        }
    }

    public void connectHfp(BluetoothDevice btdevice) {
        mBtDevice = btdevice;

        if (mBluetoothHeadset != null) {
            connectBluetoothHeadsetProfile();
        }
    }


    // 2016.08.17 Daniel: Mantis#7866 A2DP issue
    // need to disconnectProfilesProxy or causing memory leakage
    public void disconnectProfilesProxy() {

        // 2016.08.23 Daniel: possible null exception
        if (mBluetoothA2dp != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
        }
        if (mBluetoothHeadset != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
        }
    }

    public void disconnectA2dp(BluetoothDevice btdevice) {
        mBtDevice = btdevice;

        if (mBluetoothA2dp != null) {
            disconnectBluetoothA2dpProfile();
        }
    }


    public void disconnectHfp(BluetoothDevice btdevice) {
        mBtDevice = btdevice;

        if (mBluetoothHeadset != null) {
            disconnectBluetoothHeadsetProfile();
        }
    }


    private static void connectBluetoothHeadsetProfile() {
        Method m = null;
        try {
            m = mBluetoothHeadset.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.setAccessible(true);

        try {
            boolean successHeadset = (Boolean) m.invoke(mBluetoothHeadset, mBtDevice);
            Log.d("Airoha", "connect hfp: " + successHeadset);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void connectBluetoothA2dpProfile() {
        Method m = null;
        try {
            m = mBluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.setAccessible(true);

        try {
            boolean successHeadset = (Boolean) m.invoke(mBluetoothA2dp, mBtDevice);
            Log.d("Airoha", "connect a2dp: " + successHeadset);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void disconnectBluetoothA2dpProfile() {
        Method m = null;
        try {
            m = mBluetoothA2dp.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.setAccessible(true);

        try {
            boolean successHeadset = (Boolean) m.invoke(mBluetoothA2dp, mBtDevice);
            Log.d("Airoha", "disconnect a2dp: " + successHeadset);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void disconnectBluetoothHeadsetProfile() {
        Method m = null;
        try {
            m = mBluetoothHeadset.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.setAccessible(true);

        try {
            boolean successHeadset = (Boolean) m.invoke(mBluetoothHeadset, mBtDevice);
            Log.d("Airoha", "disconnect headset: " + successHeadset);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private static final BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothA2dp = (BluetoothA2dp) proxy;

                // 2016.08.17 Daniel: Mantis#7866 A2DP issue
//                int state = mBluetoothA2dp.getConnectionState(mBtDevice);
//                if (state == BluetoothProfile.STATE_CONNECTED ||
//                        state == BluetoothProfile.STATE_CONNECTING) // 2016.08.22 Daniel: Mantis#7868 skipp when Device is auto reconnecting
//                    return;
//
//                connectBluetoothA2dpProfile();
            }

            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;

                // 2016.08.17 Daniel: Mantis#7866 A2DP issue
//                int state = mBluetoothHeadset.getConnectionState(mBtDevice);
//                if (state == BluetoothProfile.STATE_CONNECTED ||
//                        state == BluetoothProfile.STATE_CONNECTING) // 2016.08.22 Daniel: Mantis#7868 skipp when Device is auto reconnecting
//                    return;
//
//                connectBluetoothHeadsetProfile();
            }

        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
//                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
                mBluetoothA2dp = null;
            }

            if (profile == BluetoothProfile.HEADSET) {
//                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
                mBluetoothHeadset = null;
            }

        }
    };
}
