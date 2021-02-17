package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdFotaPartitionInfoQuery;
import com.airoha.android.lib.RaceCommand.packet.fota.fotTws.RaceCmdFotaTwsQueryPartition;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.partition.PartitionId;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_QueryPartitionInfo;
import com.airoha.android.lib.util.Converter;
import com.google.common.primitives.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MTK60279 on 2018/2/7.
 */

public class FotaStage_00_QueryPartitionInfoRelay extends FotaStage_00_QueryPartitionInfo {

    public FotaStage_00_QueryPartitionInfoRelay(AirohaRaceOtaMgr mgr, QueryPartitionInfo[] queryPartitionInfos) {
        super(mgr, queryPartitionInfos);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_FOTA_PARTITION_INFO_QUERY;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;

        TAG = "FotaStage_00_QueryPartitionInfoRelay";
    }


    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }
}
