package com.airoha.android.lib.mmi.stage;

import com.airoha.android.lib.RaceCommand.constant.AvailabeDst;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.RaceCommand.packet.fota.for153xMCE.RaceCmdGetAvaDst;
import com.airoha.android.lib.fota.stage.for153xMCE.Dst;
import com.airoha.android.lib.mmi.AirohaMmiMgr;

import java.util.ArrayList;
import java.util.List;

public class MmiStageGetAvaDst extends MmiStage {
    public MmiStageGetAvaDst(AirohaMmiMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_RELAY_GET_AVA_DST;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        RacePacket cmd = new RaceCmdGetAvaDst();
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
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
            mAirohaLink.logToFile(TAG, "partner not existing");
        }

        // pass to manager, null is OK
        mMmiMgr.setAwsPeerDst(awsPeerDst);


//        mIsCompleted = true;
    }
}
