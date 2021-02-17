package com.airoha.android.lib.Common;

import com.airoha.android.lib.RaceCommand.constant.RaceId;
import com.airoha.android.lib.RaceCommand.constant.RaceType;
import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.fota.AirohaRaceOtaMgr;
import com.airoha.android.lib.fota.stage.FotaStage;
import com.airoha.android.lib.util.Converter;

public class ReadNvKey extends FotaStage {
    private short mNvkeyid;
    public ReadNvKey(AirohaRaceOtaMgr mgr, short nvkey_id) {
        super(mgr);
        mNvkeyid = nvkey_id;
        mRaceId = RaceId.RACE_NVKEY_READFULLKEY;
        mRaceRespType = RaceType.RESPONSE;
    }

    @Override
    public void genRacePackets() {
        byte[] queryId = Converter.ShortToBytes(mNvkeyid);
        byte[] queryLength = Converter.shortToBytes((short) 100);
        byte[] payload = new byte[]{queryId[0], queryId[1], queryLength[0], queryLength[1]};

        RacePacket cmd = new RacePacket(RaceType.CMD_NEED_RESP, RaceId.RACE_NVKEY_READFULLKEY, payload);
        placeCmd(cmd);
    }

    @Override
    protected void placeCmd(RacePacket cmd) {
        mCmdPacketQueue.offer(cmd);

        mCmdPacketMap.put(TAG, cmd); // only one cmd needs to check resp
    }

    @Override
    public void parsePayloadAndCheckCompeted(int raceId, byte[] packet, byte status, int raceType) {
        int length = 0;
        if (packet.length >= 8) {
            length = Converter.BytesToShort(packet[7], packet[6]);
        }
        mAirohaLink.logToFile(TAG, "ReadNvKey resp length: " + length);
        RacePacket cmd = mCmdPacketMap.get(TAG);
        cmd.setIsRespStatusSuccess();
        mIsRespSuccess = true;
        mStatusCode = 0;

        byte[] keyIdArr = Converter.shortToBytes(mNvkeyid);
        String keyId = "0x" + Converter.byteArrayToHexString(new byte[]{keyIdArr[1], keyIdArr[0]}).toUpperCase();
        mOtaMgr.addReadNvKeyEvent(keyId, packet, !mIsRelay);
    }
}
