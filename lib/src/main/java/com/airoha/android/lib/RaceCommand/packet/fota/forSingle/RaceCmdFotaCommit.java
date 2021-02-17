package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 6
public class RaceCmdFotaCommit extends RacePacket {
    public RaceCmdFotaCommit() {
        super(RaceType.CMD_NO_RESP, RaceId.RACE_FOTA_COMMIT, null);
    }
}
