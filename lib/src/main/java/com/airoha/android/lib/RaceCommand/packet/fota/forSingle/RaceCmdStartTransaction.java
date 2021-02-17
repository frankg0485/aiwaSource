package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdStartTransaction extends RacePacket {
    public RaceCmdStartTransaction() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_START_TRANSCATION, null);
    }
}
