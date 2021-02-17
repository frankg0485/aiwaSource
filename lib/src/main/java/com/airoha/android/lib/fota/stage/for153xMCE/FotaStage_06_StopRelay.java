package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_06_Stop;

public class FotaStage_06_StopRelay extends FotaStage_06_Stop {
    public FotaStage_06_StopRelay(AirohaRaceOtaMgr mgr, byte[] recipients, byte reason) {
        super(mgr, recipients, reason);


        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_FOTA_STOP;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);

        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd);
    }
}
