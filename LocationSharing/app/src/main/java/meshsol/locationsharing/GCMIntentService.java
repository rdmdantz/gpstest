package meshsol.locationsharing;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class GCMIntentService extends IntentService{

    private static final String TAG = "msg";
    Double myLat;
    Double myLon;
    Double senderLat;
    Double senderLon;
    private String senderSpeed;
    private String extension;
    String senderName;
    String senderPhone;
    String messageBody;
    String addressResult;
    String operation="";

    String distance="";
    String eta="";
    float etaInMinutes;
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
                senderName="";
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
                int cancel_alarm_id_hf=Integer.valueOf(SharePreferences.getPrefHighfrequencyAlarmId(getApplicationContext()));
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
                intent1.putExtra("msg","Guest Arrived");
                startActivity(intent1);

            } else if(parts[0].equalsIgnoreCase("stopUpdatingLocation")){
                //ENDING SESSION

                SharePreferences.setPrefSession(context,"");

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
                int cancel_alarm_id_hf=Integer.valueOf(SharePreferences.getPrefHighfrequencyAlarmId(getApplicationContext()));
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

            }else if(parts[0].equalsIgnoreCase("stopUpdatingLocation1")){
                //ENDING SESSION

                SharePreferences.setPrefSession(context,"");

                //STOP SENDING NORMAL UPDATES
             /*   AlarmManager aManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                int cancel_alarm_id_normal=Integer.valueOf(SharePreferences.getPrefNormalAlarmId(getApplicationContext()));
                Log.e("msg", "STOP SENDING NORMAL UPDATES for id: " + cancel_alarm_id_normal);
                Intent Receiverintent = new Intent(getApplicationContext(), RepeatNotification.class);
                PendingIntent alarmIntent;
                alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_normal, Receiverintent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent.cancel();
                aManager.cancel(alarmIntent);

                //STOP SENDING HIGH FREQUENCY UPDATES
                AlarmManager aManager1= (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                int cancel_alarm_id_hf=Integer.valueOf(SharePreferences.getPrefHighfrequencyAlarmId(getApplicationContext()));
                Log.d("msg", "STOP SENDING HIGH FREQUENCY UPDATES for id: " + cancel_alarm_id_hf);
                Intent Receiverintent1 = new Intent(getApplicationContext(), HighFreqAlarm.class);
                PendingIntent alarmIntent1;
                alarmIntent1 = PendingIntent.getBroadcast(getApplicationContext(),cancel_alarm_id_hf, Receiverintent1, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmIntent1.cancel();
                aManager1.cancel(alarmIntent1);

             */   Intent intent1 = new Intent();
                intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("msg","Guest decided to end Location Sharing Session");
                startActivity(intent1);

            }else if (parts[0].equalsIgnoreCase("updateLocation")) {
                intent = new Intent(context, LocationTracking.class);
                intent.putExtra("number", sender);
                Log.e("msg", "updateLocationOnRequest");
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
                intent1.putExtra("msg", "You have reached Destination!");
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
            }else  if(parts[0].equalsIgnoreCase("UpdatingNormal") || parts[0].equalsIgnoreCase("requestedUpdate")){


/*
                SharePreferences.setPrefServerUpdated(getApplicationContext(), "false");
                Intent intent1 = new Intent(GCMIntentService.this,LocationDisplay.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent1.putExtra("lat", parts[3]);
                intent1.putExtra("lon", parts[4]);
                intent1.putExtra("extension", parts[0]);
                intent1.putExtra("speed", parts[5]);
                intent1.putExtra("sender", parts[1]);
                startActivity(intent1);
*/


                extension=parts[0];
                senderPhone=parts[1];

                senderSpeed=parts[5];
                toastETA(parts[3],parts[4]);

            }else if(parts[0].equalsIgnoreCase("UpdatingHighFreq")){

                SharePreferences.setPrefServerUpdated(getApplicationContext(), "false");
                Intent intent1 = new Intent(GCMIntentService.this,LocationDisplay.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    {


        final Context MyContext = getApplicationContext();
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


    private void toastETA(String senderLat,String senderLon){
        this.senderLat=Double.valueOf(senderLat);
        this.senderLon=Double.valueOf(senderLon);

        if(SharePreferences.getPrefIsSafeway(getApplicationContext())){

            myLat=AppManager.SafewayLatitude;
            myLon=AppManager.SafewayLongitude;
        }
        else{
            myLat=Double.valueOf(SharePreferences.getPrefMyLat(getApplicationContext()));
            myLon=Double.valueOf(SharePreferences.getPrefMyLon(getApplicationContext()));
        }
        Location myLoc=new Location("");
        myLoc.setLatitude(myLat);
        myLoc.setLongitude(myLon);


        LatLng currentPosition = new LatLng(myLat,myLon);
        LatLng senderPosition=new LatLng(Double.valueOf(senderLat),Double.valueOf(senderLon));


        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(currentPosition, senderPosition);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        //  return "https://maps.googleapis.com/maps/api/directions/json?origin=33.653354,73.0866419&destination=33.65335,73.9&sensor=false";
        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            MarkerOptions markerOptions = new MarkerOptions();

            try {
                if (result.size() < 1) {

                    ShowToast("No Path available!");
                    return;
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            // Traversing through all the routes
            try {
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        if (j == 0) {    // Get distance from the list
                            distance = (String) point.get("distance");
                            continue;
                        } else if (j == 1) { // Get duration from the list
                            eta = (String) point.get("duration");
                            continue;
                        }

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(5);
                    lineOptions.color(Color.RED);
                }

                // customizing duration/Time ETA
                String durationTrimmed = eta.replaceAll("[a-zA-Z]", "");
                float fDuration = Float.valueOf(durationTrimmed.replace(" ", ""));
                long etaTimeInMillis = System.currentTimeMillis(); //just initializing...
                Long etaMillis = System.currentTimeMillis(); //just initializing...
                float etaInMinutes;
                if (eta.contains("hour") || eta.contains("hours")) {
                    String[] parts = durationTrimmed.split("  ");
                    long hour = Long.parseLong(parts[0].trim());
                    long min = Long.parseLong(parts[1].trim());
                    etaInMinutes = (hour * 60) + min;
                    etaMillis = ((hour * 60 * 60) + (min * 60)) * 1000;
                    etaTimeInMillis = (long) System.currentTimeMillis() + etaMillis;

                } else {
                    etaInMinutes = fDuration;
                    etaMillis = (long) fDuration * 60 * 1000;
                    etaTimeInMillis = (long) (System.currentTimeMillis() + fDuration * 60 * 1000);
                }

                // Changing ETA from millis to Time  SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
                DateFormat dateFormat = DateFormat.getTimeInstance();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(etaTimeInMillis);
                String etaTime = dateFormat.format(calendar.getTime());

                SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "true");


                // Changing display messages for speed and ETA if messages are vulnerable
                Random r = new Random();
                if (etaTime.equalsIgnoreCase("Infinity")) {
                    etaTime = String.valueOf(r.nextInt(6 - 1) + 1);
                }
                if (senderSpeed.equalsIgnoreCase("Infinity")) {
                    senderSpeed = String.valueOf(r.nextInt(6 - 1) + 1);
                }

                senderName=getContactName(GCMIntentService.this,senderPhone);
                //Formatting Messages for Displaying
                if (senderName != "") {
                    ShowToast(senderName + " is coming to me\n" + "Distance: " + distance + "\n Speed: " + senderSpeed + " miles/h \n ETA: " + etaTime+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                } else {
                    ShowToast(senderPhone + " is coming to me\n" + "Distance: " + distance + "\n Speed: " + senderSpeed + " miles/h \n ETA: " + etaTime+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                }


                //Showing Notifications for updatesNormal or HighFrequency
                if (extension.equalsIgnoreCase("UpdatingNormal") || extension.equalsIgnoreCase("UpdatingHighFreq") || extension.equalsIgnoreCase("requestedUpdate") && SharePreferences.getPrefServerUpdated(getApplicationContext()).equalsIgnoreCase("false")) {

                    SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "true");
                    SharePreferences.setPrefServerUpdated(getApplicationContext(), "true");
                    // operation = "updateLocationDb";
                    getAddressFromLocation(senderLat, senderLon, GCMIntentService.this, new GeocoderHandler(), "updateLocationDb");
                    Long requiredDiff = System.currentTimeMillis() - Long.valueOf(SharePreferences.getPrefPrevTimeEta(getApplicationContext(), "EtaStoringTime"));
                    Long actualDiff = Long.valueOf(SharePreferences.getPrefPrevEta(getApplicationContext(), "ValueOfEta")) - etaMillis;
                    int diff = (int) ((requiredDiff - actualDiff) / 1000) / 60;
                    Log.d("msg", "ETA Diffrence: " + diff);
                }


                //Formating Distance
                float distance_in_meters = 0.0f; //just initializing
                if (distance.contains("k") || distance.contains("K")) {
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters = Float.valueOf(formatedDistance) * 1000;
                } else if (distance.contains("i") || distance.contains("l")) {
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters = Float.valueOf(formatedDistance) * 1609.34f;
                }else if (distance.contains("f") || distance.contains("F")) {
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters = Float.valueOf(formatedDistance)/3.0f;
                }else {
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters = Float.valueOf(formatedDistance);
                }
                if (distance_in_meters<17 && extension.equalsIgnoreCase("UpdatingNormal") && SharePreferences.getPrefRequestedToChangeUpdateFrequency(getApplicationContext()).equalsIgnoreCase("false")) {


                        getAddressFromLocation(senderLat, senderLon, GCMIntentService.this, new GeocoderHandler(),"changeUpdateFreq");  //Need to send myLat and myLon but not important here.
                        SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "true");
                        Log.d("msg", "changed update freq");
                    }

            } catch (Exception e) {
                        e.printStackTrace();
            }
        }
    }



                private void updateServer(final String thisOperation){
                    Log.d("msg", "updating server  for operation :"+thisOperation);
                    RequestQueue queue = Volley.newRequestQueue(this);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,AppManager.url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    // Result handling
                                    Log.d("response msg", response);
                                    if(response.equalsIgnoreCase("false")){
                                        Toast.makeText(getApplicationContext(),"operation "+operation,Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            // Error handling
                            System.out.println("Something went wrong!");
                            error.printStackTrace();

                        }
                    })
                    {
                        //Code to send parameters to server
                        @Override
                        protected Map getParams() throws AuthFailureError
                        {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("operation",thisOperation);
                            params.put("eta", etaInMinutes + " min");
                            params.put("distance",distance);
                            params.put("speed", senderSpeed);
                            params.put("lat", String.valueOf(senderLat));
                            params.put("lon",String.valueOf(senderLon));
                            params.put("location",addressResult);
                            params.put("message_type",extension);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Calendar calendar = Calendar.getInstance();
                            formatter.format(calendar.getTimeInMillis());
                            params.put("date_time", formatter.format(calendar.getTimeInMillis()));
                            params.put("receiver",senderPhone);
                            params.put("sender",SharePreferences.getPrefUserNumber(getApplicationContext()));
                            params.put("guest_server_id",SharePreferences.getPrefGuestServerId(getApplicationContext()));
                            params.put("session_id",SharePreferences.getPrefSessionid(getApplicationContext()));
                            Log.d("msg","Params: "+params.toString());
                            return params;
                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            10000,
                            3,
                            1));
                    queue.add(stringRequest);
                }


                public static void getAddressFromLocation(final double latitude, final double longitude,
                final Context context, final Handler handler, final String Serveroperation) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            String result = null;
                            try {
                                List<Address> addressList = geocoder.getFromLocation(
                                        latitude, longitude, 1);
                                if (addressList != null && addressList.size() > 0) {
                                    Address address = addressList.get(0);
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                        sb.append(address.getAddressLine(i)).append("\n");
                                    }
                                    //sb.append(address.getLocality()).append("\n");
                                    //sb.append(address.getPostalCode()).append("\n");
                                    sb.append(address.getCountryName());
                                    result = sb.toString();
                                }
                            } catch (IOException e) {
                                Log.e("msg", "Unable connect to Geocoder", e);
                            } finally {
                                Message message = Message.obtain();
                                message.setTarget(handler);
                                if (result != null) {
                                    message.what = 1;
                                    Bundle bundle = new Bundle();
                                    //result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    //      "\n\nAddress:\n" + result;

                                    bundle.putString("address", result);
                                    bundle.putString("operation",Serveroperation);
                                    message.setData(bundle);
                                } else {
                                    message.what = 1;
                                    Bundle bundle = new Bundle();
                                    result = "Latitude: " + latitude + " Longitude: " + longitude +
                                            "\n Unable to get address for this lat-long.";
                                    bundle.putString("address", result);
                                    message.setData(bundle);
                                }
                                message.sendToTarget();
                            }
                        }
                    };
                    thread.start();
                }

                public class GeocoderHandler extends Handler {
                    @Override
                    public void handleMessage(Message message) {
                        String locationAddress;
                        String oper="";
                        switch (message.what) {
                            case 1:
                                Bundle bundle = message.getData();
                                locationAddress = bundle.getString("address");
                                oper=bundle.getString("operation");
                                break;
                            default:
                                locationAddress = null;
                        }
                        addressResult=locationAddress;
                        updateServer(oper);
                    }
                }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }


}
