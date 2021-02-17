package com.airoha.android.lib.peq;

import android.graphics.Path;
import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageReclaimNvkey extends PeqStage {
    private static final String TAG = "PeqStageReclaimNvkey";

    private short mReclaimSize = 0;

//    public PeqStageReclaimNvkey(AirohaPeqMgr mgr, short reclaimSize) {
//        super(mgr);
//
//        mReclaimSize = reclaimSize;
//
//        mRaceId = RaceId.RACE_NVKEY_RECLAIM;
//        mRaceRespType = RaceType.RESPONSE;
//    }

    public enum Option{
        SaveCoef,
        SavePeqPath,
        SaveAudioPath,
        SaveUiData,
    }

    private Option mReclaimOption;

    public PeqStageReclaimNvkey(AirohaPeqMgr mgr, Option option) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_RECLAIM;
        mRaceRespType = RaceType.RESPONSE;

        mReclaimOption = option;
    }

    @Override
    protected RacePacket genCmd() {
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_RECLAIM);

        int reclaimSize =0;

        switch (mReclaimOption){
            case SaveCoef:

                reclaimSize = mPeqMgr.getSaveCoefPaload().length;
                break;

            case SavePeqPath:

                reclaimSize = mPeqMgr.getWriteBackPeqSubsetContent().length;
                break;

            case SaveAudioPath:

                reclaimSize = mPeqMgr.getAudioPathWriteBackContent().length;
                break;
        }


        byte[] payload = Converter.shortToBytes((short)reclaimSize);

        cmd.setPayload(payload);

        return cmd;
    }

    @Override
    protected void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        //0x00, fail!
        //!= 0x00, success!

        if(status != 0x00){
            mIsCompleted = true;
        }else {
            mIsError = true;
        }
    }
}
