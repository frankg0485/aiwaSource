package com.airoha.android.lib.RaceCommand.packet.fota;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;

public class RaceRespActiveFotaPreparation extends RacePacket {
    public RaceRespActiveFotaPreparation(){
        super(RaceType.RESPONSE, RaceId.RACE_FOTA_ACTIVE_FOTA_PREPARATION,
                new byte[]{StatusCode.FOTA_ERRCODE_SUCESS});
    }
}
