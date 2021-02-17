package com.airoha.android.lib.RaceCommand.packet.mmi;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdSwitchPowerMode extends RacePacket {
    public RaceCmdSwitchPowerMode(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_SWITCH_POWER_MODE, payload);
    }
}
