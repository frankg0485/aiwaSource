package com.airoha.android.lib.fota;

/**
 * Created by MTK60279 on 2018/2/22.
 */

public class StatusCode {
    public static final byte FOTA_ERRCODE_SUCESS = 0x00;
    public static final byte FOTA_ERRCODE_READ_FOTA_HEADER_FAIL = 0x01;
    public static final byte FOTA_ERRCODE_READ_FOTA_DATA_FAIL = 0x02;
    public static final byte FOTA_ERRCODE_CHECK_INTEGRITY_FAIL = 0x03;
    public static final byte FOTA_ERRCODE_UNKNOWN_STORAGE_TYPE = 0x04;
    public static final byte FOTA_ERRCODE_UNKNOWN_INTEGRITY_CHECK_TYPE = 0x05;
    public static final byte FOTA_ERRCODE_SHA256_IS_NOT_SUPPORTED = 0x06;
    public static final byte FOTA_ERRCODE_COMMIT_FAIL_DUE_TO_INTEGRITY_NOT_CHECKED = 0x07;
    public static final byte FOTA_ERRCODE_UNKNOWN_PARTITION_ID = 0x08;
    public static final byte FOTA_ERRCODE_UNINITIALIZED = (byte)0xFD;
    public static final byte FOTA_ERRCODE_UNSUPPORTED = (byte)0xFE;
    public static final byte FOTA_ERROCODE = (byte)0xFF;

    public static final byte FOTA_ERRCODE_UNSUPPORTED_PARTITION_ID = (byte)0x09;
}
