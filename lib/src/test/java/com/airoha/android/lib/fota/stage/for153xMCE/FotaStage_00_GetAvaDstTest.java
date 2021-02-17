package com.airoha.android.lib.fota.stage.for153xMCE;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class FotaStage_00_GetAvaDstTest {

    @Test
    public void parsePayloadAndCheckCompeted() {
        // input data
        byte[] packet = {(byte) 0x05, (byte)0x5B, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x0D,
                (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x04, (byte)0x03, (byte)0x03};

        // logic start
        int payloadLength = (packet[2]&0xFF) | (packet[3]&0xFF << 8);

        int dstListLength = (payloadLength - 2)/2;

        List<Dst> dstList = new ArrayList<>();
        for(int i = RacePacket.IDX_PAYLOAD_START; i < packet.length -1; i=i+2) {
            Dst dst = new Dst();
            dst.Type = packet[i];
            dst.Id = packet[i+1];
            dstList.add(dst);
        }


        Dst awsPeerDst=null;
        for(Dst dst : dstList){
            if(dst.Type == AvailabeDst.RACE_CHANNEL_TYPE_AWSPEER){
                awsPeerDst = dst;
                break;
            }
        }

        if(awsPeerDst == null){
            // do error handling
        }


        // assert check
        assertEquals(dstListLength, dstList.size());
        assertNotNull(awsPeerDst);
    }
}