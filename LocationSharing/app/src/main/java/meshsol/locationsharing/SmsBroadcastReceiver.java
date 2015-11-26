package meshsol.locationsharing;

/**
 * Created by Wasiq Billah on 10/30/2015.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
            Context appContext=context.getApplicationContext();
        try {
            Bundle intentExtras = intent.getExtras();
            //AlarmManager aManager = (AlarmManager) context.getSemService(context.ALARM_SERVICE);
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                String smsMessageStr = "";
                for (int i = 0; i < sms.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                    String smsBody = smsMessage.getMessageBody().toString();
                    String address = smsMessage.getOriginatingAddress();
                    smsMessageStr += "SMS From: " + address + "\n";
                    smsMessageStr += smsBody + "\n";
                    Log.d("msg","receiver msg body: "+smsBody);
                    String[] separated = smsBody.split("#");
                    String subAddress = "";
                    subAddress = address.replaceAll("\\D+", "");

                    long l = Long.parseLong(subAddress);

                    if (separated[0] != null && separated[0] != "" && separated[0].equalsIgnoreCase("Clicke below link to get user location\n http://meshsol.com/LocationSharing")) {
                            Log.d("msg","in receiver");
                        if(separated[3].equalsIgnoreCase("stopUpdatingLocation") && SharePreferences.getPrefSession(context)!=null && SharePreferences.getPrefSession(context)!="" && SharePreferences.getPrefSession(context).equalsIgnoreCase("active")){
                            //ENDING SESSION
                            SharePreferences.setPrefSession(context,"destroyed");

                            //STOP SENDING NORMAL UPDATES
                            AlarmManager aManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                            Log.d("msg", "STOP SENDING NORMAL UPDATES afor id: " + l);
                            Intent Receiverintent = new Intent(context, RepeatNotification.class);
                            PendingIntent alarmIntent;
                            alarmIntent = PendingIntent.getBroadcast(context, (int) l, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmIntent.cancel();
                            aManager.cancel(alarmIntent);

                            //STOP SENDING HIGH FREQUENCY UPDATES
                            AlarmManager aManager1= (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                            Log.d("msg", "STOP SENDING HIGH FREQUENCY UPDATES for id: " + l);
                            Intent Receiverintent1 = new Intent(context, HighFreqAlarm.class);
                            PendingIntent alarmIntent1;
                            alarmIntent1 = PendingIntent.getBroadcast(context, (int) l, Receiverintent1, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmIntent1.cancel();
                            aManager1.cancel(alarmIntent1);

                        }else if (separated[3].equalsIgnoreCase("updateLocation") && SharePreferences.getPrefSession(context)!=null && SharePreferences.getPrefSession(context)!="" && SharePreferences.getPrefSession(context).equalsIgnoreCase("active")) {
                            intent = new Intent(context, LocationTracking.class);
                            intent.putExtra("number", address);
                            intent.putExtra("frequency","requestedUpdate");
                            appContext.startService(intent);

                        } else if (separated[3].equalsIgnoreCase("changeUpdateFreq")&& SharePreferences.getPrefSession(context).equalsIgnoreCase("active")) {
                            AlarmManager aManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                            Log.d("msg", "Cancelling alarm for id: " + l);
                            Intent Receiverintent = new Intent(context, RepeatNotification.class);
                            PendingIntent alarmIntent;
                            alarmIntent = PendingIntent.getBroadcast(context, (int) l, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmIntent.cancel();
                            aManager.cancel(alarmIntent);

                            intent = new Intent(context, LocationTracking.class);
                            intent.putExtra("specialMesssage", "startHighFreqAlarm");
                            intent.putExtra("address", address);
                            intent.putExtra("alarmId", "" + l);
                            appContext.startService(intent);

                        } else if (separated[3].equalsIgnoreCase("StopSendingUpdates") && SharePreferences.getPrefSession(context)!=null && SharePreferences.getPrefSession(context)!="" && SharePreferences.getPrefSession(context).equalsIgnoreCase("active")) {
                            AlarmManager aManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                            Log.d("msg", "Stoping to send more sms for id: " + l);
                            Intent Receiverintent = new Intent(context, HighFreqAlarm.class);
                            PendingIntent alarmIntent;
                            alarmIntent = PendingIntent.getBroadcast(context, (int) l, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmIntent.cancel();
                            aManager.cancel(alarmIntent);
                        } else {
                            Log.d("msg","starting activity");
                            Intent intent1 = new Intent(appContext,LocationDisplay.class);
                            //intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.LocationDisplay");
                            intent1.setFlags(intent1.FLAG_ACTIVITY_NEW_TASK
                                    | intent1.FLAG_ACTIVITY_CLEAR_TOP
                                    | intent1.FLAG_ACTIVITY_SINGLE_TOP);
                            intent1.putExtra("lat", separated[1]);
                            intent1.putExtra("lon", separated[2]);
                            intent1.putExtra("extension", separated[3]);
                            if(separated[3].equalsIgnoreCase("Response")){
                                intent1.putExtra("server_id", separated[4]);
                            }else{
                                intent1.putExtra("speed", separated[4]);
                                intent1.putExtra("server_id", separated[5]);
                            }
                            if(separated.length>6 && separated[6].equalsIgnoreCase("safeway")){
                             SharePreferences.setPrefIsSafeway(appContext,true);
                            }else{
                                SharePreferences.setPrefIsSafeway(appContext,false);
                            }
                            intent1.putExtra("sender", address);

                            appContext.startActivity(intent1);
                        }
                    }else{
                        Log.d("msg","other messages goes here");
                    }


                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}