package com.airoha.android.lib.fota.nvr;

import com.airoha.android.lib.util.Converter;

public class NvrDescriptor {
    public NvrDescriptor(String mNvKey, String mNvValue) {
        this.mNvKey = mNvKey;
        this.mNvValue = mNvValue;
    }

    private String mNvKey;
    private String mNvValue;

    private int mIntNvKey;

    private byte[] mBytesNvValue;

    public int getNvKey(){
        return Integer.valueOf(mNvKey, 16);
    }

    public byte[] getNvValue(){
        // check

        return Converter.hexStringToByteArray(mNvValue);
    }

}
