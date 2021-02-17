package com.airoha.android.lib.RaceCommand.packet.mmi;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdGetBattery extends RacePacket {
    public RaceCmdGetBattery() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_GET_BATTERY, null);
    }
}
