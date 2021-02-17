package com.airoha.android.lib.onlinedump;

import android.os.Environment;
import android.util.Log;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.mmi.stage.MmiStage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class StageGetBuildInfo extends MmiStage {

    public StageGetBuildInfo(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_GET_BUILD_VERSION_INFO;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, mRaceId);

        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_SUSPEND_DSP resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);
        if (status == StatusCode.FOTA_ERRCODE_SUCESS){
            /*
            * typedef struct {
            * char sw_verno[48];    // SW version:
            * char build_time[32]  // build time:
            * char hw_verno[16]  // hw version:
            * } build_version_info;
            * */

            byte[] tmp = new byte[48];
            System.arraycopy(packet, 7, tmp, 0, 48);
            String SW_version = new String(tmp);
            Arrays.fill( tmp, (byte) 0 );
            System.arraycopy(packet, 55, tmp, 0, 32);
            String build_time = new String(tmp);
            Arrays.fill( tmp, (byte) 0 );
            System.arraycopy(packet, 87, tmp, 0, 16);
            String HW_version = new String(tmp);


            File mFile = null;
            FileOutputStream mFos = null;

            mFile = new File(Environment.getExternalStorageDirectory(), "version.txt");
            try {
                if(mFile.exists())
                    mFile.delete();

                mFile.createNewFile();

                mFos = new FileOutputStream(mFile, true);
                String log = "SW version: " + SW_version.trim() + "\n";
                mFos.write(log.getBytes());
                log = "Build time: " + build_time.trim() + "\n";
                mFos.write(log.getBytes());
                log = "HW version: " + HW_version.trim() + "\n";
                mFos.write(log.getBytes());


                if(mFile != null) {
                    mFile = null;
                }
                if (mFos != null) {
                    mFos.flush();
                    mFos.close();
                }
            } catch (IOException e) {
                Log.d("StageGetBuildInfo", "create FileOutputStream fail");
                e.printStackTrace();
            }

            cmd.setIsRespStatusSuccess();
        } else {
            //cmd.increaseRetryCounter();
            return;
        }
    }
}
