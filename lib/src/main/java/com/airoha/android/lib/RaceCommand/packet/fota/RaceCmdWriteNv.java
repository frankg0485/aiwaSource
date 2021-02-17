package com.airoha.android.lib.RaceCommand.packet.fota;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

/**
 * Created by MTK60279 on 2018/3/27.
 */

public class RaceCmdWriteNv extends RacePacket {
    public RaceCmdWriteNv(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_WRITEFULLKEY, payload);
    }

    public RaceCmdWriteNv(int nvKey, byte[] nvValue){
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_WRITEFULLKEY, null);

        byte[] payload = new byte[2+nvValue.length];

        payload[0] = (byte) (nvKey& 0xFF);
        payload[1] = ((byte) ((nvKey >> 8) & 0xFF));


        System.arraycopy(nvValue, 0, payload, 2, nvValue.length);

        super.setPayload(payload);
    }
}
