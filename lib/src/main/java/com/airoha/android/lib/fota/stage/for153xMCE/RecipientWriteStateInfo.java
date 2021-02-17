package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

import java.util.List;

public class RecipientWriteStateInfo {
    //RecipientCount (1 byte),
    //{
    //    Recipient (1 byte),
    //    FotaState (2 bytes)
    //} [%RecipientCount%]
    public byte Recipient;
    public int FotaState;

    public byte[] toRaw(){
        byte[] fotaState = Converter.ShortToBytes((short)FotaState);

        byte[] result = new byte[3];

        result[0] = Recipient;

        System.arraycopy(fotaState, 0, result, 1, 2);

        return result;
    }
}
