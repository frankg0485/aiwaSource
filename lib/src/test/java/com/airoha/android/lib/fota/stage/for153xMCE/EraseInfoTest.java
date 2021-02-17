package com.airoha.android.lib.fota.stage.for153xMCE;

import org.junit.Test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EraseInfoTest {

    @Test
    public void toRaw() {
        EraseInfo eraseInfo = new EraseInfo();
        eraseInfo.Recipient = 0x00;
        eraseInfo.StorageType = 0x01;
        eraseInfo.Address = new byte[]{0x02, 0x03, 0x04, 0x05};
        eraseInfo.Length = new byte[]{0x06, 0x07, 0x08, 0x09};

        byte[] expected = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 , 0x09};


        assertArrayEquals(expected, eraseInfo.toRaw());

    }
}