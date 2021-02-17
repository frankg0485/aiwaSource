package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_01_Lock_Unlock;

public class FotaStage_01_Lock_UnlockRelay extends FotaStage_01_Lock_Unlock {
    public FotaStage_01_Lock_UnlockRelay(AirohaRaceOtaMgr mgr, boolean isLock) {
        super(mgr, isLock);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_LOCK_UNLOCK;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }


    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);

        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, cmd);
    }
}
