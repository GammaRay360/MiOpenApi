package com.github.gammaray360.MiOpenApi;

class MiDeviceInfo {
    private final byte[] mId = new byte[4];
    private final byte[] mTimeStamp = new byte[4];
    private final byte[] mToken = new byte[16];

    MiDeviceInfo(){}
    MiDeviceInfo(byte[] id, byte[] timeStamp, byte[] token){
        setId(id);
        setTimeStamp(timeStamp);
        setToken(token);
    }


    public void setData(byte[] buffer){
        if(buffer.length < MiPacket.HEADER_SIZE)
             new RuntimeException("buffer too small");

        System.arraycopy(buffer, MiPacket.FIELD_DEVICE_ID_POS, mId, 0, MiPacket.FIELD_DEVICE_ID_SIZE);
        System.arraycopy(buffer, MiPacket.FIELD_TIMESTAMP_POS, mTimeStamp, 0, MiPacket.FIELD_TIMESTAMP_SIZE);
        if(MiProtocolHelper.isZero(buffer)) return;
        System.arraycopy(buffer, MiPacket.FIELD_MD5_POS, mToken, 0, MiPacket.FIELD_MD5_SIZE);
    }
    public byte[] getId() {
        return mId.clone();
    }
    public MiDeviceInfo setId(byte[] id) {
        System.arraycopy(id,0,mId,0,mId.length);
        return this;
    }
    public byte[] getTimeStamp() {
        return mTimeStamp.clone();
    }
    public MiDeviceInfo setTimeStamp(byte[] timeStamp) {
        System.arraycopy(timeStamp,0,mTimeStamp,0,mTimeStamp.length);
        return this;
    }
    public byte[] getToken() {
        return mToken.clone();
    }
    public MiDeviceInfo setToken(byte[] token) {
        System.arraycopy(token,0,mToken,0,mToken.length);
        return this;
    }
}
