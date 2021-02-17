package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdGetClientExistence extends RacePacket {
    public RaceCmdGetClientExistence() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_BLUETOOTH_GET_CLIENT_EXISTENCE, null);
    }
}
