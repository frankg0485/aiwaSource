package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 3
public class RaceCmdFlashSectorErase extends RacePacket {
    /**
     * @param address 4 bytes address
     */
    public RaceCmdFlashSectorErase(byte[] address) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FLASH_SECTOR_ERASE, address);

        setAddr(address);
    }
}
