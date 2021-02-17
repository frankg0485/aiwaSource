package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_13_GetPartitionEraseStatusStorage;

import java.io.InputStream;

public class FotaStage_13_GetPartitionEraseStatusStorageRelay extends FotaStage_13_GetPartitionEraseStatusStorage {
    public FotaStage_13_GetPartitionEraseStatusStorageRelay(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_GET_4K_ERASED_STATUS;;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;
    }
    public FotaStage_13_GetPartitionEraseStatusStorageRelay(AirohaRaceOtaMgr mgr, InputStream ios) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_STORAGE_GET_4K_ERASED_STATUS;;
        mRelayRaceRespType = RaceType.INDICATION;

        mIsRelay = true;

        mInputStream = ios;
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);

        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        super.parsePayloadAndCheckCompeted(raceId, packet, status, raceType);
    }
}
