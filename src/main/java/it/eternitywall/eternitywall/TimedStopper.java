package it.eternitywall.eternitywall;

import android.util.Log;

import java.util.TimerTask;

/**
 * Created by Riccardo Casatta @RCasatta on 19/04/16.
 */
public class TimedStopper extends TimerTask {
    private static final String TAG = "TimedStopper";
    private EWApplication ewApplication;

    public TimedStopper(EWApplication ewApplication) {
        this.ewApplication = ewApplication;
    }

    @Override
    public void run() {
        Log.i(TAG,"TimedStopper called!");
        ewApplication.unbindService( );
    }
}
