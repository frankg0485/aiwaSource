package com.airoha.android.lib.RaceCommand.packet.mmi.anc;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdAncWriteGainToNvKey extends RacePacket {
    public RaceCmdAncWriteGainToNvKey(byte step0, byte step1) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_ANC_WRITE_GAIN_TO_NVKEY);

        byte[] payload = new byte[]{step0, step1};

        super.setPayload(payload);
    }
}
