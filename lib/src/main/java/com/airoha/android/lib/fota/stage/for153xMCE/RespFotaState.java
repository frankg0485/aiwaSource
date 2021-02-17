package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RespFotaState {
    public byte Recipient;
    public byte[] FotaState = new byte[2];


    public static RespFotaState[] extractRespFotaStates(byte[] packet) {
        //Status (1 byte),
        //RecipientCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    FotaState (2 bytes)
        //} [%RecipientCount%]

        int idx = RacePacket.IDX_PAYLOAD_START + 1;
        int recipientCount = packet[idx];

        idx = idx + 1;

        RespFotaState[] respFotaStates = new RespFotaState[recipientCount];

        for (int i = 0; i < recipientCount; i++) {
            respFotaStates[i] = new RespFotaState();
            respFotaStates[i].Recipient = packet[idx];
            idx = idx + 1;

            System.arraycopy(packet, idx, respFotaStates[i].FotaState, 0, 2);
            idx = idx + 2;
        }
        return respFotaStates;
    }
}
