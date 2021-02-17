package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdQueryState extends RacePacket {
    public RaceCmdQueryState() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_QUERY_STATE, null);
    }
}
