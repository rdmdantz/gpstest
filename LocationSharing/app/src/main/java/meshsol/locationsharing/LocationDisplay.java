package meshsol.locationsharing;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

/**
 * Created by Wasiq Billah on 10/30/2015.
 */
public class LocationDisplay extends FragmentActivity {
    ProgressDialog pdialog;
    private Button updateButton;
    private Button stopUpdateButton;

    private Button updateButton1;
    private Button stopUpdateButton1;

    private Button btnGetCustomLocation;
    private EditText etGetLocation;
    private TextView tvMessage;
    GoogleMap mMap;

    private String senderLat="";
    private String senderLon="";
    private String sender="";
    private String senderByLink="";
    private String alertMessage="";
    private String extension="";
    private boolean currentNotShown=false;
    private double lat=33.7472;
    private double lon=73.1389;
   // private double SafewayLatitude=37.3946841;    //for client
   // private double SafewayLongitude=-121.9480919; //for client
   // private double SafewayLatitude=33.6505986;  //Sheryar biryani center
   // private double SafewayLongitude=73.0889171; //Sheryar biryani center
    private String distance = "";
    private String duration = "";
    private String senderName="";
    private String messageBody="";
    private String user_speed="";
    private float user_speed_actual=0.0f;
    private String newETA="";
    private  boolean responseSet=false;
    private boolean startedToMove=false;
    private float etaInMinutes=0.0f;
    public String addressResult="";
    private long etaMillis;
    private String operation="";
    private String server_id="";
    private LocationManager thisLocationManager;
    public MyLocationListener myLocationListener;
    private ImageButton btMenuLocationDisplay;
    static final int PICK_CONTACT= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        String caller=intent.getStringExtra("caller");
        if(caller!=null && caller.equalsIgnoreCase("home")){
            setContentView(R.layout.activity_maps_buttons);


/*
            btMenuLocationDisplay=(ImageButton)findViewById(R.id.btMenuLocationDisplay);
            btMenuLocationDisplay.setVisibility(View.GONE);
*/

            updateButton1 = (Button) findViewById(R.id.btnUpdateLocation1);
            stopUpdateButton1=(Button)findViewById(R.id.btnStopNotification1);
            updateButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        getAddressFromLocation(lat,lon,LocationDisplay.this,new GeocoderHandler(),"updateLocation");

                    } catch (Exception e) {
                        Log.e("error msg", e.getMessage().toString());
                    }

                    Toast.makeText(getApplicationContext(), "Requested To Update Location...", Toast.LENGTH_SHORT).show();
                }
            });

            stopUpdateButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(), "stopUpdatingLocation");
                        SharePreferences.setPrefMode(getApplicationContext(), "");
                        SharePreferences.setPrefSession(getApplicationContext(), "");
                        Intent intents = new Intent(LocationDisplay.this,MapsActivity.class);
                        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intents);
                        finish();
                    } catch (Exception e) {
                        Log.e("msg", e.getMessage().toString());
                    }

                    Toast.makeText(getApplicationContext(), "Requested To  Stop Update Location...", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
           /* SharedPreferences save = getSharedPreferences(SAVE, MODE_PRIVATE);
            SharedPreferences.Editor ed = save.edit();
            SharePreferences.saveBundle(ed, "", bundle);*/
            senderLat = intent.getStringExtra("lat");
            senderLon = intent.getStringExtra("lon");
            sender = intent.getStringExtra("sender");
            extension = intent.getStringExtra("extension");
            user_speed = intent.getStringExtra("speed");
            senderByLink = intent.getStringExtra("senderByLink");
            server_id = intent.getStringExtra("server_id");
            responseSet = false;
            startedToMove = false;
            etaInMinutes = 0.0f;
            myLocationListener = new MyLocationListener();


            //GETTING SENDER NAME
            if (sender.equalsIgnoreCase("link")) {
                senderName = getContactName(getApplicationContext(), senderByLink);
            } else {
                senderName = getContactName(getApplicationContext(), sender);
            }
            if (sender.equalsIgnoreCase("link")) {

            } else {
                if (senderName != "") {
                    alertMessage = senderName + " shared Location with You \n Do you want to see and share your location?";

                } else {
                    alertMessage = sender + " shared Location with You \n Do you want to see and share your location?";

                }

            }
            showLocation();
        }
    }


    public void showLocation() {
        setContentView(R.layout.activity_maps);
        btMenuLocationDisplay=(ImageButton)findViewById(R.id.btMenuLocationDisplay);

/*
        btMenuLocationDisplay.setVisibility(View.GONE);
*/

        btnGetCustomLocation=(Button)findViewById(R.id.btnGetCustomLocation);
        btnGetCustomLocation.setVisibility(View.GONE);
        tvMessage = (TextView) findViewById(R.id.tvmsg);
        updateButton = (Button) findViewById(R.id.btnUpdateLocation);
        stopUpdateButton=(Button)findViewById(R.id.btnStopNotification);
        etGetLocation=(EditText)findViewById(R.id.etSearchLocation);
        etGetLocation.setVisibility(View.GONE);
        if (extension.equalsIgnoreCase("Response")) {
            updateButton.setVisibility(View.GONE);
            stopUpdateButton.setVisibility(View.GONE);
        } else {
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                            getAddressFromLocation(lat,lon,LocationDisplay.this,new GeocoderHandler(),"updateLocation");

                    } catch (Exception e) {
                       Log.e("error msg", e.getMessage().toString());
                    }

                     Toast.makeText(getApplicationContext(), "Requested To Update Location...", Toast.LENGTH_SHORT).show();
                }
            });

            stopUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                            getAddressFromLocation(lat,lon,LocationDisplay.this,new GeocoderHandler(),"stopUpdatingLocation");

                    } catch (Exception e) {
                        Log.e("msg",e.getMessage().toString());
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
            //    userLastLocation=new LatLng(loc.getLatitude(),loc.getLongitude());
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
        Location location = locationManager.getLastKnownLocation(provider);


    }

    private void showCurrentLocation(Location loc){

        //Registering Location Manager for further updates
        thisLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = thisLocationManager.getBestProvider(criteria, true);





        mMap.clear();
        currentNotShown=false;
        if(SharePreferences.getPrefIsSafeway(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"safeway",Toast.LENGTH_SHORT).show();
            loc.setLatitude(AppManager.SafewayLatitude);
            loc.setLongitude(AppManager.SafewayLongitude);
        }

        lat=loc.getLatitude();
        lon=loc.getLongitude();
        LatLng currentPosition = new LatLng(loc.getLatitude(),loc.getLongitude());

        if(!sender.equalsIgnoreCase("link") && extension.equalsIgnoreCase("Response") && !responseSet){
            currentPosition=new LatLng(Double.valueOf(SharePreferences.getPrefInitLat(getApplicationContext())),Double.valueOf(SharePreferences.getPrefInitLon(getApplicationContext())));     //COMMENTED B/C NOT DOING GOOD AS SOMETIME ETA HAS BEEN PASSED
        }

        BitmapDescriptor bitmapMarker1 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        messageBody="Clicke below link to get user location\n http://meshsol.com/LocationSharing#"+loc.getLatitude()+"#"+loc.getLongitude()+"#Response#"+SharePreferences.getPrefUserServerId(getApplicationContext());
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

                    Toast.makeText(getBaseContext(), "No Path available by Road! You can use Air Service", Toast.LENGTH_SHORT).show();
                    pdialog.cancel();
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
                            duration = (String) point.get("duration");
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
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);

                // customizing duration/Time ETA
                String durationTrimmed = duration.replaceAll("[a-zA-Z]", "");
                float fDuration = Float.valueOf(durationTrimmed.replace(" ", ""));
                long millis = System.currentTimeMillis();
                etaMillis = System.currentTimeMillis(); //just initializing...

                if (duration.contains("hour") || duration.contains("hours")) {
                    String[] parts = durationTrimmed.split("  ");
                    long hour = Long.parseLong(parts[0].trim());
                    long min = Long.parseLong(parts[1].trim());
                    etaInMinutes = (hour * 60) + min;
                    etaMillis = hour * min * 60 * 1000;
                    millis = (long) System.currentTimeMillis() + hour * min * 60 * 1000;

                } else {
                    etaInMinutes = fDuration;
                    etaMillis = (long) fDuration * 60 * 1000;
                    millis = (long) (System.currentTimeMillis() + fDuration * 60 * 1000);
                }

                // Changing ETA from millis to Time  SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
                DateFormat dateFormat = DateFormat.getTimeInstance();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(millis);
                newETA = dateFormat.format(calendar.getTime());

                //Special messages for request response and updating Normal
                if ((extension.equalsIgnoreCase("Request") && Float.valueOf(user_speed) < 5)) {
                    newETA = "NOT DEPARTED";
                }
                if (extension.equalsIgnoreCase("Response") && Float.valueOf(user_speed) < 5 && !startedToMove) {
                    newETA = "NOT DEPARTED";
                }
                if (extension.equalsIgnoreCase("updatingNormal") && Float.valueOf(user_speed) < 5 && SharePreferences.getPrefUpdatedNormal(getApplicationContext()).equalsIgnoreCase("false")) {
                    newETA = "NOT DEPARTED";
                    SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "true");
                }

                // Changing display messages for speed and ETA if messages are vulnerable
                Random r = new Random();
                if (newETA.equalsIgnoreCase("Infinity")) {
                    newETA = String.valueOf(r.nextInt(6 - 1) + 1);
                }
                if (user_speed.equalsIgnoreCase("Infinity")) {
                    user_speed = String.valueOf(r.nextInt(6 - 1) + 1);
                }

                try {
                    Float f = Float.valueOf(user_speed);
                    if (user_speed.matches("\\-?\\d+")){//optional minus and at least one digit
                     //   System.out.println("integer" + d);
                    } else {
                        System.out.println("float" + f);
                    }
                } catch (Exception e) {
                    System.out.println("not number");
                }


                //Formatting Messages for Displaying
                if (sender.equalsIgnoreCase("link")) {
                    Toast.makeText(LocationDisplay.this,"Activity started from message in inbox",Toast.LENGTH_LONG).show();
                    if(extension.equalsIgnoreCase("Response")) {
                        if (senderName != "") {
                            tvMessage.setText("I am going to " + senderName + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                        } else {
                            tvMessage.setText("I am going to" + senderByLink + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                        }
                    }else{
                        if (senderName != "") {
                            tvMessage.setText(senderName + " is coming to me\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                        } else {
                            tvMessage.setText(senderByLink + " is coming to me \n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA);
                        }
                    }
                } else {
                    if(extension.equalsIgnoreCase("Response")) {
                        if (senderName != "") {
                            tvMessage.setText("I am going to " + senderName + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                        } else {
                            tvMessage.setText("I am going to" + sender + "\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                        }
                    }else{
                        if (senderName != "") {
                            tvMessage.setText( senderName + " is coming to me\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                        } else {
                            tvMessage.setText(sender + " is coming to me\n" + "Distance: " + distance + "\n Speed: " + user_speed + " miles/h \n ETA: " + newETA+"\n Session ID: "+SharePreferences.getPrefSessionid(getApplicationContext()));
                        }
                    }
                }

                //Showing Notifications for updatesNormal or HighFrequency
              //  extension.equalsIgnoreCase("requestedUpdate") || extension.equalsIgnoreCase("UpdatingNormal") ||
                if ( extension.equalsIgnoreCase("UpdatingHighFreq") && SharePreferences.getPrefServerUpdated(getApplicationContext()).equalsIgnoreCase("false") && !sender.equalsIgnoreCase("link"))
                {

                    SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "true");
                    SharePreferences.setPrefServerUpdated(getApplicationContext(), "true");

                    getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(),"updateLocationDb");

               /*     String notification_message = "ON TIME";
                    Long requiredDiff = System.currentTimeMillis() - Long.valueOf(SharePreferences.getPrefPrevTimeEta(getApplicationContext(), "EtaStoringTime"));
                    Long actualDiff = Long.valueOf(SharePreferences.getPrefPrevEta(getApplicationContext(), "ValueOfEta")) - etaMillis;
                    int diff = (int) ((requiredDiff - actualDiff) / 1000) / 60;
                    Log.d("msg", "ETA Diffrence: " + diff);
                    if (diff < 3) {
                        if (senderName != "") {
                            notification_message = senderName + " is \n ON TIME";
                        } else {
                            notification_message = sender + " is \n ON TIME";
                        }
                    } else if (diff < 10) {
                        if (senderName != "") {
                            notification_message = senderName + " is \n DLAYED";
                        } else {
                            notification_message = sender + " is \n DLAYED";
                        }
                    } else if (diff > 10) {
                        if (senderName != "") {
                            notification_message = senderName + " is \n LATE";
                        } else {
                            notification_message = sender + " is \n LATE";
                        }
                    }


                    int icon = R.drawable.ic_launcher;
                    long when = System.currentTimeMillis();
                    Notification notification;
                    NotificationManager notificationManager = (NotificationManager) LocationDisplay.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent notificationIntent = new Intent(LocationDisplay.this, MapsActivity.class);
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

                    notificationManager.notify(notification_id, notification);*/
                }

                //Formating Distance
                float distance_in_meters=0.0f; //just initializing
                if(distance.contains("k") || distance.contains("K")){
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters=Float.valueOf(formatedDistance)*1000;
                }else  if(distance.contains("i") || distance.contains("l")){
                    String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                    distance_in_meters=Float.valueOf(formatedDistance)*1609.34f;
                }else{
                      String formatedDistance = distance.replaceAll("[a-zA-Z]", "");
                      distance_in_meters=Float.valueOf(formatedDistance);
                }
                    //etaInMinutes < 4
               /* if (distance_in_meters <1000 && extension.equalsIgnoreCase("UpdatingNormal") && !sender.equalsIgnoreCase("link") && SharePreferences.getPrefRequestedToChangeUpdateFrequency(getApplicationContext()).equalsIgnoreCase("false")) {
                    SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "true");
                    //SEND MESSAGE TO CANCEL ALARM AFTER 5 AND STARD UPDATING AFTER 15 SECONDS
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + " " + "#" + "" + "#changeUpdateFreq";
                    try {

                        operation = "changeUpdateFreq";
                        getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(),"changeUpdateFreq");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }   //etaInMinutes<= 1 ||
                } else*/ if ((distance_in_meters <17) && extension.equalsIgnoreCase("UpdatingHighFreq") && !sender.equalsIgnoreCase("link") && SharePreferences.getPrefStopedToSendMessagesMessageSent(getApplicationContext()).equalsIgnoreCase("false")) {
                    SharePreferences.setPrefStopedSendingMessage(getApplicationContext(), "true");
                  //  messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + " " + "#" + "" + "#StopSendingUpdates";
                    try {

                        operation = "StopSendingUpdates";
                        getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(),"StopSendingUpdates");
                        Intent intent1 = new Intent();
                        intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.putExtra("msg", "Guest Arrived");
                        startActivity(intent1);


                       } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (extension.equalsIgnoreCase("Request") && !sender.equalsIgnoreCase("link")) {

                    new AlertDialog.Builder(LocationDisplay.this)
                            .setTitle("Location Sharing App")
                            .setMessage(alertMessage)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    try {
                                      //  operation = "updateLocationDb";
                                      //  SharePreferences.setPrefSession(getApplicationContext(), "active");

                                        getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(), "updateLocationDb");

                                        SharePreferences.setPrefGuestPhone(getApplicationContext(),sender);
                                        SharePreferences.setPrefMyLat(getApplicationContext(), String.valueOf(lat));
                                        SharePreferences.setPrefMyLon(getApplicationContext(),String.valueOf(lon));
                                        SharePreferences.setPrevTimeEta(getApplicationContext(), "EtaStoringTime", String.valueOf(System.currentTimeMillis()));
                                        SharePreferences.setPrevEta(getApplicationContext(), "ValueOfEta", String.valueOf(etaMillis));
                                        SharePreferences.setPrefUpdatedNormal(getApplicationContext(), "false");
                                        SharePreferences.setPrefStopedSendingMessage(getApplicationContext(), "false");
                                        SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "false");
                                        SharePreferences.setPrefServerUpdated(getApplicationContext(), "false");
                                        SharePreferences.setPrefGuestServerId(getApplicationContext(), server_id);
                                        SharePreferences.setPrefMode(getApplicationContext(), AppManager.hostMode);

                                        try {
                                            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                            startActivityForResult(intent, PICK_CONTACT);
                                        } catch(Exception e){
                                            e.printStackTrace();
                                        }


                                     //   SmsManager smsManager = SmsManager.getDefault();
                                     //   smsManager.sendTextMessage(sender, null, messageBody, null, null);   //SENDING RESPONSE
                                      /*  Toast.makeText(LocationDisplay.this, "Location Shared Successfully! with"+sender,
                                                Toast.LENGTH_LONG).show();
                                        finish();*/
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }


                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else if (extension.equalsIgnoreCase("Response") && !sender.equalsIgnoreCase("link") && !responseSet) {

                    responseSet = true;
                    SharePreferences.setPrefSession(getApplicationContext(), "active");
                    SharePreferences.setPrefMode(getApplicationContext(), AppManager.guestMode);
                    SharePreferences.setPrefHostServerId(getApplicationContext(), server_id);
                    SharePreferences.setPrefHostPhone(getApplicationContext(),sender);

                    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Intent Receiverintent = new Intent(getApplicationContext(), RepeatNotification.class);
                    Receiverintent.putExtra("sender", sender);
                    Receiverintent.putExtra("frequency", "normal");
                    String sender1 = sender.replaceAll("\\D+", "");
                    long l = Long.parseLong(sender1);
                    int alarm_id_int=(int)l;
                    SharePreferences.setPrefNormalAlarmIdId(getApplicationContext(), String.valueOf(alarm_id_int));
                    Log.d("msg", "alarm id: " + alarm_id_int);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),alarm_id_int, Receiverintent, 0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +1*60*1000,1* 60 * 1000, pendingIntent);
                }

                //STOPING TO SEND NOTIFICATIONS ON GUEST SIDE IF CLOSE ENOUGH TO DESTINATION
                // etaInMinutes<1 || statement not for local testing..
                if((distance_in_meters <17) && SharePreferences.getPrefMode(getApplicationContext()).equalsIgnoreCase(AppManager.guestMode)){
                //    Toast.makeText(LocationDisplay.this,"distance in meters: "+distance_in_meters+" Time in mins"+fDuration,Toast.LENGTH_SHORT).show();

                    operation = "guestReached";
                    getAddressFromLocation(lat, lon, LocationDisplay.this, new GeocoderHandler(), "guestReached");

                    cancelAllAlarms();

                }



                if (pdialog.isShowing()) {
                    pdialog.cancel();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String id = c
                                .getString(c
                                        .getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c
                                .getString(c
                                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver()
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                    + " = " + id, null, null);
                            phones.moveToFirst();
                            sender = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            try {

                                 SmsManager smsManager = SmsManager.getDefault();
                                 smsManager.sendTextMessage(sender, null, messageBody, null, null);   //SENDING RESPONSE
                                  Toast.makeText(LocationDisplay.this, "Location Shared Successfully! with"+sender,
                                                Toast.LENGTH_LONG).show();
                                Intent intents = new Intent(LocationDisplay.this,MapsActivity.class);
                                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intents);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
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
        Intent intents = new Intent(LocationDisplay.this,MapsActivity.class);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intents);
        finish();
    }



    private void updateServer(final String thisOperation){
//        operation=thisOperation;
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
                        if(operation.equalsIgnoreCase("stopUpdatingLocation")){
                            finish();
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
                if(extension.equalsIgnoreCase("Request")) {
                    params.put("eta", "Not Departed");
                    params.put("lat", String.valueOf(lat));
                    params.put("lon",String.valueOf(lon));
                    params.put("receiver",sender);
                    params.put("sender",SharePreferences.getPrefUserNumber(getApplicationContext()));
                    params.put("message_type","Response");
                }else  {
                    params.put("eta", etaInMinutes + " min");
                    params.put("lat", String.valueOf(senderLat));
                    params.put("lon",String.valueOf(senderLon));
                    params.put("receiver",SharePreferences.getPrefUserNumber(getApplicationContext()));
                    params.put("sender",SharePreferences.getPrefGuestPhone(getApplicationContext()));
                    params.put("message_type",extension);
                }

                params.put("distance",distance);
                params.put("speed", user_speed);
                params.put("location",addressResult);


                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                formatter.format(calendar.getTimeInMillis());
                params.put("date_time", formatter.format(calendar.getTimeInMillis()));
                params.put("guest_server_id",SharePreferences.getPrefGuestServerId(getApplicationContext()));
                params.put("host_server_id",SharePreferences.getPrefHostServerId(getApplicationContext()));
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

    private void cancelAllAlarms(){
        Context context=getApplicationContext();

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


        //SHOWING MESSAGE REACHED DESTINATION IN NOTIFICATION BOX
        Intent intent1 = new Intent();
        intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("msg", "You have reached Destination");
        startActivity(intent1);

    }

    private class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc)
        {
            if (loc != null)
            {
                // Do something knowing the location changed by the distance you requested
                showCurrentLocation(loc);
            }
        }

        @Override
        public void onProviderDisabled(String arg0)
        {
            // Do something here if you would like to know when the provider is disabled by the user
        }

        @Override
        public void onProviderEnabled(String arg0)
        {
            // Do something here if you would like to know when the provider is enabled by the user
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2)
        {
            // Do something here if you would like to know when the provider status changes
        }
    }

}
