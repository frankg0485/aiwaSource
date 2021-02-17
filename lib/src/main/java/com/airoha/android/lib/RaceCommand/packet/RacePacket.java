package com.airoha.android.lib.RaceCommand.packet;

import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MTK60279 on 2018/2/5.
 */

public class RacePacket {
    public static final int IDX_PAYLOAD_START = 6;
    private static final String TAG = "RacePacket";

    public static final byte START_CHANNEL_BYTE = (byte) 0x05;

    private byte mbType;
    private int mLength;
    private byte[] mbArrLength = new byte[2];
    private byte[] mbArrID = new byte[2];
    private int mRaceId;
    private byte[] mbArrPayload;

    private volatile boolean mIsRespStatusSuccess;
    private int mRetryCounter;


    protected byte[] mbAddr = new byte[4];
    protected byte[] mClientAddr = new byte[4];
    protected byte mStatus = (byte) 0xFF;

    private String mQueryKey;

    public RacePacket(final byte type, final byte[] arrayId, final byte[] payload){
        mbType = type;

        mbArrID = arrayId;

        mRaceId = (arrayId[0] & 0xFF) | ((arrayId[1] & 0xFF) << 8);

        setPayload(payload);
    }


    public RacePacket(final byte type, int id, final byte[] payload) {
        mbType = type;
        mRaceId = id;

        mbArrID = new byte[]{(byte) (id & 0xFF), (byte) ((id >> 8) & 0xFF)};
//        mbArrPayload = payload;
//
//        mLength = mbArrID.length;
//        if (payload != null) {
//            mLength = mbArrID.length + payload.length;
//            mbArrPayload = payload;
//        }
//
//        mbArrLength[0] = (byte) (mLength & 0xFF);
//        mbArrLength[1] = (byte) ((mLength >> 8) & 0xFF);

        setPayload(payload);
    }

    public RacePacket(final byte type, int id) {
        this(type, id, null);
    }


    public void setPayload(byte[] payload){
        mbArrPayload = payload;

        mLength = mbArrID.length;
        if (payload != null) {
            mLength = mbArrID.length + payload.length;
            mbArrPayload = payload;
        }

        mbArrLength[0] = (byte) (mLength & 0xFF);
        mbArrLength[1] = (byte) ((mLength >> 8) & 0xFF);
    }

    // 05 5B 07 00 04 07 00 00 C0 16 00
    // only used in erase program resp handle
    public RacePacket(byte[] rawRespPacket) {
        System.arraycopy(rawRespPacket, 7, mbAddr, 0, mbAddr.length);
        mStatus = rawRespPacket[6];
    }

    public byte[] getRaw() {
        List<Byte> list = new ArrayList<>();

        list.add(START_CHANNEL_BYTE);

        list.add(mbType);

        list.add(mbArrLength[0]);
        list.add(mbArrLength[1]);

        list.add(mbArrID[0]);
        list.add(mbArrID[1]);

        if (mbArrPayload != null) {
            for (byte b : mbArrPayload) {
                list.add(b);
            }
        }

        Byte[] arrB = list.toArray(new Byte[list.size()]);

        byte[] arrb = new byte[arrB.length];

        for (int i = 0; i < arrb.length; i++) {
            arrb[i] = arrB[i];
        }
        return arrb;
    }


    synchronized public boolean isRespStatusSuccess() {
        return mIsRespStatusSuccess;
    }

    synchronized public void setIsRespStatusSuccess() {
        mIsRespStatusSuccess = true;
    }

    public void increaseRetryCounter() {
        mRetryCounter++;
        Log.d(TAG, "retryCounter:" + mRetryCounter);
    }

    public boolean isRetryUpperLimit() {
        return (mRetryCounter >= 3);
    }

    public byte[] getAddr() {
        return mbAddr;
    }

    public void setAddr(byte[] addr) {
        mbAddr = addr;
    }

    public byte[] getClientAddr() {
        return mClientAddr;
    }

    public void setClientAddr(byte[] addr) {
        mClientAddr = addr;
    }

    public String toHexString(){
        return Converter.byte2HexStr(getRaw());
    }

    public boolean isNeedResp(){
        if(mbType == RaceType.CMD_NEED_RESP)
            return true;

        return false;
    }

    public int getRaceId(){
        return mRaceId;
    }

    public void setQueryKey(String key){
        mQueryKey = key;
    }

    public String getQueryKey(){
        return mQueryKey;
    }
}
