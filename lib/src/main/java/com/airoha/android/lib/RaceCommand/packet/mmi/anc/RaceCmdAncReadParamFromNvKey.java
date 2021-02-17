package com.airoha.android.lib.RaceCommand.packet.mmi.anc;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdAncReadParamFromNvKey extends RacePacket {
    public RaceCmdAncReadParamFromNvKey() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_ANC_READ_PARAM_FROM_NVKEY);
    }
}
