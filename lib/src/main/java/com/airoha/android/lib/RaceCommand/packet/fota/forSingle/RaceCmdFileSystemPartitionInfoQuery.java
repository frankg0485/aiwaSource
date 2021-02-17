package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.constant.RaceType;

/**
 * Created by MTK60279 on 2018/2/22.
 */

public class RaceCmdFileSystemPartitionInfoQuery extends RacePacket {
    public RaceCmdFileSystemPartitionInfoQuery(byte[] payload) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_FOTA_PARTITION_INFO_QUERY, payload);
    }
}
