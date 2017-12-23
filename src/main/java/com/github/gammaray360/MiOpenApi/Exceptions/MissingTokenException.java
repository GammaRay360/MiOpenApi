package com.github.gammaray360.MiOpenApi.Exceptions;

/**
 * Created by junk_ on 09/11/2017.
 */
public class MissingTokenException extends RuntimeException{
    public MissingTokenException(){}
    public MissingTokenException(String message){
        super(message);
    }
    public MissingTokenException(String message, Throwable cause){
        super(message,cause);
    }
    public MissingTokenException(Throwable cause){
        super(cause);
    }
}
