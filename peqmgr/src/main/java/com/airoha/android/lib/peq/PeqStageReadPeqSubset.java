package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Airoha internal use
 */
public class PeqStageReadPeqSubset extends PeqStage {
    private static final String TAG = "PeqStageReadPeqSubset";


    public PeqStageReadPeqSubset(AirohaPeqMgr mgr) {
        super(mgr);

        mRaceId = RaceId.RACE_NVKEY_READFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        return genReadNvKeyPacket(mPeqMgr.getAudioPathTargetNvKey());
    }

    @Override
    protected void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "rx packet: " + Converter.byte2HexStr(packet));

        int nvValuePayloadLength = Converter.BytesToU16(packet[7], packet[6]);

        byte[] readNvPayloadValue = new byte[nvValuePayloadLength];

        // 05 5B 0C 00 00 0A 08 00 [01 00 01 00 00 00 60 F2]
        // 01 00 number of sets
        // [01 00 00 00] [60 F2]

        System.arraycopy(packet, 8, readNvPayloadValue, 0, nvValuePayloadLength);

        boolean isNvContentEmpty = true;

        for (byte b : readNvPayloadValue) {
            if (b != 0x00) {
                isNvContentEmpty = false;
                break;
            }
        }

        // check, could be empty
        if (isNvContentEmpty) {
            mAirohaLink.logToFile(TAG, "no default peq subset");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                bos.write(new byte[]{0x01, 0x00});
                bos.write(new byte[]{0x01, 0x00, 0x00, 0x00});
                bos.write(mPeqMgr.getPeqCoefTargetNvKey());
            } catch (IOException e) {
                e.printStackTrace();
            }

            mPeqMgr.setWriteBackPeqSubsetContent(bos.toByteArray());
        } else {

            // convert to hex string
            String contentHexWithoutSeperator = Converter.byte2HexStrWithoutSeperator(readNvPayloadValue);

            // check if this subset insisting
            String targetPeqSubset = Converter.byte2HexStrWithoutSeperator(mPeqMgr.getPeqCoefTargetNvKey());

            if(contentHexWithoutSeperator.contains(targetPeqSubset)){
                mAirohaLink.logToFile(TAG, "target subset existing");

                mPeqMgr.setWriteBackPeqSubsetContent(readNvPayloadValue);

            }else {
                mAirohaLink.logToFile(TAG, "append target subset to write back");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                // modify number of sets
                int numberOfSets = Converter.BytesToU16(readNvPayloadValue[1], readNvPayloadValue[0]);

                numberOfSets = numberOfSets + 1;
                mAirohaLink.logToFile(TAG, "number of sets: " + numberOfSets);

                byte[] bytesNumOfSets = Converter.shortToBytes((short)numberOfSets);

                readNvPayloadValue[0] = bytesNumOfSets[0];
                readNvPayloadValue[1] = bytesNumOfSets[1];

                try {
                    bos.write(readNvPayloadValue);

                    // append
                    bos.write(new byte[]{0x01, 0x00, 0x00, 0x00});
                    bos.write(mPeqMgr.getPeqCoefTargetNvKey());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPeqMgr.setWriteBackPeqSubsetContent(bos.toByteArray());
            }
        }

        mIsCompleted = true;
    }

}
