package com.airoha.android.lib.peq;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Airoha internal use
 */
public class PeqStageHostAudioSaveStatus extends PeqStage {

    public PeqStageHostAudioSaveStatus(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_HOSTAUDIO_PEQ_SAVE_STATUS;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // component (1 byte)
        //    A2DP = 0x00,
        //    line-in = 0x01,
        //    mp3 = 0x02;
        // nvkey ID (2 byte)
        bos.write((byte)0x00);
        try {
            bos.write(mPeqMgr.getPeqCoefTargetNvKey());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] payload = bos.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_HOSTAUDIO_PEQ_SAVE_STATUS, payload);

        return cmd;
    }
}
