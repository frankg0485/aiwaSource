package com.airoha.android.lib.RaceCommand.packet.mmi;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdResumeDsp extends RacePacket {
    public RaceCmdResumeDsp() {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_RESUME_DSP, null);
    }
}
