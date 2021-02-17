package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdGetAvaDst;
import com.airoha.android.lib.fota.fotaError.FotaErrorMsg;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;

import java.util.ArrayList;
import java.util.List;

public class PeqStageGetAvaDst extends PeqStage {
    public PeqStageGetAvaDst(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_GET_AVA_DST;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        return new RaceCmdGetAvaDst();
    }

    @Override
    protected void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        // logic start
        // Rx packet :  05 5B 08 00 00 0D [00 00 05 04 03 03]

        List<Dst> dstList = new ArrayList<>();
        for (int i = RacePacket.IDX_PAYLOAD_START; i < packet.length - 1; i = i + 2) {
            Dst dst = new Dst();
            dst.Type = packet[i];
            dst.Id = packet[i + 1];
            dstList.add(dst);
        }


        Dst awsPeerDst = null;
        for (Dst dst : dstList) {
            if (dst.Type == AvailabeDst.RACE_CHANNEL_TYPE_AWSPEER) {
                awsPeerDst = dst;
                break;
            }
        }

        if (awsPeerDst == null) {
            // do error handling
//            mIsErrorOccurred = true;
//            mStrErrorReason = FotaErrorMsg.PartnerNotExisting;
            mAirohaLink.logToFile(TAG, "partner not existing");
        }

        // pass to manager, null is OK
        mPeqMgr.setAwsPeerDst(awsPeerDst);


        mIsCompleted = true;
    }
}
