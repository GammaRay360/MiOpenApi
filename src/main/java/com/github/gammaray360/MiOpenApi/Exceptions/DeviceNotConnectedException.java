package com.github.gammaray360.MiOpenApi.Exceptions;

/**
 * Created by junk_ on 08/11/2017.
 */
public class DeviceNotConnectedException extends RuntimeException {
    public DeviceNotConnectedException(){}
    public DeviceNotConnectedException(String message){
        super(message);
    }
    public DeviceNotConnectedException(String message, Throwable cause){
        super(message,cause);
    }
    public DeviceNotConnectedException(Throwable cause){
        super(cause);
    }
}
