package meshsol.locationsharing;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

/*
*
 ///* Created by Wasiq Billah on 10/30/2015.
 */
public class LocationDisplay extends FragmentActivity {
    String senderLat;
    String senderLon;
    String sender;
    String senderByLink;
    String alertMessage;
    String extension;
    GoogleMap mMap;
    private LatLng userLastLocation;
    private boolean currentNotShown;
    private TextView tvMessage;
    private double lat=33.7472;
    private double lon=73.1389;
    String distance = "";
    String duration = "";
    private String senderName="";
    ProgressDialog pdialog;
    private Button updateButton;
    private Button stopUpdateButton;
    String messageBody;
    String user_speed;
    float user_speed_actual;
    String myNumber;
    String newETA;
    private  boolean responseSet;
    private boolean startedToMove;
    private float etaInMinutes;
    public String addressResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        senderLat=intent.getStringExtra("lat");
        senderLon=intent.getStringExtra("lon");
        sender=intent.getStringExtra("sender");
        extension=intent.getStringExtra("extension");
        user_speed=intent.getStringExtra("speed");
        senderByLink=intent.getStringExtra("senderByLink");
        //myNumber=intent.getStringExtra("deviceNumber");
        responseSet=false;
        startedToMove=false;
        etaInMinutes=0.0f;
        if(sender.equalsIgnoreCase("link")){
            senderName = getContactName(getApplicationContext(), senderByLink);
        }else {
            senderName = getContactName(getApplicationContext(), sender);
        }
        if(sender.equalsIgnoreCase("link")){

        }else {
            if(senderName!=""){
                alertMessage=senderName+" shared Location with You \n Do you want to see and share your location?";

            }else{
                alertMessage=sender+" shared Location with You \n Do you want to see and share your location?";
				//test;
            }

        }
        showLocation();
    }


    public void showLocation() {
        setContentView(R.layout.activity_maps);
        tvMessage = (TextView) findViewById(R.id.tvmsg);
        updateButton = (Button) findViewById(R.id.btnUpdateLocation);
        stopUpdateButton=(Button)findViewById(R.id.btnStopNotification);
        if (extension.equalsIgnoreCase("Response")) {
            updateButton.setVisibility(View.GONE);
            stopUpdateButton.setVisibility(View.GONE);
        } else {
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + " " + "#" + "" + "#updateLocation";
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        if (sender.equalsIgnoreCase("link")) {
                            smsManager.sendTextMessage(senderByLink, null, message, null, null);
                        } else {
                            smsManager.sendTextMessage(sender, null, message, null, null);
                        }
                        DevicePolicyManager mDPM = (DevicePolicyManager) LocationDisplay.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        mDPM.lockNow();
                    } catch (Exception e) {
                        //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                     Toast.makeText(getApplicationContext(), "Requested To Update Location...", Toast.LENGTH_SHORT).show();
                }
            });

            stopUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + " " + "#" + "" + "#stopUpdatingLocation";
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        if (sender.equalsIgnoreCase("link")) {
                            smsManager.sendTextMessage(senderByLink, null, message, null, null);
                        } else {
                            smsManager.sendTextMessage(sender, null, message, null, null);
                        }
                        DevicePolicyManager mDPM = (DevicePolicyManager) LocationDisplay.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        mDPM.lockNow();
                    } catch (Exception e) {
                        //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), "Requested To  Stop Update Location...", Toast.LENGTH_SHORT).show();
                }
            });
        }
            currentNotShown = true;
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            pdialog = new ProgressDialog(this);
            pdialog.setTitle("Progress...");
            pdialog.setMessage("Getting Current Location");
            pdialog.setCancelable(false);
            pdialog.show();

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            setUpMapIfNeeded();


    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SuppmortMapFragment.
            MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    setUpMap();
                }
            });
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            //mMap.addMarker(new MarkerOptions().position(loc));
            if((mMap != null && currentNotShown) || extension.equalsIgnoreCase("Response") ){
                showCurrentLocation(location);
            }else{
                setUpMapIfNeeded();
            }
        }
    };
    private void setUpMap() {
        if(mMap==null){
            setUpMapIfNeeded();
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {
                userLastLocation=new LatLng(loc.getLatitude(),loc.getLongitude());
                if(currentNotShown || extension.equalsIgnoreCase("Response")) {
                    lat=loc.getLatitude();
                    lon=loc.getLongitude();
                    showCurrentLocation(loc);
                    currentNotShown=false;
                }
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            //  ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
            //        LOCATION_SERVICE.MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }


        locationManager.requestLocationUpdates(provider, 24 * 60 * 60 * 1000, 50, locationListener);
        // Getting initial Location
        Location      location = locationManager.getLastKnownLocation(provider);


    }

    private void showCurrentLocation(Location loc){
        mMap.clear();
        currentNotShown=false;
        LatLng currentPosition = new LatLng(loc.getLatitude(),loc.getLongitude());
        if(!sender.equalsIgnoreCase("link") && extension.equalsIgnoreCase("Response") && !responseSet){
            currentPosition=new LatLng(Double.valueOf(SharePreferences.getPrefInitLat(getApplicationContext())),Double.valueOf(SharePreferences.getPrefInitLon(getApplicationContext())));
        }

        BitmapDescriptor bitmapMarker1 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        messageBody="Clicke below link to get user location \n http://meshsol.com/LocationSharing#"+loc.getLatitude()+"#"+loc.getLongitude()+"#Response#"+sender;
        mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .flat(true)
                .title("I'm here!")
                .icon(bitmapMarker1)).showInfoWindow();

        LatLng senderPosition=new LatLng(Double.valueOf(senderLat),Double.valueOf(senderLon));
        BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        mMap.addMarker(new MarkerOptions()
                .position(senderPosition)
                .flat(true)
                .title("Sender Position")
                .icon(bitmapMarker)).showInfoWindow();

        //save sender previous lat and lon to shared pref
        //get sender prevous lat and lon

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 10));
        if(extension.equalsIgnoreCase("Response")){
            long cur_time = System.currentTimeMillis() / 1000L;
            if(extension.equalsIgnoreCase("Request")|| SharePreferences.getPrefPrevLat(getApplicationContext())=="" || SharePreferences.getPrefPrevLon(getApplicationContext())=="" || SharePreferences.getPrefPrevTime(getApplicationContext())==""){
                Log.d("msg","shared pref values for response are null");
                SharePreferences.setPrevLat(getApplicationContext(), String.valueOf(loc.getLatitude()));
                SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
                SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));
            }
            if (loc.hasSpeed()) {
                user_speed_actual = loc.getSpeed();
                if(user_speed_actual==0.0f){
                    float[] result = new float[1];
                    loc.distanceBetween(Double.valueOf(SharePreferences.getPrefPrevLat(getApplicationContext())),
                            Double.valueOf(SharePreferences.getPrefPrevLon(getApplicationContext())), loc.getLatitude(),
                            loc.getLongitude(),
                            result);
                    float loc_distance = result[0];
                    //speed if location.getSpeed is null
                    user_speed_actual = loc_distance / (cur_time - Long.valueOf(SharePreferences.getPrefPrevTime(getApplicationContext())));
                }
            } else {
                //setting values to pref if not set yet....

                float[] result = new float[1];
                loc.distanceBetween(Double.valueOf(SharePreferences.getPrefPrevLat(getApplicationContext())), Double.valueOf(SharePreferences.getPrefPrevLon(getApplicationContext())),
                        loc.getLatitude(), loc.getLongitude(),
                        result);
                float loc_distance = result[0];
                //speed if location.getSpeed is null
                user_speed_actual = loc_distance / (cur_time - Long.valueOf(SharePreferences.getPrefPrevTime(getApplicationContext())));


            }
            if(Float.isNaN(user_speed_actual)){
                Log.d("msg","speed is NaN");
                user_speed_actual=0.0f;
            }
            // converting to km/hr
            user_speed_actual=((user_speed_actual*18)/5)*0.621f;
            user_speed=String.format("%.1f", user_speed_actual);
            if(Float.valueOf(user_speed)>5){
                startedToMove=true;
            }
            SharePreferences.setPrevLat(getApplicationContext(),String.valueOf(loc.getLatitude()));
            SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
            SharePreferences.setPrevTime(getApplicationContext(), String.valueOf((System.currentTimeMillis() / 1000L)));

        }
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
    private class DownloadTask extends AsyncTask<String, Void, String>{

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
            if(isConnectingToInternet())
            {
                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);
            }
            else
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LocationDisplay.this);
                alertDialogBuilder.setTitle("Exiting Application");
                alertDialogBuilder
                        .setMessage("Internet Connection is required! Please make sure you are connected to internet and then start application.")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(0);
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }


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

                    Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                    pdialog.cancel();
                    return;
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }

            String durationTrimmed = duration.replaceAll("[a-zA-Z]", "");
            float fDuration=Float.valueOf(durationTrimmed.replace(" ", ""));
            long millis=System.currentTimeMillis();
            long etaMillis=System.currentTimeMillis();
            if(duration.contains("hour") || duration.contains("hours")) {
                String[] parts=durationTrimmed.split("  ");
                long hour=Long.parseLong(parts[0].trim());
                long min=Long.parseLong(parts[1].trim());
                etaInMinutes=hour*min;
                etaMillis=hour*min*60*1000;
                millis=(long)System.currentTimeMillis()+hour*min*60*1000;

            }else {
                etaInMinutes=fDuration;
                etaMillis=(long)fDuration*60*1000;
                millis=(long)(System.currentTimeMillis() + fDuration * 60 * 1000);
            }
          //  SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
            DateFormat dateFormat=DateFormat.getTimeInstance();
            // Create a calendar object that will convert the date and time value in milliseconds to date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            newETA=dateFormat.format(calendar.getTime());
            if((extension.equalsIgnoreCase("Request") &&  Float.valueOf(user_speed)<5)){
                newETA="NOT DEPARTED";
            }
            if(extension.equalsIgnoreCase("Response") && Float.valueOf(user_speed)<5 && !startedToMove){
                newETA="NOT DEPARTED";
            }
            if(extension.equalsIgnoreCase("updatingNormal") && Float.valueOf(user_speed)<5 && SharePreferences.getPrefUpdatedNormal(getApplicationContext()).equalsIgnoreCase("false")){
                newETA="NOT DEPARTED";
                Log.e("msg update nor status1",""+SharePreferences.getPrefUpdatedNormal(getApplicationContext()));
                SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "true");
            }
            Random r=new Random();
            if(newETA.equalsIgnoreCase("Infinity")){
                newETA=String.valueOf(r.nextInt(6-1)+1);
            }
            if(user_speed.equalsIgnoreCase("Infinity")){
                newETA=String.valueOf(r.nextInt(6-1)+1);
            }
            Log.e("msg update nor status",""+SharePreferences.getPrefUpdatedNormal(getApplicationContext()));
            if(sender.equalsIgnoreCase("link")){
                if(senderName!="") {
                    tvMessage.setText("Sender: " + senderName + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                }else{
                    tvMessage.setText("Sender: " + senderByLink + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                }
            }else {
                if(senderName!="") {
                    tvMessage.setText("Sender: " + senderName + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                }else{
                    tvMessage.setText("Sender: " + sender + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                }
            }

            //Showing Notifications.

            if(extension.equalsIgnoreCase("UpdatingNormal") || extension.equalsIgnoreCase("UpdatingHighFreq") && !sender.equalsIgnoreCase("link")) {

                if(Float.valueOf(user_speed)>5){
                   // SharePreferences.setPrefUpdatedNormal(getApplicationContext(),"true");

                }
                String notification_message="ON TIME";
                Long requiredDiff=System.currentTimeMillis()-Long.valueOf(SharePreferences.getPrefPrevTimeEta(getApplicationContext(),"EtaStoringTime"));
                Long actualDiff=Long.valueOf(SharePreferences.getPrefPrevEta(getApplicationContext(), "ValueOfEta"))-etaMillis;
                int diff=(int)((requiredDiff-actualDiff)/1000)/60;
                Log.d("msg","ETA Diffrence: "+diff);
                if(diff<3){
                    if(senderName!="") {
                        notification_message = senderName + " is \n ON TIME";
                    }else{
                        notification_message = sender + " is \n ON TIME";
                    }
                }else if(diff<10){
                    if(senderName!="") {
                        notification_message = senderName + " is \n DLAYED";
                    }else{
                        notification_message = sender + " is \n DLAYED";
                    }
                }else if(diff>10){
                    if(senderName!="") {
                        notification_message = senderName + " is \n LATE";
                    }else{
                        notification_message = sender + " is \n LATE";
                    }
                }


                int icon = R.drawable.ic_launcher;
                long when = System.currentTimeMillis();
                Notification notification;
                NotificationManager notificationManager = (NotificationManager) LocationDisplay.this.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(LocationDisplay.this, MapsActivity.class);
                // set intent so it does not start a new activity
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                PendingIntent intent = PendingIntent.getActivity(LocationDisplay.this, 0, notificationIntent, 0);
                String title = getApplicationContext().getString(R.string.app_name);
                long nid = System.currentTimeMillis();
                int notification_id = (int) (nid);
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationDisplay.this);
                notification = builder.setContentIntent(intent)
                        .setSmallIcon(icon).setTicker(title).setWhen(when)
                        .setAutoCancel(true).setContentTitle(title)
                        .setContentText(notification_message).build();

                notificationManager.notify(notification_id, notification);
            }





            String formatedDistance=distance.replaceAll("[a-zA-Z]", "");
            float fdistance=Float.valueOf(formatedDistance);

            Log.d("msg","ETA: "+fDuration);
            if(fDuration<4 && extension.equalsIgnoreCase("UpdatingNormal") && !sender.equalsIgnoreCase("link") && SharePreferences.getPrefRequestedToChangeUpdateFrequency(getApplicationContext()).equalsIgnoreCase("true")){
                getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler());
                SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "true");
                //SEND MESSAGE TO CANCEL ALARM AFTER 5 AND STARD UPDATING AFTER 15 SECONDS
                messageBody="Clicke below link to get user location \n http://meshsol.com/LocationSharing#"+" "+"#"+""+"#changeUpdateFreq";
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(sender, null, messageBody, null, null);
                    DevicePolicyManager mDPM = (DevicePolicyManager) LocationDisplay.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    mDPM.lockNow();
                } catch (Exception e) {
                    //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }else if((fDuration<=1 || fdistance<10) && extension.equalsIgnoreCase("UpdatingHighFreq") && !sender.equalsIgnoreCase("link") && SharePreferences.getPrefStopedToSendMessagesMessageSent(getApplicationContext()).equalsIgnoreCase("false")) {
                getAddressFromLocation(lat, lon,LocationDisplay.this,new GeocoderHandler());
                SharePreferences.setPrefStopedSendingMessage(getApplicationContext(), "true");
                messageBody="Clicke below link to get user location \n http://meshsol.com/LocationSharing#"+" "+"#"+""+"#StopSendingUpdates";
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(sender, null,messageBody, null, null);
                    DevicePolicyManager mDPM = (DevicePolicyManager) LocationDisplay.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    mDPM.lockNow();
                } catch (Exception e) {
                    //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }



            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
            //if(!sender.equalsIgnoreCase("link") && !extension.equalsIgnoreCase("Response") && !extension.equalsIgnoreCase("Updating")) {
            if(extension.equalsIgnoreCase("Request")&& !sender.equalsIgnoreCase("link")) {
                getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler());
                SharePreferences.setPrevTimeEta(getApplicationContext(), "EtaStoringTime", String.valueOf(System.currentTimeMillis()));
                SharePreferences.setPrevEta(getApplicationContext(), "ValueOfEta", String.valueOf(etaMillis));
                SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "false");
                SharePreferences.setPrefStopedSendingMessage(getApplicationContext(), "false");
                SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "false");
                new AlertDialog.Builder(LocationDisplay.this)
                        .setTitle("Location Sharing App")
                        .setMessage(alertMessage)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sender, null,messageBody, null, null);
                                    Toast.makeText(LocationDisplay.this, "Location Shared Successfully!",
                                            Toast.LENGTH_LONG).show();
                                    DevicePolicyManager mDPM = (DevicePolicyManager) LocationDisplay.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                    mDPM.lockNow();
                                } catch (Exception e) {
                                    //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                                     }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //showLocation = false;
                                //   showLocation();
                                finish();
                            }


                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else  if(extension.equalsIgnoreCase("Response") && !sender.equalsIgnoreCase("link") && !responseSet){

                responseSet=true;
                AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent Receiverintent = new Intent(getApplicationContext(), RepeatNotification.class);
                Receiverintent.putExtra("sender",sender);
                Receiverintent.putExtra("frequency","normal");
                String sender1 = sender.replaceAll("\\D+","");
                long l = Long.parseLong(sender1);
                Log.d("msg","alarm id: "+l);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),(int)l, Receiverintent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1*60*1000,1*60*1000,pendingIntent);
                getAddressFromLocation(lat,lon,LocationDisplay.this,new GeocoderHandler());
            }


            if(pdialog.isShowing()) {
                pdialog.cancel();
            }

        }
    }


    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                dialog.cancel();
                                startActivity(callGPSSettingIntent);

                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // Checking for all possible internet providers
    public boolean isConnectingToInternet() {

        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
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





    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent1=new Intent(LocationDisplay.this,MapsActivity.class);
        startActivity(intent1);
    }



    private void updateServer(){
        Log.d("msg", "updating server");
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,AppManager.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        Log.d("msg",response);

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
                params.put("operation","updateLocation");
                if((extension.equalsIgnoreCase("Request") || extension.equalsIgnoreCase("Response")))
                    params.put("eta","Not Departed");
                params.put("eta",etaInMinutes+" min");
                params.put("distance",distance);
                params.put("speed", user_speed);
                params.put("lat", String.valueOf(lat));
                params.put("lon",String.valueOf(lon));
                params.put("location",addressResult);
                params.put("message_type",extension);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                formatter.format(calendar.getTimeInMillis());
                params.put("date_time", formatter.format(calendar.getTimeInMillis()));
                //params.put("user",myNumber);
                params.put("sender",sender);
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
                                              final Context context, final Handler handler) {
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
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            addressResult=locationAddress;
            updateServer();
        }
    }

}
