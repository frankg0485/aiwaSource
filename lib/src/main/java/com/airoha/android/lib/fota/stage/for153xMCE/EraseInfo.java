package com.airoha.android.lib.fota.stage.for153xMCE;

import com.google.common.primitives.Bytes;

import java.util.ArrayList;
import java.util.List;

public class EraseInfo {
    public byte Recipient;
    public byte StorageType;
    public byte[] Address;
    public byte[] Length;

    public byte[] toRaw(){
        List<Byte> list = new ArrayList<>();

        list.add(Recipient);
        list.add(StorageType);

        for(byte b : Address){
            list.add(b);
        }

        for (byte b: Length){
            list.add(b);
        }

        return Bytes.toArray(list);
    }
}
