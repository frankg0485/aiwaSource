package com.airoha.android.lib.RaceCommand.packet.fota;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdGetInternalFlashPartitionSHA256 extends RacePacket {

    private byte[] mPartitionAddr = new byte[4];
    private byte[] mPartitionLength = new byte[4];
    private byte mRole;

    public RaceCmdGetInternalFlashPartitionSHA256(byte[] partitionAddr, byte[] partitionLength, byte role) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_GET_INTERNAL_FLASH_PARTITION_SHA256, null);

        mPartitionAddr = partitionAddr;
        mPartitionLength = partitionLength;
        mRole = role;

        byte[] payload = new byte[9];

        System.arraycopy(mPartitionAddr, 0, payload, 0, 4);
        System.arraycopy(mPartitionLength, 0, payload, 4, 4);
        payload[8] = mRole;

        super.setPayload(payload);

        setAddr(partitionAddr);
    }

    public byte getRole() {
        return mRole;
    }
}
