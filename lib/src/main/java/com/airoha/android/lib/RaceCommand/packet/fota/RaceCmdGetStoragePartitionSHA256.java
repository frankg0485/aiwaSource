package com.airoha.android.lib.RaceCommand.packet.fota;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdGetStoragePartitionSHA256 extends RacePacket {

    private byte[] mPartitionAddr = new byte[4];
    private byte[] mPartitionLength = new byte[4];
    private byte mRecipient;
    private byte mStorageType;

    public RaceCmdGetStoragePartitionSHA256(byte recipient, byte storageType, byte[] partitionAddr, byte[] partitionLength) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_GET_PARTITION_SHA256, null);

        mPartitionAddr = partitionAddr;
        mPartitionLength = partitionLength;
        mRecipient = recipient;
        mStorageType = storageType;

        //"RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes)
        //} [%RecipientCount%]"

        byte[] payload = new byte[11];
        int idx = 0;

        payload[idx] = 0x01; //RecipientCount
        idx = idx + 1;

        payload[idx] = mRecipient;
        idx = idx + 1;

        payload[idx] = mStorageType;
        idx = idx + 1;

        System.arraycopy(mPartitionAddr, 0, payload, idx, 4);
        idx = idx + 4;
        System.arraycopy(mPartitionLength, 0, payload, idx, 4);


        super.setPayload(payload);

        setAddr(partitionAddr);
    }

    public byte getRole() {
        return mRecipient;
    }

    public byte getStorageType() {return mStorageType;}
}
