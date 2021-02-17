package com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;

public class RaceCmdRelayPass extends RacePacket {
    public RaceCmdRelayPass() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_RELAY_PASS_TO_DST);
    }

    public RaceCmdRelayPass(Dst dst, RacePacket packetToBeRelayed){
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_RELAY_PASS_TO_DST);

        byte[] relayCmdRaw = packetToBeRelayed.getRaw();

        byte[] payload = new byte[2+relayCmdRaw.length];

//        byte[] dst = new byte[]{0x05, 0x04};
        System.arraycopy(dst.toRaw(), 0, payload, 0, 2);
        System.arraycopy(relayCmdRaw, 0, payload, 2, relayCmdRaw.length);

        setPayload(payload);
    }
}
