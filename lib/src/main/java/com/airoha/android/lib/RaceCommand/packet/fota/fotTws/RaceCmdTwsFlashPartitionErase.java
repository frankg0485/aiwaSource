package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdTwsFlashPartitionErase extends RacePacket {
    private byte[] mAgentLength = new byte[4];
    private byte[] mAgentFlashAddr = new byte[4];
    private byte[] mClientLength = new byte[4];
    private byte[] mClientFlashAddr = new byte[4];

    public RaceCmdTwsFlashPartitionErase(byte[] agentLength, byte[] agentFlashAddr, byte[] clientLength, byte[] clientFlashAddr) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FLASH_DUAL_DEVICES_PARTITION_ERASE);

        mAgentLength = agentLength;
        mAgentFlashAddr = agentFlashAddr;
        mClientLength = clientLength;
        mClientFlashAddr = clientFlashAddr;

        byte[] payload = new byte[16];

        System.arraycopy(mAgentLength, 0, payload, 0, 4);
        System.arraycopy(mAgentFlashAddr, 0, payload, 4, 4);
        System.arraycopy(mClientLength, 0, payload, 8, 4);
        System.arraycopy(mClientFlashAddr, 0, payload, 12, 4);

        super.setPayload(payload);


        setAddr(agentFlashAddr);
        setClientAddr(clientFlashAddr);
        //AgentLength (4 bytes),
        //AgentFlashAddr ( 4 bytes),
        //ClientLength (4 bytes),
        //ClientFlashAddr ( 4 bytes)
    }
}
