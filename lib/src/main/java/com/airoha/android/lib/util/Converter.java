package com.airoha.android.lib.util;

import com.google.common.primitives.Bytes;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

public class Converter {

    private final static char[] mChars = "0123456789ABCDEF".toCharArray();

    public static byte intToByte(int value) {
        return (byte) value;
    }

    public static byte[] intToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    public static byte[] intToBytes(int i){
        return new byte[]{ (byte)(i & 0xFF),
                (byte)(i>> 8 & 0xFF),
                (byte)(i>> 16 & 0xFF),
                (byte)(i>> 24 & 0xFF)
        };
    }

    public static byte[] stringtoascii(String string) {
        byte[] name = {};
        try {
            name = string.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return name;
    }

    public static String hexStrToAsciiStr(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for( int i=0; i<hex.length()-1; i+=2 ){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());
        return sb.toString();
    }

    public static int BytesToU16(byte High, byte Low) {
        int high = (High&0xFF) << 8;
        int low = (Low&0xFF);
        return (high + low);
    }

    public static int BytesToInt(byte High, byte Low) {
        int high = (High&0xFF) << 8;
        int low = (Low&0xFF);
        return (high + low);
    }

    // to signed short
    public static short BytesToShort(byte High, byte Low){
        return (short)(((High & 0xFF) << 8) | (Low & 0xFF));
    }

    public static int bytesToInt32(byte[] bytesInLE){
        return (int)((bytesInLE[0] &0xFF) | ((bytesInLE[1] & 0xFF) << 8) | ((bytesInLE[2] & 0xFF) << 16) | ((bytesInLE[3] & 0xFF) << 24));
    }

    public static byte[] ShortToBytes(short Value)
    {
        byte[] b = new byte[2];
        b[1] = (byte)((Value >> 8) & 0xFF);
        b[0] = (byte)(Value & 0xFF);
        return b;
    }

    public static String byte2HexStr(byte[] b, int iLen) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < iLen; n++) {
            sb.append(mChars[(b[n] & 0xFF) >> 4]);
            sb.append(mChars[b[n] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static String byte2HexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(mChars[(b & 0xFF) >> 4]);
        sb.append(mChars[b & 0x0F]);
        return sb.toString().trim().toUpperCase(Locale.US);
    }


    public static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < b.length; n++) {
            sb.append(mChars[(b[n] & 0xFF) >> 4]);
            sb.append(mChars[b[n] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static String byte2HerStrReverse(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int n = b.length -1; n >=0 ; n--) {
            sb.append(mChars[(b[n] & 0xFF) >> 4]);
            sb.append(mChars[b[n] & 0x0F]);
            sb.append(' ');
        }

        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static String byte2HexStrWithoutSeperator(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < b.length; n++) {
            sb.append(mChars[(b[n] & 0xFF) >> 4]);
            sb.append(mChars[b[n] & 0x0F]);
        }
        return sb.toString().trim().toUpperCase(Locale.US);
    }


    public static String short2Str(Short[] shorts) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < shorts.length; n++) {
            sb.append(shorts[n]);
            sb.append(' ');
        }
        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes){
        String rtn = "";
        for(byte b : bytes){
            rtn += String.format("%02x", b);
        }
        return rtn;
    }

    public static byte[] listBytesToBytes(List<Byte> list){
        return Bytes.toArray(list);
    }

    public static byte[] shortToBytes(short Value)
    {
        byte[] b = new byte[2];
        b[1] = (byte)((Value >> 8) & 0xFF);
        b[0] = (byte)(Value & 0xFF);
        return b;
    }

    public static byte[] shortArrToBytes(short[] coefParam) {
        byte[] result = new byte[coefParam.length * 2];

        for (int i = 0; i < coefParam.length; i++) {
            byte[] covertedBytes = Converter.shortToBytes(coefParam[i]);

            System.arraycopy(covertedBytes, 0, result, i * 2, 2);
        }

        return result;
    }

    public static String shortArrToString(short[] param) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < param.length; n++) {
            sb.append(param[n]);
            sb.append(',');
        }
        return sb.toString().trim();

    }
}