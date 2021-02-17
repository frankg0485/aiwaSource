package com.airoha.android.lib.RaceCommand.packet.fota;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

/**
 * Created by MTK60348 on 2018/7/20.
 */

public class RaceFotaGetPartitionEraseStatus extends RacePacket {

    private byte mStorageType;
    private byte[] mPartitionAddr = new byte[4];
    private byte[] mPartitionLength = new byte[4];
    private byte mRole;

    public RaceFotaGetPartitionEraseStatus(byte role, byte storageType, byte[] partitionAddr, byte[] partitionLength) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_GET_PARTITION_ERASE_STATUS, null);

        mStorageType = storageType;
        mPartitionAddr = partitionAddr;
        mPartitionLength = partitionLength;
        mRole = role;

        byte[] payload = new byte[10];

        payload[0] = mRole;
        payload[1] = mStorageType;
        System.arraycopy(mPartitionAddr, 0, payload, 2, 4);
        System.arraycopy(mPartitionLength, 0, payload, 6, 4);

        super.setPayload(payload);

        setAddr(partitionAddr);
    }

    public byte getRole() {
        return mRole;
    }
}
