package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdFotaActiveFotaPreparation extends RacePacket {
    public RaceCmdFotaActiveFotaPreparation(byte agentOrClient) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_ACTIVE_FOTA_PREPARATION, new byte[]{agentOrClient});
    }
}
