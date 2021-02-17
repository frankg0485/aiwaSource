package com.airoha.android.lib.mmi.stage;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.mmi.AirohaMmiMgr;

public class MmiStageResetRelay extends MmiStageReset {
    public MmiStageResetRelay(AirohaMmiMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_SOFTWARE_RESET;
        mRelayRaceRespType = RaceType.CMD_NO_RESP;
        mIsRelay = true;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = createWrappedRelayPacket(new RacePacket(RaceType.CMD_NO_RESP, RaceId.RACE_SOFTWARE_RESET));
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }
}
