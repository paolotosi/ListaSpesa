package com.mobile.paolo.listaspesa.database.remote;

/**
 * -- RemoteDatabaseHelper --
 * The attribute 'mode' of this class let the user choose between running the app
 * on the emulator or on a real android device.
 * In order to do so, the development machine IP address needs to be set in the
 * 'DEV_ADDRESS' variable and the phone must be on the same network.
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

    String getHost()
    {
        if(this.mode == EMULATOR_MODE)
        {
            return LOOPBACK;
        }
        else return DEV_ADDRESS;
    }

}
