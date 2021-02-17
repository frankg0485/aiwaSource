package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Airoha internal use
 */
public class PeqStageHostAudioSaveStatusRelay extends PeqStageHostAudioSaveStatus {

    public PeqStageHostAudioSaveStatusRelay(AirohaPeqMgr mgr) {
        super(mgr);

        mIsRelay = true;
    }
}
