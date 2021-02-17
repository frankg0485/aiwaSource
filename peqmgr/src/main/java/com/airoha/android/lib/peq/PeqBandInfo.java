package com.airoha.android.lib.peq;

import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Client App will use this for input/output with UI
 * @see PeqUiDataStru
 */
public class PeqBandInfo {
    private byte mByteEnable = 0x01;
    private byte mByteType = 0x02;
    private byte[] mBytesFreq; //= new byte[4];
    private byte[] mBytesGain; //= new byte[4];
    private byte[] mBytesBw; //= new byte[4];
    private byte[] mBytesQ; //= new byte[4];

    private float mFreq;
    private float mGain;
    private float mBw;
    private float mQ; // Freq/Bw

    /**
     * Client App will need to input the param for building {@link PeqUiDataStru}
     * @param freq
     * @param bw
     * @param gain
     */
    public PeqBandInfo(float freq, float bw, float gain) {
        mFreq = freq;
        mBw = bw;
        mGain = gain;
        mQ = mFreq / mBw;

        mByteEnable = 0x01;
        mByteType = 0x02;

        // refer config. tool
        mBytesFreq = Converter.intToBytes((int) (mFreq * 100));
        mBytesGain = Converter.intToBytes((int) (mGain * 100));
        mBytesBw = Converter.intToBytes((int) (mBw * 100));
        mBytesQ = Converter.intToBytes((int) (mQ * 100));
    }

    /**
     * Airoha internal use
     * @param raw
     */
    public PeqBandInfo(byte[] raw) {
        mByteEnable = raw[0];
        mByteType = raw[1];

        int idx = 2;

        mBytesFreq = new byte[4];
        System.arraycopy(raw, idx, mBytesFreq, 0, 4);
        idx += 4;

        mBytesGain = new byte[4];
        System.arraycopy(raw, idx, mBytesGain, 0, 4);
        idx += 4;

        mBytesBw = new byte[4];
        System.arraycopy(raw, idx, mBytesBw, 0, 4);
        idx += 4;

        mBytesQ = new byte[4];
        System.arraycopy(raw, idx, mBytesQ, 0, 4);


        mFreq = (float) (Converter.bytesToInt32(mBytesFreq) / 100.0);
        mGain = (float) (Converter.bytesToInt32(mBytesGain) / 100.0);
        mBw = (float) ((Converter.bytesToInt32(mBytesBw) / 100.0));
        mQ = (float) ((Converter.bytesToInt32(mBytesQ) / 100.0));
    }

    /**
     * reserved for complex UI design
     * @return
     */
    public boolean isEnable() {
        return mByteEnable == 0x01;
    }

    /**
     * Client App will call this to display UI
     * @return
     */
    public float getFreq() {
        return mFreq;
    }

    /**
     * Client App will call this to display UI
     * @return
     */
    public float getGain() {
        return mGain;
    }

    /**
     * Client App will call this to display UI
     * @return
     */
    public float getBw() {
        return mBw;
    }

    public float getQ() {
        return mQ;
    }

    /**
     * Airoha internal use
     * @return
     */
    public byte[] getRaw() {
        List<Byte> bytelist = new ArrayList<>();

        bytelist.add(mByteEnable);

        bytelist.add(mByteType);

        for (byte b : mBytesFreq) {
            bytelist.add(b);
        }

        for (byte b : mBytesGain) {
            bytelist.add(b);
        }

        for (byte b : mBytesBw) {
            bytelist.add(b);
        }

        for (byte b : mBytesQ) {
            bytelist.add(b);
        }


        byte[] result = new byte[bytelist.size()];

        for (int i = 0; i < bytelist.size(); i++) {
            result[i] = bytelist.get(i);
        }

        return result;
    }

}
