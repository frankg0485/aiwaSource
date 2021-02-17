package com.airoha.android.lib.mmi;

import java.util.List;

/**
 * App should register and listen to the callback for the API response.
 * {@link AirohaMmiMgr}
 */
public interface OnAirohaMmiClientAppListener {
    void OnRespSuccess(final String stageName);
    void OnFindMeState(final byte state);
    void OnBattery(final byte role, final byte level);
    void OnAncTurnOn(final byte status);
    void OnPassThrough(final byte status);
    void OnAncTurnOff(final byte status);
    void OnGameModeStateChanged(final boolean isEnabled);
    void notifyAgentIsRight(final boolean isRight);
    void notifyPartnerIsExisting(final boolean isExisting);
    void notifyQueryVpLanguage(final List<String> vpList);
    void notifyGetVpIndex(final byte index);
    void notifySetVpIndex(final boolean status);
    void notifyAncStatus(final byte status);
    void notifyGameModeState(final byte state);
    void notifyResetState(final byte role, final byte state);
}
