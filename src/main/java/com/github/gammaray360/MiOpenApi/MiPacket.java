package com.github.gammaray360.MiOpenApi;

import com.sun.istack.internal.Nullable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Created by junk_ on 06/11/2017.
 */
class MiPacket{
    private final static Logger LOGGER = Logger.getLogger(MiDevice.class.getName());

    // region Frame format:
    public static final int FIELD_MAGIC_NUMBER_POS = 0;
    public static final int FIELD_LENGTH_POS = 2;
    public static final int FIELD_UNKNOWN_POS = 4;
    public static final int FIELD_DEVICE_ID_POS = 8;
    public static final int FIELD_TIMESTAMP_POS = 12;
    public static final int FIELD_MD5_POS = 16;
    public static final int FIELD_MAGIC_NUMBER_SIZE =2;
    public static final int FIELD_LENGTH_SIZE = 2;
    public static final int FIELD_UNKNOWN_SIZE = 4;
    public static final int FIELD_DEVICE_ID_SIZE = 4;
    public static final int FIELD_TIMESTAMP_SIZE = 4;
    public static final int FIELD_MD5_SIZE = 16;
    // endregion

    // region Class members and methods: {...}
    public static final byte[] HELLO_PACKET_BYTES = {
            (byte)0x21,(byte)0x31,(byte)0x00,(byte)0x20,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
            (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff
    };
    public static MiPacket getHelloPacket(){
        MiPacket hello = new MiPacket();
        hello.setData(HELLO_PACKET_BYTES,HELLO_PACKET_BYTES.length);
        return hello;
    }
    public static final int HEADER_SIZE = 32; // bytes
    public static final int MAX_PAYLOAD_SIZE = 65503; // bytes
    private static final int TOKEN_SIZE = 16; // bytes
    private static final byte[] MAGIC_NUMBER = {0x21,0x31};
    private static  final byte[] UNKNOWN = {0,0,0,0};
    private static final String ENCODING = "UTF8";
    // endregion

    // region Packet Header Fields and Payload: {...}
    private final byte[] mMagicNumber = new byte[FIELD_MAGIC_NUMBER_SIZE];
    private final byte[] mLength = new byte[FIELD_LENGTH_SIZE];
    private final byte[] mUnknown = new byte[FIELD_UNKNOWN_SIZE];
    private final byte[] mDeviceId = new byte[FIELD_DEVICE_ID_SIZE];
    private final byte[] mTimeStamp = new byte[FIELD_TIMESTAMP_SIZE];
    private final byte[] mMd5 = new byte[FIELD_MD5_SIZE];
    private byte[] mPayload;
    // endregion

    // region Private Members: {...}
    private String mEncoding = ENCODING;
    private void updateLength(){
        int length = HEADER_SIZE;
        if (mPayload != null){
            length += mPayload.length;
        }
        setLength(MiProtocolHelper.toByteArray(length,mLength.length));
    }
    // endregion

    // region Setters and getters: {...}
    public byte[] getMagicNumber() {
        return mMagicNumber.clone();
    }
    public MiPacket setMagicNumber(byte[] magicNumber) {
        if(magicNumber == null){
            System.arraycopy(MAGIC_NUMBER,0,mMagicNumber,0,mMagicNumber.length);
            return this;
        }
        System.arraycopy(magicNumber,0,mMagicNumber,0,mMagicNumber.length);
        return this;
    }
    public byte[] getLength() {
        return mLength.clone();
    }
    private MiPacket setLength(byte[] length) {
        // This shouldn't be used. the length determined aromaticity.
        if(length == null){
            Arrays.fill( mLength, (byte) 0 );
            return this;
        }
        System.arraycopy(length,0,mLength,0,mLength.length);
        return this;
    }
    public byte[] getUnknown() {
        return mUnknown.clone();
    }
    public MiPacket setUnknown(byte[] unknown) {
        if(unknown == null){
            System.arraycopy(UNKNOWN,0,mUnknown,0,mUnknown.length);
            return this;
        }
        System.arraycopy(unknown,0,mUnknown,0,mUnknown.length);
        return this;
    }
    public byte[] getDeviceId() {
        return mDeviceId.clone();
    }
    public MiPacket setDeviceId(byte[] deviceId) {
        if(deviceId == null){
            Arrays.fill( mDeviceId, (byte) 0 );
            return this;
        }
        System.arraycopy(deviceId,0,mDeviceId,0,mDeviceId.length);
        return this;
    }
    public byte[] getTimeStamp() {
        return mTimeStamp.clone();
    }
    public MiPacket setTimeStamp(byte[] timeStamp) {
        if(timeStamp == null){
            Arrays.fill( mTimeStamp, (byte) 0 );
            return this;
        }
        System.arraycopy(timeStamp,0,mTimeStamp,0,mTimeStamp.length);
        return this;
    }
    public byte[] getMd5() {
        return mMd5.clone();
    }
    public MiPacket setMd5(byte[] md5) {
        if(md5 == null){
            Arrays.fill( mMd5, (byte) 0 );
            return this;
        }
        System.arraycopy(md5,0,mMd5,0,mMd5.length);
        return this;
    }
    public synchronized String getPayload() {
        if(mPayload == null){
            return null;
        }
        try {
            return new String(mPayload,mEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized MiPacket setPayload(String payload) {
        if(payload == null){
            mPayload = null;
            updateLength();
            return this;
        }
        try {
            mPayload = payload.getBytes(mEncoding);
            updateLength();
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized boolean hasPayload() {
        if(mPayload == null){
            return false;
        }
        return true;
    }
    public String getEncoding() {
        return mEncoding;
    }
    public void setEncoding(String encoding) {
        if(encoding == null){
            mEncoding = ENCODING;
            return;
        }
        Charset.forName(encoding); // check if encoding supported.
        mEncoding = encoding;
    }
    // endregion

    // region constructors {...}
    MiPacket(){
        setMagicNumber(MAGIC_NUMBER);
        setUnknown(UNKNOWN);
    }
    MiPacket(@Nullable byte[] magicNumber, @Nullable byte[] length, @Nullable byte[] unknown,
                   @Nullable byte[] deviceId, @Nullable byte[] timeStamp, @Nullable byte[] md5){
        setMagicNumber(magicNumber);
        setLength(length);
        setUnknown(unknown);
        setDeviceId(deviceId);
        setTimeStamp(timeStamp);
        setMd5(md5);
    }
    // endregion

    // region Public methods: {...}
    public byte[] getData() {
        return MiProtocolHelper.concat(mMagicNumber, mLength, mUnknown, mDeviceId,
                mTimeStamp, mMd5, mPayload);
    }
    public void setData(byte[] buffer,int length) {
        if (buffer.length < length) {
            throw new IllegalArgumentException("buffers length is not as described in 'length'");
        }
        if (length < HEADER_SIZE) {
            throw new IllegalArgumentException("buffer is too small");
        }
        byte[] magicNumber = Arrays.copyOfRange(buffer, 0, 2);
        if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
            LOGGER.log(Level.WARNING, "buffer doesn't have a valid magic number");
        }
        System.arraycopy(buffer, FIELD_LENGTH_POS, mLength, 0, FIELD_LENGTH_SIZE);
        System.arraycopy(buffer, FIELD_UNKNOWN_POS, mUnknown, 0, FIELD_UNKNOWN_SIZE);
        System.arraycopy(buffer, FIELD_DEVICE_ID_POS, mDeviceId, 0, FIELD_DEVICE_ID_SIZE);
        System.arraycopy(buffer, FIELD_TIMESTAMP_POS, mTimeStamp, 0, FIELD_TIMESTAMP_SIZE);
        System.arraycopy(buffer, FIELD_MD5_POS, mMd5, 0, FIELD_MD5_SIZE);
        if (length == HEADER_SIZE) {
            mPayload = null;
        } else {
            mPayload = Arrays.copyOfRange(buffer, HEADER_SIZE, length);
        }
    }
    public MiPacket encryptAndCalculateChecksum(byte[] token){
        if(token.length < TOKEN_SIZE){
            throw new IllegalArgumentException("token is too small");
        }
        mPayload = MiProtocolHelper.encrypt(mPayload, token);
        updateLength();
        setMd5(token);
        setMd5(MiProtocolHelper.md5(getData()));
        return this;
    }
    public MiPacket decrypt(byte[] token){
        if(token.length < TOKEN_SIZE){
            throw new IllegalArgumentException("token is too small");
        }
        mPayload = MiProtocolHelper.decrypt(mPayload, token);
        return this;
    }
    // endregion

    @Override
    public synchronized String toString() {
        StringBuilder string = new StringBuilder("{");
        string.append(format("\tMagic number = %s,\n",MiProtocolHelper.toString(MAGIC_NUMBER)));
        string.append(format("\tPacket length = %s,\n",MiProtocolHelper.toString(mLength)));
        string.append(format("\tUnknown = %s,\n",MiProtocolHelper.toString(mUnknown)));
        string.append(format("\tDevice Id = %s,\n",MiProtocolHelper.toString(mDeviceId)));
        string.append(format("\tTimestamp = %s,\n",MiProtocolHelper.toString(mTimeStamp)));
        string.append(format("\tmd5 = %s,\n",MiProtocolHelper.toString(mMd5)));
        if(mPayload != null){
            string.append(format("\tpayload = %s",getPayload()));
        }
        string.append("\n}\n");
        return string.toString();
    }
}