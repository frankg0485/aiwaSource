package com.airoha.android.lib.transport.PacketParser;

/**
 * Created by MTK60279 on 2018/2/6.
 */

public interface OnRacePacketListener {
    void handleRespOrInd(int raceId, final byte[] packet, int raceType);
}
