package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

/**
 * Airoha internal use
 */
public class PeqStageLoadUiData extends PeqStage {

    private byte[] mQueryId;

    public PeqStageLoadUiData(AirohaPeqMgr mgr, byte[] queryId) {
        super(mgr);

        mQueryId = queryId;

        mRaceId = RaceId.RACE_NVKEY_READFULLKEY;
        mRaceRespType =RaceType.RESPONSE;
    }

    @Override
    protected RacePacket genCmd() {
        return genReadNvKeyPacket(mQueryId);
    }

    @Override
    protected void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, Converter.byte2HexStr(packet));

        try{

            // [GVA-9286] FW could response some packet, but too short to parse
            if(packet.length < 21) {
                mPeqMgr.notifyOnLoadPeqUiData(null);
                mIsCompleted = true;
                return;
            }

            byte[] raw = new byte[packet.length - 8];

            System.arraycopy(packet, 8, raw, 0, raw.length);

            PeqUiDataStru peqUiDataStru = new PeqUiDataStru(raw);

            mPeqMgr.notifyOnLoadPeqUiData(peqUiDataStru);
        }catch (IndexOutOfBoundsException e){
            mPeqMgr.notifyOnLoadPeqUiData(null);
        }

        mIsCompleted = true;
    }
}
