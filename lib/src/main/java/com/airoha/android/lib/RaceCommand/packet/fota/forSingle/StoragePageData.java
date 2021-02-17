package com.airoha.android.lib.RaceCommand.packet.fota.forSingle;

/**
 * will be 261 size
 */
public class StoragePageData {
    public byte CRC;
    public byte[] StorageAddress; // 4bytes
    public byte[] Data; // 256 bytes

    public StoragePageData(byte crc, byte[] storageAddress, byte[] data){
        this.CRC = crc;
        this.StorageAddress = storageAddress;
        this.Data = data;
    }

    public byte[] toRaw() {
        byte[] raw = new byte[261];

        raw[0] = this.CRC;
        System.arraycopy(this.StorageAddress, 0, raw, 1, StorageAddress.length);
        System.arraycopy(this.Data, 0, raw, 5,Data.length);

        return raw;
    }

    public byte[] getAddress(){
        return StorageAddress;
    }
}