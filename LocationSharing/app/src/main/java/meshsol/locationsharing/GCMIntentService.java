package meshsol.locationsharing;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class GCMIntentService extends IntentService {

    private static final String TAG = "msg";

    String user_id;
    public GCMIntentService() {
        super(AppManager.project_id);
    }


    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
                Context context=getApplicationContext();
                String message = intent.getExtras().getString("message");
                Log.e(TAG, " Received Notification: " + message);
//                ShowToast(message);
                String[] parts = message.split(";");
                parts[1]=parts[1].replaceAll(" ","");
                parts[2]=parts[2].replaceAll(" ","");
                String sender = "";
                sender = parts[1].replaceAll("\\D+", "");
                long l = Long.parseLong(sender);
        if (parts[0] != null && parts[0] != "" ) {

            if(parts[0].equalsIgnoreCase("guestReached")){

                // GUEST HAS REACHED DESTINATION
                SharePreferences.setPrefSession(context,"destroyed");
                AlarmManager aManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                int cancel_alarm_id_normal=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.e("msg", "STOP SENDING NORMAL UPDATES for id: " + cancel_alarm_id_normal);
                Intent Receiverintent = new Intent(getApplicationContext(), RepeatNotification.class);
                PendingIntent alarmIntent;
                alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_normal, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent.cancel();
                aManager.cancel(alarmIntent);

                //STOP SENDING HIGH FREQUENCY UPDATES
                AlarmManager aManager1= (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                int cancel_alarm_id_hf=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.d("msg", "STOP SENDING HIGH FREQUENCY UPDATES for id: " + cancel_alarm_id_hf);
                Intent Receiverintent1 = new Intent(getApplicationContext(), HighFreqAlarm.class);
                PendingIntent alarmIntent1;
                alarmIntent1 = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_hf, Receiverintent1, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent1.cancel();
                aManager1.cancel(alarmIntent1);

                Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
                intent1.setFlags(intent1.FLAG_ACTIVITY_NEW_TASK
                        | intent1.FLAG_ACTIVITY_CLEAR_TOP
                        | intent1.FLAG_ACTIVITY_SINGLE_TOP);
                intent1.putExtra("msg","Guest decided to end Location Sharing Session");
                startActivity(intent1);

            } else if(parts[0].equalsIgnoreCase("stopUpdatingLocation")){
                //ENDING SESSION

                SharePreferences.setPrefSession(context,"destroyed");

                //STOP SENDING NORMAL UPDATES
                AlarmManager aManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                int cancel_alarm_id_normal=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.e("msg", "STOP SENDING NORMAL UPDATES for id: " + cancel_alarm_id_normal);
                Intent Receiverintent = new Intent(getApplicationContext(), RepeatNotification.class);
                PendingIntent alarmIntent;
                alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_normal, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent.cancel();
                aManager.cancel(alarmIntent);

                //STOP SENDING HIGH FREQUENCY UPDATES
                AlarmManager aManager1= (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                int cancel_alarm_id_hf=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.d("msg", "STOP SENDING HIGH FREQUENCY UPDATES for id: " + cancel_alarm_id_hf);
                Intent Receiverintent1 = new Intent(getApplicationContext(), HighFreqAlarm.class);
                PendingIntent alarmIntent1;
                alarmIntent1 = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_hf, Receiverintent1, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent1.cancel();
                aManager1.cancel(alarmIntent1);

                Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("msg","Host decided to end Location Sharing Session");
                startActivity(intent1);

            }else if (parts[0].equalsIgnoreCase("updateLocation")) {
                intent = new Intent(context, LocationTracking.class);
                intent.putExtra("number", sender);
                Log.e("msg","updateLocationOnRequest");
                intent.putExtra("frequency", "requestedUpdate");
                if(!isMyServiceRunning(LocationTracking.class,context)) {
                    context.startService(intent);
                }

            } else if (parts[0].equalsIgnoreCase("changeUpdateFreq")) {
                AlarmManager aManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                int cancel_alarm_id=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.d("msg", "Cancelling alarm for id: " + cancel_alarm_id);
                Intent Receiverintent = new Intent(context, RepeatNotification.class);
                PendingIntent alarmIntent;
                alarmIntent = PendingIntent.getBroadcast(context,cancel_alarm_id, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent.cancel();
                aManager.cancel(alarmIntent);

                intent = new Intent(context, LocationTracking.class);
                intent.putExtra("specialMesssage", "startHighFreqAlarm");
                intent.putExtra("address", sender);
                intent.putExtra("alarmId", "" + l);
                context.startService(intent);

            } else if (parts[0].equalsIgnoreCase("StopSendingUpdates")) {
                AlarmManager aManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                int cancel_alarm_id=Integer.valueOf(SharePreferences.getPrefHighfrequencyAlarmId(getApplicationContext()));
                Log.d("msg", "Stoping to send more sms for id: " + cancel_alarm_id);
                Intent Receiverintent = new Intent(context, HighFreqAlarm.class);
                PendingIntent alarmIntent;
                alarmIntent = PendingIntent.getBroadcast(context,cancel_alarm_id, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent.cancel();
                aManager.cancel(alarmIntent);


                //SHOWING MESSAGE REACHED DESTINATION IN NOTIFICATION BOX
                Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("msg", "You have reached Destination");
                startActivity(intent1);

            } else  if(parts[0].equalsIgnoreCase("request")){
                SharePreferences.setPrefServerUpdated(getApplicationContext(), "false");
                Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.LocationDisplay");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("lat", parts[3]);
                intent1.putExtra("lon", parts[4]);
                intent1.putExtra("extension", parts[0]);
                intent1.putExtra("speed", parts[5]);
                intent1.putExtra("sender", parts[1]);
                context.startActivity(intent1);
            }else  if(parts[0].equalsIgnoreCase("response")){

                Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.LocationDisplay");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("lat", parts[3]);
                intent1.putExtra("lon", parts[4]);
                intent1.putExtra("extension", parts[0]);
                intent1.putExtra("speed", parts[5]);
                intent1.putExtra("sender", parts[1]);
                context.startActivity(intent1);
            }else  if(parts[0].equalsIgnoreCase("UpdatingNormal") || parts[0].equalsIgnoreCase("UpdatingHighFreq") || parts[0].equalsIgnoreCase("requestedUpdate")){
                Log.d("msg", "starting activity from intent service");
                SharePreferences.setPrefServerUpdated(getApplicationContext(), "false");
                Intent intent1 = new Intent(GCMIntentService.this,LocationDisplay.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                intent1.putExtra("lat", parts[3]);
                intent1.putExtra("lon", parts[4]);
                intent1.putExtra("extension", parts[0]);
                intent1.putExtra("speed", parts[5]);
                intent1.putExtra("sender", parts[1]);
                startActivity(intent1);
            }

        }

    }catch(Exception e){
            e.printStackTrace();
        }
            }



    /**
     * Issues a notification to inform the user that server has sent a message.
     */

    private  void generateNotification(String message) {

       int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        Notification notification;
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(GCMIntentService.this, MapsActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        String title = this.getString(R.string.app_name);
        long nid=System.currentTimeMillis();
        int notification_id=(int) (nid);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        notification = builder.setContentIntent(intent)
                .setSmallIcon(icon).setTicker(title).setWhen(when)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(message).build();

        notificationManager.notify(notification_id, notification);

    }

    /**
     * Checking service is running or not
     */
    private boolean isMyServiceRunning(Class serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Function using Handler to show Toast message
     */
    public void ShowToast(final String msg)
    {  final Context MyContext = getApplicationContext();
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {  @Override public void run()
            {
                Context context = getApplicationContext();
                // Create layout inflator object to inflate toast.xml file
                LayoutInflater inflater = (LayoutInflater) MyContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

                // Call toast.xml file for toast layout
                View toastRoot = inflater.inflate(R.layout.toast, null);
                TextView tvMessage=(TextView)toastRoot.findViewById(R.id.tvToastMessages);
                tvMessage.setText(msg);
                Toast toast = new Toast(MyContext);

                // Set layout to toast
                toast.setView(toastRoot);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                        0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
            }
        });
    };
}
