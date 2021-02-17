package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.NvKeyId;
import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Airoha internal use
 */
public class PeqStageReadAudiPath extends PeqStage {

    public PeqStageReadAudiPath(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_READFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_READFULLKEY);

        byte[] queryId = Converter.shortToBytes((short) NvKeyId.AUDIO_PATH);
        byte[] queryLength = Converter.shortToBytes((short) 1000); // 0x03E8

        byte[] payload = new byte[]{queryId[0], queryId[1], queryLength[0], queryLength[1]};

        cmd.setPayload(payload);

        mAirohaLink.logToFile(TAG, "cmd: " + Converter.byte2HexStr(payload));

        return cmd;
    }

    @Override
    protected void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "rx packet: " + Converter.byte2HexStr(packet));

        // length idx 2~3
//        byte[] bLength = new byte[2];
//        System.arraycopy(packet, 2, bLength, 0, 2);

        //05 5B 0A 00 00 0A 06 00 [01 00 00 00 34 F2]
        // 01 00 : number of sets: always 1
        // 00 00: peq phase, no use in app side
        // 34 F2: setToNvkey => need to memory this

        int nvValuePayloadLength = Converter.BytesToU16(packet[7], packet[6]);

        byte[] readNvPayloadValue = new byte[nvValuePayloadLength];
        // parse payload from idx 6
        System.arraycopy(packet, 8, readNvPayloadValue, 0, nvValuePayloadLength);

        boolean isNvContentEmpty = true;

        for (byte b : readNvPayloadValue) {
            if (b != 0x00) {
                isNvContentEmpty = false;
                break;
            }
        }

        // check, could be empty, then use 0xF234
        if (isNvContentEmpty) {
            mAirohaLink.logToFile(TAG, "no default audio path, use 0xF234");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] defaultKey = Converter.shortToBytes((short)0xF234);

            try {
                bos.write(new byte[]{0x01, 0x00});
                bos.write(new byte[]{0x00, 0x00});
                bos.write(defaultKey);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mPeqMgr.setAudioPathTargetNvKey(defaultKey);

            mPeqMgr.setAudioPathWriteBackContent(bos.toByteArray());

        } else {

            // 01 00 : number of sets: always 1
            // 00 00: peq phase, no use in app side
            // 34 F2: setToNvkey => need to memory this

            byte[] defaultKey = new byte[]{readNvPayloadValue[4], readNvPayloadValue[5]};

            mPeqMgr.setAudioPathTargetNvKey(defaultKey);

            mPeqMgr.setAudioPathWriteBackContent(readNvPayloadValue);
        }

        mIsCompleted = true;
    }
}
