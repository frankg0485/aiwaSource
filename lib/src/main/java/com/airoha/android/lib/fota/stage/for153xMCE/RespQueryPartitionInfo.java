package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RespQueryPartitionInfo {
    //PartitionInfoCount (1 byte),
    //{
    //    Recipient (1 byte),
    //    PartitionID (1 byte),
    //    StorageType (1 byte),
    //    Address (4 bytes),
    //    Length (4 bytes)
    //} [%PartitionInfoCount%]
    public byte Recipient;
    public byte PartitionID;
    public byte StorageType;
    public byte[] Address = new byte[4];
    public byte[] Length = new byte[4];


    public static RespQueryPartitionInfo[] extractRespPartitionInfo(byte[] packet){
        //Status (1 byte),
        //PartitionInfoCount (1 byte),
        //{
        //    Recipient (1 byte),
        //    PartitionID (1 byte),
        //    StorageType (1 byte),
        //    Address (4 bytes),
        //    Length (4 bytes)
        //} [%PartitionInfoCount%]

        int idx = RacePacket.IDX_PAYLOAD_START + 1;

        int partitionInfoCount = packet[idx];
        idx = idx + 1;

        RespQueryPartitionInfo[] respQueryPartitionInfos = new RespQueryPartitionInfo[partitionInfoCount];

        for(int i = 0; i < partitionInfoCount; i++){
            respQueryPartitionInfos[i] = new RespQueryPartitionInfo();
            respQueryPartitionInfos[i].Recipient = packet[idx];
            idx = idx + 1;

            respQueryPartitionInfos[i].PartitionID = packet[idx];
            idx = idx + 1;

            respQueryPartitionInfos[i].StorageType = packet[idx];
            idx = idx + 1;

            System.arraycopy(packet, idx, respQueryPartitionInfos[i].Address, 0, 4);
            idx = idx + 4;

            System.arraycopy(packet, idx, respQueryPartitionInfos[i].Length, 0, 4);
            idx = idx + 4;
        }

        return respQueryPartitionInfos;
    }
}
