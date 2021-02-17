package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_11_DiffFlashPartitionEraseStorage;

public class FotaStage_11_DiffFlashPartitionEraseStorageRelay extends FotaStage_11_DiffFlashPartitionEraseStorage {
    public FotaStage_11_DiffFlashPartitionEraseStorageRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_PARTITION_ERASE;
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
