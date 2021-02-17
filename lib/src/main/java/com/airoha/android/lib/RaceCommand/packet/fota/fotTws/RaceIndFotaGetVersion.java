package com.airoha.android.lib.RaceCommand.packet.fota.fotTws;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;

public class RaceIndFotaGetVersion extends RacePacket {
    public RaceIndFotaGetVersion(byte agentOrClient){
        //        "Status (1 byte),
        //        AgentOrClient (1 byte),
        //        VersionLength (1 byte),
        //        Version (string with length VersionLength)"
        super(RaceType.INDICATION, RaceId.RACE_FOTA_GET_VERSION,
                new byte[]{StatusCode.FOTA_ERRCODE_SUCESS, agentOrClient, 0x00});
    }
}
