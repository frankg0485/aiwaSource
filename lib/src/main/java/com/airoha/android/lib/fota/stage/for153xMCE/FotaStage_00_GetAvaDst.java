package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdGetAvaDst;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.fotaError.FotaErrorMsg;
import com.airoha.android.lib.fota.stage.FotaStage;

import java.util.ArrayList;
import java.util.List;

public class FotaStage_00_GetAvaDst extends FotaStage {
    public FotaStage_00_GetAvaDst(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_GET_AVA_DST;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = new RaceCmdGetAvaDst();
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {


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
        } else {
            // pass to manager
            mOtaMgr.setAwsPeerDst(awsPeerDst);

            RacePacket cmd = mCmdPacketMap.get(TAG);
            cmd.setIsRespStatusSuccess();

            mIsRespSuccess = true;
        }

    }
}
