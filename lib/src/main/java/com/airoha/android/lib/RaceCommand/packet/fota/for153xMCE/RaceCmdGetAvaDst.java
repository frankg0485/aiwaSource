package com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdGetAvaDst extends RacePacket {
    public RaceCmdGetAvaDst() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_RELAY_GET_AVA_DST);
    }
}
