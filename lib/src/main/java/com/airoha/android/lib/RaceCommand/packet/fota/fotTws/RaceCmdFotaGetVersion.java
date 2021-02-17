package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.fota.AgentClientEnum;

public class RaceCmdFotaGetVersion extends RacePacket {
    /**
     *
     * @param payLoad {@link AgentClientEnum}
     */
    public RaceCmdFotaGetVersion(byte[] payLoad) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_GET_VERSION, payLoad);
    }
}
