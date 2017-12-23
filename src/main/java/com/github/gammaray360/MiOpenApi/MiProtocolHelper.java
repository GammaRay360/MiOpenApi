package com.github.gammaray360.MiOpenApi;

import com.sun.istack.internal.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import static java.lang.String.format;

/**
 * Created by junk_ on 05/11/2017.
 */
public abstract class MiProtocolHelper {
    private static final int MD5_LENGTH = 16; // bytes
    private static final int MD5_START_BYTE_POS = 16; // first byte is position 0.

    private static MessageDigest mMessageDigest;
    private static Cipher mCipher;

    // private constructor:
    private MiProtocolHelper(){}

    public static String toString(byte[] bytes){
            StringBuilder string = new StringBuilder("");
            for (byte b : bytes) {
                string.append(format("%02X",b));
            }
            return string.toString();
        }

    public static long toLong(byte[] bytes) {
        long value = 0;
        for(byte b : bytes){
            value += (long) b & 0xFF;
            value <<=  8;
        }
        value >>=  8;
        return value;
    }

    public static byte[] toByteArray(long num, int numOfBytes){
        byte[] bytes = new byte[numOfBytes];
        int i=numOfBytes-1;
        while(num > 0){
            if(i < 0) {
                // TODO: error. number has more bytes than numPfBytes.
                break;
            }
            bytes[i--] = (byte) (num & 0xFF);
            num >>= 8;
        }
        return bytes;
    }

    public static boolean isZero(byte[] bytes){
        for(byte b : bytes){
            if(b != 0)
                return false;
        }
        return true;
    }

    public static byte[] concat(@Nullable byte[]...arrays) {
        // Determine the length of the result array
        int totalLength = 0;
        for (byte[] array :arrays)
        {
            if(array == null){
                continue;
            }
            totalLength += array.length;
        }

        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for (byte[] array : arrays)
        {
            if(array == null){
                continue;
            }
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    public static byte[] md5(byte[] buffer) {
        if(mMessageDigest == null){
            try {
                mMessageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return mMessageDigest.digest(buffer);
    }

    public static byte[] encrypt(byte[] buffer, byte[] token){
        try {
            if(mCipher == null){
                mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //should be PKCS7 instead of PKCS5, but still works.
            }
            byte[] key = md5(token);
            byte[] keyAndToken = concat(key, token);
            byte[] ivs = md5(keyAndToken);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivs);
            mCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
            return mCipher.doFinal(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] buffer,byte[] token){
        try {
            if(mCipher == null){
                mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //should be PKCS7 instead of PKCS5, but still works.
            }
            byte[] key = md5(token);
            byte[] keyAndToken = concat(key, token);
            byte[] ivs = md5(keyAndToken);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivs);
            mCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
            return mCipher.doFinal(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
