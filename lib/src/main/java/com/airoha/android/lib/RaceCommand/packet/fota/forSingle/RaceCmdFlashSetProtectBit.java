package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 2
public class RaceCmdFlashSetProtectBit extends RacePacket {

    public static final byte PROTECT_BIT_UNLOCK = 0x00;
    public static final byte PROTECT_BIT_LOCK = 0x0F;

    /**
     * @param protectBit: 0x00: unlock, 0x0F: Lock
     */
    public RaceCmdFlashSetProtectBit(byte[] protectBit) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FLASH_SET_PROTECT_BIT, protectBit);
    }
}
