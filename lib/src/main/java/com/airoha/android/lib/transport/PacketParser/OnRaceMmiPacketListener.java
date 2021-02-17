package com.airoha.android.lib.transport.PacketParser;

public interface OnRaceMmiPacketListener {
    void OnAncSetOnResp(final byte resp);
    void OnAncSetOffResp(final byte resp);
    void OnAncSetGainResp(final byte resp);

    void OnAncGetStatusResp(byte resp);

    void OnAncReadParamFromNvKeyResp(byte resp);

    void OnAncWriteGainToNvKeyResp(byte resp);

    void OnAncReadParamFromNvKeyInd(byte[] payload);
}
