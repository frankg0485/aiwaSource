package com.airoha.android.lib.util;

/**
 * Created by MTK60279 on 2017/10/19.
 */

public class ContentConcatenater {
    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
