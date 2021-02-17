package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_05_DetachReset;

public class FotaStage_05_DetachResetRelay extends FotaStage_05_DetachReset {
    public FotaStage_05_DetachResetRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.RESPONSE;

        mRelayRaceId = RaceId.RACE_SOFTWARE_RESET;
        mRelayRaceRespType = RaceType.RESPONSE;

        // not giving indication with wrapped header
        mIsRelay = false;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
    }
}
