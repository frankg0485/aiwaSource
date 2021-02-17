package com.airoha.android.lib.RaceCommand.packet.mmi.anc;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdAncOff extends RacePacket {
    public RaceCmdAncOff() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_ANC_OFF);
    }
}
