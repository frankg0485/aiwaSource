package com.airoha.android.lib.fota.stage.for153xMCE;

import java.util.ArrayList;
import java.util.List;

public class QueryPartitionInfo {
    byte Recipient;
    byte PartitionID;

    public QueryPartitionInfo(byte  recipient, byte partitionID){
        this.Recipient = recipient;
        this.PartitionID = partitionID;
    }

    public byte[] toRaw(){
        return new byte[]{Recipient, PartitionID};
    }
}
