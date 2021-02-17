package com.airoha.android.lib.fota.stage.forSingle;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AgentClientEnum;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;

public class FotaStage_00_GetAudioChannel extends FotaStage {

    public FotaStage_00_GetAudioChannel(AirohaRaceOtaMgr mgr) {
        super(mgr);

        mAgentOrClient = AgentClientEnum.AGENT;

        mRaceId = RaceId.RACE_NVKEY_READFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    private byte mAgentOrClient = AgentClientEnum.AGENT;

    @Override
    public void genRacePackets() {
        byte[] queryId = new byte[2];
        queryId[0] = (byte)0xB5; //0xF2B5
        queryId[1] = (byte)0xF2;

        placeCmd(genReadNvKeyPacket(queryId));
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);
        mCmdPacketMap.put(TAG, cmd);
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        mAirohaLink.logToFile(TAG, "RACE_FOTA_GET_AUDIO_CHANNEL resp status: " + status);

        RacePacket cmd = mCmdPacketMap.get(TAG);

        //Tx packet: 05 5A 06 00 00 0A B5 F2 E8 03
        //Rx packet :  05 5B 09 00 00 0A 05 00 00 01 01 02 14
        //Rx Relay packet :  05 5D 11 00 01 0D 05 06 05 5B 09 00 00 0A 05 00 00 02 01 02 14

        if (packet.length >= 13 && packet[8] == 0){
            cmd.setIsRespStatusSuccess();
            mStatusCode = 0;
        } else {
            passToMgr((byte)0xFF);
            cmd.setIsRespStatusSuccess();
            mStatusCode = 0;
            return;
        }

        byte channel = packet[9];
        passToMgr(channel);
        mOtaMgr.addReadNvKeyEvent("0xF2B5", packet, !mIsRelay);
    }

    protected void passToMgr(byte channel){
        mOtaMgr.setAgentAudioChannel(channel);
    }
}
