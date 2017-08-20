package com.mobile.paolo.listaspesa.database.remote;

/**
 * Created by paolo on 20/08/17.
 */

public class RemoteDatabaseHelper
{
    private static RemoteDatabaseHelper instance;

    // Dev machine address
    private static final String DEV_ADDRESS = "192.168.0.104";

    // Local loopback
    private static final String LOOPBACK = "10.0.2.2";

    // Mode
    private static final int EMULATOR_MODE = 1;
    private static final int REAL_DEVICE_MODE = 2;

    private int mode = EMULATOR_MODE;

    public static synchronized RemoteDatabaseHelper getInstance()
    {
        if(instance == null)
        {
            instance = new RemoteDatabaseHelper();
        }
        return instance;
    }

    public String getHost()
    {
        if(this.mode == EMULATOR_MODE)
        {
            return LOOPBACK;
        }
        else return DEV_ADDRESS;
    }

}
