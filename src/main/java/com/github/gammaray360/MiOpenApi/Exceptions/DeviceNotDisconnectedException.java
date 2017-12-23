package com.github.gammaray360.MiOpenApi.Exceptions;

/**
 * Created by junk_ on 08/11/2017.
 */
public class DeviceNotDisconnectedException extends RuntimeException {
    public DeviceNotDisconnectedException(){}
    public DeviceNotDisconnectedException(String message){
        super(message);
    }
    public DeviceNotDisconnectedException(String message, Throwable cause){
        super(message,cause);
    }
    public DeviceNotDisconnectedException(Throwable cause){
        super(cause);
    }
}
