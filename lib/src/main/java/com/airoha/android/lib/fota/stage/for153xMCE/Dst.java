package com.airoha.android.lib.fota.stage.for153xMCE;

public class Dst {
    public byte Type;
    public byte Id;

    public byte[] toRaw(){
        return new byte[]{Type, Id};
    }
}
