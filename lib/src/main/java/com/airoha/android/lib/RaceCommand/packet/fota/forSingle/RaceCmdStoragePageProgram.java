package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;

/**
 * Created by MTK60279 on 2018/2/7.
 */ // TODO FOTA 4
public class RaceCmdStoragePageProgram extends RacePacket {

    private byte mStoageType;
    private byte mPageCount;
    private StoragePageData[] mStoragePageData;

    public RaceCmdStoragePageProgram(byte storageType, byte pageCount, StoragePageData[] storagePageData) {
        super(RaceType.CMD_NEED_RESP, RaceId.RACE_STORAGE_PAGE_PROGRAM);

        mStoageType = storageType;
        mPageCount = pageCount;
        mStoragePageData = storagePageData;

        byte[] payload = new byte[2 + 261*pageCount];

        //"StorageType  (1 byte),
        //PageCount (1 byte),
        //{
        //CRC (1 byte),
        //StorageAddress (4 bytes),
        //Data[256]
        //} [%PageCount%]"

        payload[0] = mStoageType;
        payload[1] = mPageCount;

        assert mPageCount ==1;

        for(int i = 0; i<mPageCount; i++) {
            System.arraycopy(mStoragePageData[i].toRaw(), 0, payload, 2+261*i, 261);
        }

        setPayload(payload);


//        byte[] addr = {payload[1], payload[2], payload[3], payload[4]};
//
        setAddr(mStoragePageData[0].StorageAddress);
    }

}
