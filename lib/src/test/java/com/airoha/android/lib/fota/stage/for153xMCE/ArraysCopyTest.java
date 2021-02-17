package com.airoha.android.lib.fota.stage.for153xMCE;

import com.google.common.primitives.Bytes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ArraysCopyTest {

//    @Test
//    public void testArrayCopy() {
//        byte[] packet = {0x01, 0x02, 0x03, 0x04, 0x05};
//
//        byte[] copied = Arrays.copyOfRange(packet, 0, 3);
//
//        byte[] expected = {0x01, 0x02, 0x03};
//
//        assertArrayEquals(expected, copied);
//    }

    @Test
    public void testGuava() {
        List<Byte> list = new ArrayList<>();
        list.add((byte)0x00);
        list.add(((byte)0x01));
        list.add((byte)0x09);

        byte[] arr = Bytes.toArray(list);

        byte[] expected = {(byte)0x00, (byte)0x01, (byte)0x09};

        assertArrayEquals(expected, arr);
    }
}
