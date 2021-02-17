package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_14_CompareFileSystemPartition;

import java.io.File;

public class FotaStage_14_CompareFileSystemPartitionRelay extends FotaStage_14_CompareFileSystemPartition {

    public FotaStage_14_CompareFileSystemPartitionRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }

    public FotaStage_14_CompareFileSystemPartitionRelay(AirohaRaceOtaMgr mgr, File file) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;

        mFile = file;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd);
    }
}
