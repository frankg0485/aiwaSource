package com.airoha.android.lib.util;

/**
 * Created by MTK60279 on 2017/10/19.
 */

public class ContentChecker {
    public static boolean isAllDummyHexFF(byte[] dataBuffer){
        for(int i=0; i<dataBuffer.length; i++){
            if (dataBuffer[i] != (byte) 0xFF){
                return false;
            }
        }

        return true;
    }
}
