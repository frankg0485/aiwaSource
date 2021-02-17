package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.NvKeyId;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageUpdateAudioPathRelay extends PeqStageUpdateAudioPath {

    public PeqStageUpdateAudioPathRelay(AirohaPeqMgr mgr) {
        super(mgr);

        mIsRelay = true;
    }

}
