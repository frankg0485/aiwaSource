package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_12_ProgramDiffFotaStorage;

public class FotaStage_12_ProgramDiffFotaStorageRelay extends FotaStage_12_ProgramDiffFotaStorage {
    public FotaStage_12_ProgramDiffFotaStorageRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_PAGE_PROGRAM;
        mRelayRaceRespType = RaceType.RESPONSE;

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
