package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 4
public class RaceCmdFlashPageProgram extends RacePacket {
    /**
     * @param payload 1byte CRC, 4 bytes address, 256 bytes data
     */
    public RaceCmdFlashPageProgram(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FLASH_PAGE_PROGRAM, payload);

        byte[] addr = {payload[1], payload[2], payload[3], payload[4]};

        setAddr(addr);
    }
}
