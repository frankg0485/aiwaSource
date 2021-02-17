package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdRoleSwitch extends RacePacket {
    public RaceCmdRoleSwitch() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_BLUETOOTH_ROLE_SWITCH, null);
    }
}
