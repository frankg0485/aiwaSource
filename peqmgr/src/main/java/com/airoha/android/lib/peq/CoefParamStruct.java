package com.airoha.android.lib.peq;

import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Airoha internal use
 */
public class CoefParamStruct {
    byte[] mSampleRateId;
    byte[] mParamCount;
    byte[] mCoefParam;

    public CoefParamStruct(short sampleRateid, short paramCount, byte[] coefParam){
        mSampleRateId = Converter.shortToBytes(sampleRateid);
        mParamCount = Converter.shortToBytes(paramCount);
        mCoefParam = coefParam;
    }

    public byte[] getRaw(){
        List<Byte> bytelist = new ArrayList<>();

        for(byte b: mSampleRateId){
            bytelist.add(b);
        }

        for (byte b: mParamCount){
            bytelist.add(b);
        }

        for(byte b: mCoefParam) {
            bytelist.add(b);
        }

        byte[] result = new byte[bytelist.size()];

        for(int i = 0; i< bytelist.size(); i++) {
            result[i] = bytelist.get(i);
        }

        return result;
    }
}
