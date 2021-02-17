package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdFotaTwsWriteState extends RacePacket {
    public RaceCmdFotaTwsWriteState(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_DUAL_DEVICES_WRITE_STATE, payload);
    }
}
