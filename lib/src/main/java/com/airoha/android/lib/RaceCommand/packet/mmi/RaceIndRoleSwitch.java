package com.airoha.android.lib.RaceCommand.packet.mmi;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceIndRoleSwitch extends RacePacket {
        public RaceIndRoleSwitch() {
        super(RaceType.INDICATION, RaceId.RACE_BLUETOOTH_ROLE_SWITCH, new byte[]{0x00});
    }
}
