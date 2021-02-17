package com.airoha.android.lib.mmi.stage;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.mmi.AirohaMmiMgr;
import com.airoha.android.lib.util.Converter;

public class MmiStageGetGameMode extends MmiStage {
    public MmiStageGetGameMode(AirohaMmiMgr mgr) {
        super(mgr);
        mRaceId = RaceId.RACE_HOSTAUDIO_MMI_GET_ENUM;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        //Cmd format
        //05 + type + length(2 byte) + CMD id(2 byte) + module(2 byte)
        // 05  5A     0400             0109             0300
        //Rsp format
        //05 + type + length(2 byte) + CMD id(2 byte) + module(2 byte) + status(1 byte) + variable para

        // Module Enum
        // {
        //         PEQ Group Index = 0,
        //         VP OnOff = 1,
        //         VP Language = 2,
        //         VP Get = 3,
        //         VP Set = 4,
        //         ANC Status = 5,
        //         GAME MODE = 6
        // }

        byte[] moduleId = Converter.shortToBytes((short) 6);

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_HOSTAUDIO_MMI_GET_ENUM, moduleId);
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {

        //Rsp format
        //05 + type + length(2 byte) + CMD id(2 byte) + module(2 byte) + status(1 byte) + variable para
        mAirohaLink.logToFile(TAG, "MmiStageGetGameMode resp packet: " + Converter.byte2HexStr(packet));

        if(raceId != RaceId.RACE_HOSTAUDIO_MMI_GET_ENUM)
            return;

        if(packet[8] != StatusCode.FOTA_ERRCODE_SUCESS)
            return;

        byte index = packet[9];
        mMmiMgr.notifyGameModeState(index);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if (status == StatusCode.FOTA_ERRCODE_SUCESS) {
            cmd.setIsRespStatusSuccess();
        } else {
            return;
        }
    }
}
