package com.airoha.android.lib.fota.stage.for153xMCE;

import com.airoha.android.lib.RaceCommand.packet.RacePacket;
import com.airoha.android.lib.util.Converter;

import java.util.Arrays;

public class RelayRespExtracter {
    public static byte[] extractRelayRespPacket(byte[] originalPacket){
        return Arrays.copyOfRange(originalPacket, 8, originalPacket.length);
    }

    public static boolean isExtractedWanted(byte[] stripRelayHeaderPacket, byte raceType, int raceId){
        byte relayRaceType = stripRelayHeaderPacket[1];
        int relayRaceId = Converter.BytesToU16(stripRelayHeaderPacket[5], stripRelayHeaderPacket[4]);

        if(relayRaceType != raceType || relayRaceId != raceId)
            return false;
        return true;
    }

    public static byte extractStatus(byte[] stripRelayHeaderPacket){
        return stripRelayHeaderPacket[RacePacket.IDX_PAYLOAD_START];
    }

    public static byte extractRaceType(byte[] stripRelayHeaderPacket){
        return stripRelayHeaderPacket[1];
    }

    public static int extractRaceId(byte[] stripRelayHeaderPacket) {
        return Converter.BytesToU16(stripRelayHeaderPacket[5], stripRelayHeaderPacket[4]);
    }

}
