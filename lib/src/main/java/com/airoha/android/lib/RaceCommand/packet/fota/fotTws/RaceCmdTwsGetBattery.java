package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdTwsGetBattery extends RacePacket {
    /**
     *
     * @param payload agent or client
     */
    public RaceCmdTwsGetBattery(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_BLUETOOTH_GET_BATTERY, payload);
    }
}
