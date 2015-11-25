package meshsol.locationsharing;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Wasiq Billah on 11/6/2015.
 */
public class HighFreqAlarm  extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String Receiver="";
        String frequency="";
        //get and send location information start service to send location....
        Bundle extras=intent.getExtras();
        if (extras != null) {
            Receiver= extras.getString("sender");
            frequency = extras.getString("frequency");
        }
        intent = new Intent(context, LocationTracking.class);
        intent.putExtra("number", Receiver);
        intent.putExtra("frequency", frequency);
        if(!isMyServiceRunning(LocationTracking.class,context)) {
            context.startService(intent);
        }else{
            Log.i("msg", "service already running");
        }
    }
    private boolean isMyServiceRunning(Class serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
