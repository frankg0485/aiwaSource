package com.airoha.android.lib.RaceCommand.constant;

/**
 * Created by MTK60279 on 2018/2/22.
 */

public class RaceId {
    public static final int RACE_GET_POWER_MODE = 0x020C;
    public static final int RACE_SWITCH_POWER_MODE = 0x020D;

    public static final int RACE_SPIFLASH_DUAL_DEVICES_PARTITION_ERASE = 0x0413;

    public static final int RACE_FLASH_SET_PROTECT_BIT = 0x0702;
    public static final int RACE_FLASH_SECTOR_ERASE = 0x0704;
    public static final int RACE_FLASH_PAGE_PROGRAM = 0x0707;

    public static final int RACE_FLASH_DUAL_DEVICES_PARTITION_ERASE = 0x070F;// with INDICATION

    // component (1 byte)
    //    A2DP = 0x00,
    //    line-in = 0x01,
    //    mp3 = 0x02;
    //nvkey ID (2 byte)
    public static final int RACE_HOSTAUDIO_PEQ_SAVE_STATUS = 0x09FD;

    public static final int RACE_NVKEY_WRITEFULLKEY = 0x0A01;
    public static final int RACE_NVKEY_READFULLKEY = 0x0A00;
    public static final int RACE_NVKEY_RECLAIM = 0x0A03;
    public static final int RACE_RELOAD_NVKEY_TO_RAM = 0x0A09;

    public static final int RACE_GET_BATTERY = 0x0C02; // for single
    public static final int RACE_BLUETOOTH_GET_CLIENT_EXISTENCE = 0x0CD3;
    public static final int RACE_BLUETOOTH_IS_AGENT_RIGHT_DEVICE = 0x0CD4;
    public static final int RACE_BLUETOOTH_GET_BATTERY = 0x0CD6; // for tws // with INDICATION
    public static final int RACE_BLUETOOTH_ROLE_SWITCH = 0x0CD7;

    public static final int RACE_RELAY_GET_AVA_DST = 0x0D00;
    public static final int RACE_RELAY_PASS_TO_DST = 0x0D01;

    public static final int RACE_SUSPEND_DSP = 0x0E01;
    public static final int RACE_RESUME_DSP = 0x0E02;
    public static final int DSP_REALTIME_PEQ = 0x0E03;

    public static final int RACE_ANC_ON = 0x1200;
    public static final int RACE_ANC_OFF = 0x1201;
    public static final int RACE_ANC_GET_STATUS = 0x1202;
    public static final int RACE_ANC_SET_GAIN = 0x1203;
    public static final int RACE_ANC_READ_PARAM_FROM_NVKEY = 0x1204;
    public static final int RACE_ANC_WRITE_GAIN_TO_NVKEY = 0x1205;

    public static final int RACE_FOTA_PARTITION_INFO_QUERY = 0x1C00;
    public static final int RACE_FOTA_INTEGRITY_CHECK = 0x1C01;
    public static final int RACE_FOTA_COMMIT = 0x1C02;
    public static final int RACE_FOTA_STOP = 0x1C03;
    public static final int RACE_FOTA_QUERY_STATE = 0x1C04;
    // 2018.04.02 Daniel - replace with 0x1C05 for V29+ release
    public static final int RACE_SOFTWARE_RESET = 0x1C05;
    public static final int RACE_FOTA_WRITE_STATE = 0x1C06;
    public static final int RACE_FOTA_GET_VERSION = 0x1C07;

    public static final int RACE_FOTA_START = 0x1C08;

    public static final int RACE_FOTA_START_TRANSCATION = 0x1C0A;

    public static final int RACE_FOTA_GET_INTERNAL_FLASH_PARTITION_SHA256 = 0x1C0F; // => 0x0430

    // 2018.04.30 Daniel - for TWS
    public static final int RACE_FOTA_DUAL_DEVICES_START_TRANSACTION = 0x1C10;// with INDICATION
    public static final int RACE_FOTA_DUAL_DEVICES_COMMIT = 0x1C11;
    public static final int RACE_FOTA_DUAL_DEVICES_QUERY_STATE = 0x1C12;// with INDICATION
    public static final int RACE_FOTA_DUAL_DEVICES_WRITE_STATE = 0x1C13;// with INDICATION
    public static final int RACE_FOTA_DUAL_DEVICES_QUERY_PARTITION_INFO = 0x1C14;// with INDICATION
    public static final int RACE_FOTA_DUAL_DEVICES_CANCEL = 0x1C15;
    public static final int RACE_FOTA_GET_PARTITION_ERASE_STATUS = 0x1C16;// with INDICATION

    public static final int RACE_FOTA_ACTIVE_FOTA_PREPARATION = 0x1C19;


    // 2018.08.10 Daniel: add new cmds for internal/external commonly used
    public static final int RACE_STORAGE_LOCK_UNLOCK = 0x0430; // dual
    public static final int RACE_STORAGE_GET_PARTITION_SHA256 = 0x0431; // dual, replace: RACE_FOTA_GET_INTERNAL_FLASH_PARTITION_SHA256
    public static final int RACE_STORAGE_DUAL_DEVICES_PARTITION_ERASE = 0x0432; // dual, replace: RACE_FLASH_DUAL_DEVICES_PARTITION_ERASE
    public static final int RACE_STORAGE_GET_4K_ERASED_STATUS = 0x0433;// dual, replace: RACE_FOTA_GET_PARTITION_ERASE_STATUS


    public static final int RACE_STORAGE_READ_MANUFACTURER_AND_MEMORYTYPE = 0x04A0;
    public static final int RACE_STORAGE_SET_CONFIGURATION_REGISTER = 0x04A1;
    public static final int RACE_STORAGE_PARTITION_ERASE = 0x0404; // single
    public static final int RACE_STORAGE_PAGE_PROGRAM = 0x0402; // sigle
    public static final int RACE_STORAGE_PAGE_READ = 0x0403; // single
    public static final int RACE_STORAGE_BYTE_PROGRAM = 0x0400; // single
    public static final int RACE_STORAGE_EMPTY_KE = 0x04F0;
    public static final int RACE_AIRDUMP_ONOFF = 0x0E0B;
    public static final int RACE_ANTENNAUT_REPORT_ENABLE = 0x1700;

    public static final int RACE_HOSTAUDIO_MMI_GET_ENUM = 0x0901;
    public static final int RACE_MMI_KEY_COMMAND = 0x1101;
    public static final int RACE_FOTA_GET_AE_INFO = 0x1C09;

    // debugging service
    public static final int RACE_GET_BOOT_REASON = 0x1E00;
    public static final int RACE_GET_DUMP_ADDR = 0x1E02;
    public static final int RACE_GET_OFFLINE_DUMP_ADDR = 0x1E06;
    public static final int RACE_TOOL_ASSERT = 0x1E07;
    public static final int RACE_GET_BUILD_VERSION_INFO = 0x1E08;
    public static final int RACE_ONLINE_LOG_OVER_BT_START = 0x1E09;

}
