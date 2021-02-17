package com.airoha.android.lib.peq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PeqUiDataStru {
    private byte mByteIsKeepRescale = 0x01;
    private byte[] mBytesRescaleValue = new byte[] {0x00, 0x00};
    private List<PeqBandInfo> mArrPeqBandInfo;

    /**
     * Client App constructs this object with the array of {@link PeqBandInfo}
     * This object will be used both in Client App and {@link AirohaPeqMgr}
     * @param peqBandInfos
     */
    public PeqUiDataStru(PeqBandInfo[] peqBandInfos){
//        mArrPeqBandInfo = peqBandInfos;

        mArrPeqBandInfo = Arrays.asList(peqBandInfos);
    }

    /**
     *  Airoha internal use
     * @param raw
     */
    public PeqUiDataStru(byte[] raw) {
        mByteIsKeepRescale = raw[0];

        mBytesRescaleValue = new byte[] {raw[1], raw[2]};


        mArrPeqBandInfo = new LinkedList<>();
        for(int i = 3; i< raw.length;) {
            byte[] subRaw = new byte[18];

            System.arraycopy(raw, i, subRaw, 0, 18);

            PeqBandInfo peqBandInfo = new PeqBandInfo(subRaw);

            mArrPeqBandInfo.add(peqBandInfo);

            i = i + 18;
        }
    }

    /**
     * Client App will call this to parse the {@link PeqBandInfo} to display on UI
     * after getting the call back from
     * {@link AirohaPeqMgr.OnPeqStatusUiListener#OnLoadPeqUiData(PeqUiDataStru)}
     * @return
     */
    public List<PeqBandInfo> getPeqBandInfoList() {
        return mArrPeqBandInfo;
    }

    /**
     * Airoha internal use
     * @return
     */
    public byte[] getRaw() {
        List<Byte> byteList = new ArrayList<>();

        byteList.add(mByteIsKeepRescale);

        for(byte b : mBytesRescaleValue) {
            byteList.add(b);
        }

        for(PeqBandInfo peqBandInfo : mArrPeqBandInfo) {
            for(byte b : peqBandInfo.getRaw()){
                byteList.add(b);
            }
        }

        byte[] result = new byte[byteList.size()];

        for(int i = 0; i< byteList.size(); i++) {
            result[i] = byteList.get(i);
        }

        return result;
    }
}
