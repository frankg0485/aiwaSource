package com.airoha.android.lib.physical.spp;

import com.airoha.android.lib.transport.AirohaLink;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by MTK60279 on 2017/11/30.
 */

public class AirohaSppControllerCh3 extends AirohaSppController {
    private static final String TAG = "AirohaSppControllerR";

    public AirohaSppControllerCh3(AirohaLink airohaLink) {
        super(airohaLink);
    }

    @Override
    protected UUID getConnUUID() {
        return ((AirohaLink)mAirohaLink).UUID_AIROHA1520_CH3;
    }

    @Override
    protected void handleInputStream(RespIndPacketBuffer mmRespIndCmr) throws IOException {
        int len = mInStream.available();
//                    Log.d(TAG, "input stream available: " + len);

        if (len > 0) {
            byte[] bufferData = new byte[len];

            mInStream.read(bufferData);

            mAirohaLink.handlePhysicalPacket(bufferData);
        }
    }
}
