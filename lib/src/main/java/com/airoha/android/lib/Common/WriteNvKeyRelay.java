package com.airoha.android.lib.Common;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;

public class WriteNvKeyRelay extends WriteNvKey {
    public WriteNvKeyRelay(AirohaRaceOtaMgr mgr, short nvkey_id, byte[]nvkey_value) {
        super(mgr, nvkey_id, nvkey_value);
        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
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