package com.airoha.android.lib.fota.fotaState;

public class StageEnum {
    // Controlled by FW (0x01)
    public static final int FW_MOVE_WORKING_AREA_START = 0x0100;
    public static final int FW_MOVE_WORKING_AREA_COMPLETE = 0x0101;
    public static final int FW_MOVE_WORKING_AREA_FAIL = 0x0102;

    // Controlled by App (0x02)
    // Erase
//    public static final int APP_ERASE_START = 0x0200;
//    public static final int APP_ERASE_COMPLETE = 0x0201; // deprecated
    // Write
//    public static final int APP_WRITE_START = 0x0210; // deprecated
    public static final int APP_INTEGRITY_CHECK_SUCCESS = 0x0211;
    // Update File System
//    public static final int APP_FILE_SYSTEM_ERASE_START = 0x0220; // deprecated
//    public static final int APP_FILE_SYSTEM_NEED_RESTORE = 0x0221;
//    public static final int APP_NEW_FILE_SYSTEM_UPDATE_COMPLETE = 0x0222;
//    public static final int APP_OLD_FILE_SYSTEM_UPDATE_COMPLETE = 0x0223;

    // Update NVKey
    public static final int APP_NV_KEY_UPDATE_START = 0x0230;
    // End
    public static final int APP_RESULT_FOTA_NV_SUCCESS = 0x0240;
//    public static final int APP_RESULT_FOTA_FILE_SYSTEM_NVKEY_SUCCESS = 0x0241;
//    public static final int APP_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE = 0x0242;

    public static final int APP_UNKNOWN = 0xFFFF;

    // Controlled by App with TWS (0x03)
    // Erase
    public static final int APP_TWS_ERASE_START = 0x0300;
//    public static final int APP_TWS_ERASE_COMPLETE = 0x0301; // deprecated
    //Write
//    public static final int APP_TWS_WRITE_START = 0x0310; // deprecated
    public static final int APP_TWS_INTEGRITY_CHECK_SUCCESS = 0x0311;
    // Update File System
//    public static final int APP_TWS_FILE_SYSTEM_ERASE_START = 0x0320; // deprecated
//    public static final int APP_TWS_FILE_SYSTEM_NEED_RESTORE = 0x0321; // deprecated
    public static final int APP_TWS_NEW_FILE_SYSTEM_UPDATE_COMPLETE = 0x0322;

//    public static final int APP_TWS_START_UPDTAE_NV_KEY = 0x0330;
    // End
//    public static final int APP_TWS_RESULT_FOTA_NV_SUCCESS = 0x0340;
//    public static final int APP_TWS_RESULT_FOTA_FILE_SYSTEM_NVKEY_SUCCESS = 0x0341;
//    public static final int APP_TWS_RESULT_FOTA_FAIL_FILE_SYSTEM_RESTORE = 0x0342;
}
