package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_14_ComparePartitionV2Storage;

public class FotaStage_14_ComparePartitionV2StorageRelay extends FotaStage_14_ComparePartitionV2Storage {
    public FotaStage_14_ComparePartitionV2StorageRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_GET_PARTITION_SHA256;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd, String key) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        relayCmd.setQueryKey(key);

        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(key, relayCmd);
    }
}
