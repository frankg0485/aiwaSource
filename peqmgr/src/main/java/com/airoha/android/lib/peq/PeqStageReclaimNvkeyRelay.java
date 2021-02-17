package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageReclaimNvkeyRelay extends PeqStageReclaimNvkey {

//    public PeqStageReclaimNvkeyRelay(AirohaPeqMgr mgr, short reclaimSize) {
//        super(mgr, reclaimSize);
//
//        mIsRelay = true;
//    }

    public PeqStageReclaimNvkeyRelay(AirohaPeqMgr mgr, Option option) {
        super(mgr, option);

        mIsRelay = true;
    }
}
