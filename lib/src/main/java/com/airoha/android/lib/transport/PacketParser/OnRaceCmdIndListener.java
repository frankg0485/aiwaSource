package com.airoha.android.lib.transport.PacketParser;

public interface OnRaceCmdIndListener {
    void handleInd(int raceId, final byte[] packet);
}
