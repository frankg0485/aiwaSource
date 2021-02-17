package com.airoha.android.lib.mmi.stage;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.StatusCode;
import com.airoha.android.lib.mmi.AirohaMmiMgr;

import java.io.ByteArrayOutputStream;

public class MmiStageSetGameMode extends MmiStage {
    boolean mIsEnabled;

    public MmiStageSetGameMode(AirohaMmiMgr mgr, boolean isEnabled) {
        super(mgr);
        mIsEnabled = isEnabled;
        mRaceId = RaceId.RACE_MMI_KEY_COMMAND;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        //<HexCommand name="DSP_REALTIME_ANC_OFF">
        //      <Transmit format="05 5A %1 06 0E 00 0B" >
        //        <P1>_two_bytes_right_count_of_bytes</P1>
        //      </Transmit>
        //      <Response format="05 5B %% %% 06 0E %1 0B %2 %3 %4 %5">
        //        <P1>status</P1>
        //        <P2>reserved[0:8]</P2>
        //        <P3>reserved[8:8]</P3>
        //        <P4>reserved[16:8]</P4>
        //        <P5>reserved[24:8]</P5>
        //      </Response>
        //    </HexCommand>

        //Control	Race CMD	Red Font	Response
        //On	05 5A 05 00 06 0E 00 0A 01 	(filter select) 	05 5B 05 00 06 0E 00 0A 01 00 00 00
        //Off	05 5A 04 00 06 0E 00 0B		05 5B 04 00 06 0E 00 0B 00 00 00 00

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (mIsEnabled) {
            bos.write((byte) 0xA4);
            bos.write((byte) 0x00);
        } else {
            bos.write((byte) 0xA5);
            bos.write((byte) 0x00);
        }

        byte[] payload = bos.toByteArray();

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, mRaceId, payload);

        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        byte isSuccess = packet[RacePacket.IDX_PAYLOAD_START];

        mAirohaLink.logToFile(TAG, "MmiStageSetGameMode resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        if(isSuccess == 00) {
            mMmiMgr.notifyGameModeStatueChanged(mIsEnabled);
            cmd.setIsRespStatusSuccess();
        } else {
            mMmiMgr.notifyGameModeStatueChanged(!mIsEnabled);
        }

    }
}
