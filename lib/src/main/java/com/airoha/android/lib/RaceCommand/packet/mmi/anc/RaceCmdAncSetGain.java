package com.airoha.android.lib.RaceCommand.packet.mmi.anc;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

public class RaceCmdAncSetGain extends RacePacket {
    public RaceCmdAncSetGain(byte left, byte step) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_ANC_SET_GAIN);

        byte[] payload = new byte[]{left, step};

        super.setPayload(payload);
    }
}
