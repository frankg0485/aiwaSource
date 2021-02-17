package com.airoha.android.lib.AntennaUT;

public interface AntennaUtLogUiListener {
    void OnAddLog(final boolean is_agent, final String msg);
    void OnReportStop();
    void OnStatisticsReport(String msg);
}
