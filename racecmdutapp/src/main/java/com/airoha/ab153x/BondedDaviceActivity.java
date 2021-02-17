package com.airoha.ab153x;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class BondedDaviceActivity extends AppCompatActivity {

    public class MeshDevInfo {
        BluetoothDevice mBtDevice;
        int mRssi;
        byte[] mUUID;
        short mOobInfo;
        byte[] mUriHash;
    }
    private ArrayList<MeshDevInfo> mMeshDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search Bonded Device");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        Set<BluetoothDevice> all_devices = bluetoothAdapter.getBondedDevices();
        if (all_devices.size() > 0) {
            for (BluetoothDevice currentDevice : all_devices) {
                MeshDevInfo meshDevInfo = new MeshDevInfo();
                String name = currentDevice.getName();
                meshDevInfo.mBtDevice = currentDevice;

                mMeshDevices = new ArrayList<MeshDevInfo>();
                mMeshDevices.add(meshDevInfo);
            }
        }
    }
}
