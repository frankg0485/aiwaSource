package com.airoha.android.lib.transport.PacketParser;

/**
 * Listen for type:5D, id:0CD7
 */
public interface OnRaceMmiRoleSwitchIndListener {
    void OnRoleSwitched(final byte status);
}
