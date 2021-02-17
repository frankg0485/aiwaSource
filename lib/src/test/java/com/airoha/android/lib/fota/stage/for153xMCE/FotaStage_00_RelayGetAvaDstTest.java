package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdGetAvaDst;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdRelayPass;

import org.junit.Test;

import static org.junit.Assert.*;

public class FotaStage_00_RelayGetAvaDstTest {

    @Test
    public void genRacePackets() {
        RacePacket cmd = new RaceCmdRelayPass();

        RacePacket relayCmd = new RaceCmdGetAvaDst();
        byte[] relayCmdRaw = relayCmd.getRaw();

        byte[] payload = new byte[2+relayCmdRaw.length];

        byte[] dst = new byte[]{0x05, 0x04};
        System.arraycopy(dst, 0, payload, 0, 2);
        System.arraycopy(relayCmdRaw, 0, payload, 2, relayCmdRaw.length);

        cmd.setPayload(payload);

        byte[] expected = new byte[]{(byte)0x05, (byte)0x5A, (byte)0x0A, (byte)0x00, (byte)0x01,
                (byte)0x0D, (byte)0x05, (byte)0x04, (byte)0x05, (byte)0x5A, (byte)0x02, (byte)0x00,
                (byte)0x00, (byte)0x0D};

        assertArrayEquals(expected, cmd.getRaw());
    }
}