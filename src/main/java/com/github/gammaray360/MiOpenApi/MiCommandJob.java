package com.github.gammaray360.MiOpenApi;

import com.github.gammaray360.MiOpenApi.MiCommands.MiCommand;

/**
 * Created by junk_ on 04/11/2017.
 */
public class MiCommandJob {
    // region Status: {...}
    private static final int EMPTY = 0;
    private static final int READY = 1;
    private static final int SENDING = 2;
    private static final int SENT = 3;
    private static final int GOT_RESPONSE = 4;
    private static final int ERROR = 5;
    private int mStatus = EMPTY;
    // endregion

    // region Members: {...}
    private MiDevice mDevice;
    private MiCommand mCommand;
    private Exception mException;
    private String mResponse;
    // endregion

    public MiCommandJob(MiDevice device, MiCommand command){
        mDevice = device;
        mCommand = command;
        mStatus = READY;
    }

    public void execute(){
        if(mStatus != READY){
            //ERROR
            return;
        }
        mStatus = SENDING;
        try {
            mResponse = mDevice.send(mCommand.getCommandString());
            mStatus = GOT_RESPONSE;
        } catch (Exception e){
            mStatus = ERROR;
            mException = e;
            e.printStackTrace();
        }
    }

    public String getResponse(){
        return mResponse;
    }
}
