package meshsol.locationsharing;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

////import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends IntentService {

    private static final String TAG = "GCMIntentService";

    String user_id;
    public GCMIntentService() {
        super(AppManager.project_id);
    }

    /**
     * Method called on device registered
     **/
	/*@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		// displayMessage(context, "Your device registred with GCM");
		// Log.d("NAME", MainActivity.name);
		// ServerUtilities.register(context, MainActivity.name,
		// MainActivity.email, registrationId);
	}*/

    /**
     * Method called on device un registred
     * */
/*
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		// displayMessage(context, getString(R.string.gcm_unregistered));
		// ServerUtilities.unregister(context, registrationId);
	}
*/

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onHandleIntent(Intent intent) {
//        try {
            Log.d("gcmintentservice", "");
  /*          user_id=SaveSharedPreference.getUserId(getApplicationContext());
            System.out.println(user_id);
            Alarm_Manager alarm = new Alarm_Manager();
            String message = intent.getExtras().getString("message");
            Log.i(TAG, "Received message"+message);
            String[] parts;
            parts = message.split(";");
            System.out.println("GCM Message:  " + message);
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(this);
            //generateNotification(context, parts[1]);
            System.out.println(parts[0]);
            if (parts[0].equalsIgnoreCase("assignment_added")) {
                generateNotification("New Assignment!");
                String date = parts[2];
                String time = parts[3];
                String alert_type = parts[4];
                String server_id = parts[5];
                String aid=parts[6];
                int alarm_id=Integer.parseInt(aid);
//			long alert_id = System.currentTimeMillis();
//			// Fixalarm falarmdba=new Fixalarm(getApplicationContext());
//			//falarmdba.open();
//			falarmdba.createEntry(date, time, alert_type, server_id, alert_id,user_id);
//			//falarmdba.close();
                String[] date_arr = date.split("-");
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(date_arr[0]),
                        Integer.parseInt(date_arr[1]),
                        Integer.parseInt(date_arr[2]),
                        Integer.parseInt(time_arr[0]),
                        Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())), 00);
                alarm.setAlarm(getApplicationContext(), cal,alarm_id, "1",
                        Constants.getExam_msg());
            } else if (parts[0].equalsIgnoreCase("exam_added")) {
                generateNotification("New Exam");
                String date = parts[1];
                String time = parts[2];
                String aid=parts[4];
                int alarm_id=Integer.parseInt(aid);
//			long alert_id = System.currentTimeMillis();
//			// Fixalarm falarmdba=new Fixalarm(getApplicationContext());
//		//	falarmdba.open();
//			falarmdba.createEntry(date, time, alert_type, server_id, alert_id,user_id);
//			//falarmdba.close();
                String[] date_arr = date.split("-");
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(date_arr[0]),
                        Integer.parseInt(date_arr[1]),
                        Integer.parseInt(date_arr[2]),
                        Integer.parseInt(time_arr[0]),
                        Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())), 00);
                alarm.setAlarm(getApplicationContext(), cal, alarm_id, "1",
                        Constants.getExam_msg());
            } else if (parts[0].equalsIgnoreCase("event_added")) {
                generateNotification("Event Scheduled");
                String date = parts[2];
                String time = parts[3];
                String alert_type = parts[4];
                String server_id = parts[5];
                String aid=parts[6];
                int alarm_id=Integer.parseInt(aid);
//			long alert_id = System.currentTimeMillis();
//			// Fixalarm falarmdba=new Fixalarm(getApplicationContext());
//			//falarmdba.open();
//			falarmdba.createEntry(date, time, alert_type, server_id, alert_id,user_id);
//			//falarmdba.close();
                String[] date_arr = date.split("-");
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(date_arr[0]),
                        Integer.parseInt(date_arr[1]),
                        Integer.parseInt(date_arr[2]),
                        Integer.parseInt(time_arr[0]),
                        Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())), 00);
                //Log.d("msg","SETTING EVENT ALARM AT "+cal.getTime());
                alarm.setAlarm(getApplicationContext(), cal,alarm_id, "1",
                        Constants.getEvent_msg());
            } else if (parts[0].equalsIgnoreCase("class_added")) {
                generateNotification(parts[1]);
                String day = parts[2];
                String time = parts[3];
                String alert_type = parts[4];
                String server_id = parts[5];
                String aid=parts[6];
                int alarm_id=Integer.parseInt(aid);
//			long alert_id = System.currentTimeMillis();
//			// Weekly_alarm ralarmdba=new Weekly_alarm(getApplicationContext());
//			ralarmdba.open();
//			long a=ralarmdba.createEntry(day, time, alert_type, server_id, alert_id,user_id);
//			System.out.println("insert values returned"+a);
//			ralarmdba.close();
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK,Integer.parseInt(day));
                //cal.set(Calendar.DAY_OF_WEEK,7);
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time_arr[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())));
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                alarm.setAlarm(getApplicationContext(), cal,alarm_id, "2",
                        Constants.getClass_msg());
            } else if (parts[0].equalsIgnoreCase("class_deleted")
                    || parts[0].equalsIgnoreCase("wplan_deleted")
                    || parts[0].equalsIgnoreCase("pplan_deleted")
                    ||parts[0].equalsIgnoreCase("assignment_deleted")
                    ||parts[0].equalsIgnoreCase("sevent_deleted")
                    || parts[0].equalsIgnoreCase("exam_deleted")) {
                String $msg="";
                String notif_msg="";
                if(parts[0].equalsIgnoreCase("sevent_deleted"))
                {
                    $msg=Constants.getEvent_msg();
                    notif_msg="Event Cancelled";
                }
                else if(parts[0].equalsIgnoreCase("wplan_deleted"))
                {
                    $msg=Constants.getWork_plan_msg();
                    notif_msg="Working Plan Cancelled";
                }
                else if(parts[0].equalsIgnoreCase("class_deleted"))
                {
                    $msg=Constants.getClass_msg();
                    notif_msg="Class Cancelled";
                }
                else if(parts[0].equalsIgnoreCase("pplan_deleted"))
                {
                    $msg=Constants.getPrep_plan_msg();
                    notif_msg="Preparation Plan Cancelled";
                }
                else if(parts[0].equalsIgnoreCase("assignment_deleted"))
                {
                    $msg=Constants.getAssignment_msg();
                    notif_msg="Assignment Cancelled";
                }
                else if(parts[0].equalsIgnoreCase("exam_deleted"))
                {
                    $msg=Constants.getExam_msg();
                    notif_msg="Exam Cancelled";
                }
                generateNotification(notif_msg);
                String aid=parts[1];
                long aiid=Long.parseLong(aid);
                int alarm_id=(int)aiid;
                alarm.cancelAlarm(getApplicationContext(), alarm_id,$msg);
            }
            else if (parts[0].equalsIgnoreCase("class_updated")) {

                String aid=parts[3];
                int alarm_id=Integer.parseInt(aid);
                alarm.cancelAlarm(getApplicationContext(), alarm_id,Constants.getClass_msg());
                String notif_msg="Timetable Updated";
                String day = parts[1];
                String time = parts[2];
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK,Integer.parseInt(day));
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time_arr[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())));
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                alarm.setAlarm(getApplicationContext(), cal,alarm_id, "2",
                        Constants.getClass_msg());
                generateNotification(notif_msg);
            } else if (parts[0].equalsIgnoreCase("assignment_updated")
                    || parts[0].equalsIgnoreCase("exam_updated")
                    || parts[0].equalsIgnoreCase("sevent_updated")) {
                String $msg="";
                String notif_msg="";
                int alarm_id=0;
                String date="";
                String time="";
                if(parts[0].equalsIgnoreCase("sevent_updated"))
                {
                    $msg=Constants.getEvent_msg();
                    notif_msg="Schedule Updated";
                    String aid=parts[1];
                    alarm_id=Integer.parseInt(aid);
                    date = parts[3];
                    time = parts[4];
                }
                else
                if(parts[0].equalsIgnoreCase("exam_updated"))
                {
                    $msg=Constants.getEvent_msg();
                    notif_msg="Exams Updated";
                    String aid=parts[4];
                    alarm_id=Integer.parseInt(aid);
                    date = parts[2];
                    time = parts[3];
                }
                else
                if(parts[0].equalsIgnoreCase("assignment_updated"))
                {
                    $msg=Constants.getAssignment_msg();
                    notif_msg="Assignment Updated";
                    String aid=parts[4];
                    alarm_id=Integer.parseInt(aid);
                    date = parts[2];
                    time = parts[3];
                }
                alarm.cancelAlarm(getApplicationContext(), alarm_id,$msg);

                generateNotification(notif_msg);
                String[] date_arr = date.split("-");
                String[] time_arr = time.split(":");
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.parseInt(date_arr[0]),
                        Integer.parseInt(date_arr[1]),
                        Integer.parseInt(date_arr[2]),
                        Integer.parseInt(time_arr[0]),
                        Integer.parseInt(time_arr[1])-Integer.parseInt(SaveSharedPreference.getPrePoneAlarm(getApplicationContext())), 00);
                alarm.setAlarm(getApplicationContext(), cal,alarm_id, "1",
                        $msg);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
  */


    }

    /**
     * Method called on receiving a deleted message
     * */
	/*@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		// displayMessage(context, message);
		// notifies user
		generateNotification(message);
	}
*/
    /**
     * Method called on Error
     * */
	/*@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_error, errorId));
	}
*/
/*
	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		// displayMessage(context,
		// getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}
*/

    /**
     * Issues a notification to inform the user that server has sent a message.
     */

    private  void generateNotification(String message) {

       /* int icon = R.drawable.i3;
        long when = System.currentTimeMillis();
        Notification notification;
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(GCMIntentService.this, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
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
*/
    }

}
