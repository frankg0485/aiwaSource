package com.airoha.android.lib.AntennaUT;

import java.util.concurrent.ConcurrentHashMap;

public class AntennaUTListenerMgr {
    private ConcurrentHashMap<String, AntennaUtLogUiListener> mListenerMap;

    public AntennaUTListenerMgr() {
        mListenerMap = new ConcurrentHashMap<>();
    }

    public synchronized void addListener(String name, AntennaUtLogUiListener listener) {
        if (name == null || listener == null) return;
        mListenerMap.put(name, listener);
    }

    public synchronized void removeListener(String name) {
        if (name == null) return;
        mListenerMap.remove(name);
    }

    public synchronized void OnAddLog(boolean is_agent, String msg) {
        for (AntennaUtLogUiListener listener : mListenerMap.values()) {
            listener.OnAddLog(is_agent, msg);
        }
    }

    public synchronized void OnReportStop() {
        for (AntennaUtLogUiListener listener : mListenerMap.values()) {
            listener.OnReportStop();
        }
    }

    public synchronized void OnStatisticsReport(String msg) {
        for (AntennaUtLogUiListener listener : mListenerMap.values()) {
            listener.OnStatisticsReport(msg);
        }
    }
}
