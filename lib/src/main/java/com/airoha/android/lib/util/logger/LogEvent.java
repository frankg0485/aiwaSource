package com.airoha.android.lib.util.logger;

public class LogEvent {
    public static final int LOG_STRING_WITH_TIMESTAMP = 0;
    public static final int LOG_RAW = 1;
    public static final int LOG_DUMP = 2;

    public int logType;
    public String logName;
    public String logStr;
    public String tag;
    public byte[] logRaw;
    public Boolean isOutputToLogi;
    public Boolean isOutputToFile;

}
