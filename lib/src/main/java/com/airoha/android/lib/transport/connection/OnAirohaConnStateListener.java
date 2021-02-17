package com.airoha.android.lib.transport.connection;

/**
 * There is no Android's origin event for SPP state changes.
 * This is for listening SPP Socket I/O state changes of {@link com.airoha.android.lib.transport.AirohaLink}
 */

public interface OnAirohaConnStateListener {
    void OnConnected(String type);

    void OnConnectionTimeout();

    void OnDisconnected();

    void OnConnecting();

    void OnDisConnecting();

    void OnUnexpectedDisconnected();
}
