package com.airoha.android.lib.AntennaUT;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;

public class AntennaInfoRptRelay extends AntennaInfoRpt {

    public AntennaInfoRptRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_ANTENNAUT_REPORT_ENABLE;
        mRelayRaceRespType = RaceType.RESPONSE;
        mIsRelay = true;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }
}