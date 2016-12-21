package org.ml.gchat.util;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CryptoUtils {
    public static byte[] computeHashToByteArray(String x) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(x.getBytes());
        return md.digest();
    }

    public static byte[] computeHashToByteArray(String x, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        SecureRandom secureRandom = new SecureRandom();
        md.update(x.getBytes());
        byte[] b = salt.getBytes();

        md.update(b);
        return md.digest();
    }

    public static String computeHashToHexString(String x, String salt) throws Exception {
        byte[] bytes = computeHashToByteArray(x, salt);
        return byteArrayToHexString(bytes);
    }

    public static String computeHashToHexString(String x) throws Exception {
        byte[] bytes = computeHashToByteArray(x);
        return byteArrayToHexString(bytes);
    }

    public static byte[] hexStringToByteArray(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            int v = b & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return DatatypeConverter.printHexBinary(array).toLowerCase();
    }
}
