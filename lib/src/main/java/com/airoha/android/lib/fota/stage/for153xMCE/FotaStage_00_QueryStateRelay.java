package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.constant.Recipient;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.forSingle.RaceCmdQueryState;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.fota.stage.forSingle.FotaStage_00_QueryState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FotaStage_00_QueryStateRelay extends FotaStage_00_QueryState {

    private byte[] mRecipients;

    public FotaStage_00_QueryStateRelay(AirohaRaceOtaMgr mgr, final byte[] recipients) {
        super(mgr, recipients);

        mRaceId = RaceId.RACE_RELAY_PASS_TO_DST;
        mRaceRespType = RaceType.INDICATION;

        mRelayRaceId = RaceId.RACE_FOTA_QUERY_STATE;
        mRelayRaceRespType = RaceType.INDICATION;

        mRecipients = recipients;

        mIsRelay = true;
    }


    @Override
    protected void placeCmd(RacePacket cmd) {
        RacePacket relayCmd = createWrappedRelayPacket(cmd);
        mCmdPacketQueue.offer(relayCmd);
        mCmdPacketMap.put(TAG, relayCmd); // only one cmd needs to check resp
    }

    @Override
    protected void passInfoToMgr(RespFotaState[] respFotaStates) {
        for (RespFotaState respFotaState : respFotaStates) {
            switch (respFotaState.Recipient) {
                case Recipient.DontCare:
                    mOtaMgr.setClientFotaState(respFotaState.FotaState);
                    break;

                default:
                    break;
            }
        }
    }
}
