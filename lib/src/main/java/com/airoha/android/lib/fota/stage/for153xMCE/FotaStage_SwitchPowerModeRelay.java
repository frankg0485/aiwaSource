package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_SwitchPowerMode;

public class FotaStage_SwitchPowerModeRelay extends FotaStage_SwitchPowerMode {

    public FotaStage_SwitchPowerModeRelay(AirohaRaceOtaMgr mgr, byte modeEnum) {
        super(mgr, modeEnum);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_SWITCH_POWER_MODE;
        mRelayRaceRespType = RaceType.RESPONSE;

        mPowerMode = modeEnum;

        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);

        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }
}
