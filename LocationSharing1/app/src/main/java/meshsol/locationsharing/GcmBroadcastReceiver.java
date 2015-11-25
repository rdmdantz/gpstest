package meshsol.locationsharing;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.app.Activity;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.*;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("broadcastreceiver", "");
        ComponentName  comp = new ComponentName (context.getPackageName(),
                GCMIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        // Intent pushIntent = new Intent(context, GCMIntentService.class);
        // context.startService(pushIntent);
    }
}