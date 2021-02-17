package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

/**
 * Airoha internal use
 */
public class PeqStageSaveUiDataRelay extends PeqStageSaveUiData {

    public PeqStageSaveUiDataRelay(AirohaPeqMgr mgr) {
        super(mgr);

        mIsRelay = true;
    }
}
