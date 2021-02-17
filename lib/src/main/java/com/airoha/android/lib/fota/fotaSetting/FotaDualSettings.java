package com.airoha.android.lib.fota.fotaSetting;

import com.airoha.android.lib.fota.actionEnum.DualActionEnum;
import com.airoha.android.lib.fota.actionEnum.SingleActionEnum;

public class FotaDualSettings {
    // discussed FOTA_Design_V0.9.3_draft
    public DualActionEnum actionEnum;

    /**
     * PartitionType, FOTA or FileSystem
     */
    public PartitionType partitionType = PartitionType.Fota;

    /**
     * EraseSlidingWindow, Command sliding window size for erase stage
     */
    public int slidingWindow = 4;

    /**
     * PowerMode, Enable or disable change to normal power mode
     */
    public boolean enableNormalPowerMode = true;

    /**
     * ActiveFOTA, Enable or disable active FOTA
     */
    public boolean enableActiveFota = false;

    /**
     * BatteryThreshold, Battery level threshold for FOTA pre-checking
     */
    public int batteryThreshold = 20;


    /**
     * ProgramInterval, Command interval for program stage
     */
    public int programInterval = 0;
}
