package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

public class RaceCmdWriteState extends RacePacket {
    /**
     *
     * @param payload {@link com.airoha.android.lib.fota.fotaState.StageEnum}
     */
    public RaceCmdWriteState(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_WRITE_STATE, payload);
    }
}
