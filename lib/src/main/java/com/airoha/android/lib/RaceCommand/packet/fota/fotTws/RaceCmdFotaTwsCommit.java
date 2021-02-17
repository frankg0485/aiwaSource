package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdFotaTwsCommit extends RacePacket {
    public RaceCmdFotaTwsCommit(){
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_DUAL_DEVICES_COMMIT, null);
    }
}
