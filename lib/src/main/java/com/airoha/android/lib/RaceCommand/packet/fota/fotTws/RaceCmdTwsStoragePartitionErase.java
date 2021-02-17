package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdTwsStoragePartitionErase extends RacePacket {
    private byte[] mAgentLength = new byte[4];
    private byte[] mAgentFlashAddr = new byte[4];
    private byte[] mClientLength = new byte[4];
    private byte[] mClientFlashAddr = new byte[4];

    private byte mAgentStorageType;
    private byte mClientStorageType;

    public RaceCmdTwsStoragePartitionErase(byte agentStorageType, byte[] agentLength, byte[] agentFlashAddr,
                                           byte clientStoageType, byte[] clientLength, byte[] clientFlashAddr) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_DUAL_DEVICES_PARTITION_ERASE);

        mAgentStorageType = agentStorageType;
        mAgentLength = agentLength;
        mAgentFlashAddr = agentFlashAddr;

        mClientStorageType = clientStoageType;
        mClientLength = clientLength;
        mClientFlashAddr = clientFlashAddr;

        byte[] payload = new byte[18];

        payload[0] = mAgentStorageType;
        System.arraycopy(mAgentLength, 0, payload, 1, 4);
        System.arraycopy(mAgentFlashAddr, 0, payload, 5, 4);

        payload[9] = mClientStorageType;
        System.arraycopy(mClientLength, 0, payload, 10, 4);
        System.arraycopy(mClientFlashAddr, 0, payload, 14, 4);

        super.setPayload(payload);


        setAddr(agentFlashAddr);
        setClientAddr(clientFlashAddr);
        //AgentLength (4 bytes),
        //AgentFlashAddr ( 4 bytes),
        //ClientLength (4 bytes),
        //ClientFlashAddr ( 4 bytes)
    }
}
