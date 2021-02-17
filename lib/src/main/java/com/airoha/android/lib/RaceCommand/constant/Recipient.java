package com.airoha.android.lib.RaceCommand.constant;

public class Recipient {
    public static final byte Agent = (byte) 0x01;
    public static final byte Partner = (byte) (0x01 << 1);
    public static final byte Twin = (byte) (Agent | Partner);
    public static final byte SP = (byte) (0x01 << 7);
    public static final byte DontCare = (byte) 0xFF;
}
