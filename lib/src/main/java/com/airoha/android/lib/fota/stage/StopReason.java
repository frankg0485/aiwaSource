package com.airoha.android.lib.fota.stage;

public class StopReason {
    public static final byte Cancel = (byte) 0x00;
    public static final byte Fail = (byte) 0x01;
    public static final byte Timeout = (byte) 0x02;
    public static final byte PartnerLost = (byte) 0x03;
    public static final byte ActiveFotaStopped = (byte) 0x04;
}
