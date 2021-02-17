package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageSaveCoef extends PeqStage {

    public PeqStageSaveCoef(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        byte[] coef = genWriteNvKeyPacket(mPeqMgr.getPeqCoefTargetNvKey(), mPeqMgr.getSaveCoefPaload()).getRaw();
        mAirohaLink.logToFile(TAG, "coef: " + Converter.byte2HexStr(coef));
        return genWriteNvKeyPacket(mPeqMgr.getPeqCoefTargetNvKey(), mPeqMgr.getSaveCoefPaload());
    }
}
