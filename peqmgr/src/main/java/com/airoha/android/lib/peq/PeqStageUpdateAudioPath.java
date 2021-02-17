package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.NvKeyId;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageUpdateAudioPath extends PeqStage {
    private static final String TAG = "PeqStageUpdateAudioPath";

    public PeqStageUpdateAudioPath(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_WRITEFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        return genWriteNvKeyPacket(Converter.shortToBytes((short) NvKeyId.AUDIO_PATH),
                mPeqMgr.getAudioPathWriteBackContent());
    }
}
