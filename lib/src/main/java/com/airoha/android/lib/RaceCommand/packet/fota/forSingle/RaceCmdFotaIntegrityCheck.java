package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 5
public class RaceCmdFotaIntegrityCheck extends RacePacket {
    /**
     * @param storageType 00 internal, 01 external
     */
    public RaceCmdFotaIntegrityCheck(byte[] storageType) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_INTEGRITY_CHECK, storageType);
    }
}
