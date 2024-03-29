package meshsol.locationsharing;

/**
 * Created by Wasiq Billah on 11/2/2015.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by Wasiq Billah on 10/30/2015.
 */
public class CustomLocationDisplay extends FragmentActivity {

    private TextView tvMessage;
    private Button btnUpdateLocation;
    private Button btnStopUpdating;
    private Button btnGetCustomLocation;
    private EditText etLocation;
    ProgressDialog pdialog;

    double senderLat=0.0f;
    double senderLon=0.0f;
    GoogleMap mMap;
    private boolean currentNotShown;
    private boolean locationEntered;
    String destination;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        locationEntered=false;
        setContentView(R.layout.activity_maps);
        btnStopUpdating=(Button)findViewById(R.id.btnStopNotification);
        btnUpdateLocation=(Button)findViewById(R.id.btnUpdateLocation);
        btnUpdateLocation.setVisibility(View.GONE);
        btnStopUpdating.setVisibility(View.GONE);
        tvMessage = (TextView) findViewById(R.id.tvmsg);
        btnGetCustomLocation=(Button)findViewById(R.id.btnGetCustomLocation);
        etLocation=(EditText)findViewById(R.id.etSearchLocation);
        currentNotShown = true;
        destination="Destination";

        pdialog = new ProgressDialog(this);
        pdialog.setTitle("Progress...");
        pdialog.setMessage("Searching Location...");
        pdialog.setCancelable(false);
        pdialog.show();



        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (pdialog.isShowing()) {
                    pdialog.dismiss();
                }
                showRefreshDialog();

            }
        }, 15000);

        btnGetCustomLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String locationString = etLocation.getText().toString();
                    String[] temp=locationString.split(" ");
                    destination=temp[0];
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocationName(locationString, 1);
                    Address address = addresses.get(0);

                    senderLat = address.getLatitude();
                    senderLon = address.getLongitude();
                }catch (Exception e){
                    Log.d("msg","gecoding exception: ");
                    e.printStackTrace();
                }
                if(senderLat!=0.0 && senderLon!=0.0){
                    locationEntered=true;
                    pdialog.show();
                    Log.d("msg","gecoding Lat: "+senderLat+" Long: "+senderLon);
                    setUpMapIfNeeded();
                }else{
                    Toast.makeText(getApplicationContext(),"Location Not Found",Toast.LENGTH_SHORT).show();
                }
            }
        });
        setUpMapIfNeeded();
    }


    private void showRefreshDialog(){
        if(currentNotShown==true) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Oooppps...")
                    .setMessage("Its taking too long to get current location...")
                    .setCancelable(false)
                    .setPositiveButton("Retry",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                    Intent refresh = new Intent(CustomLocationDisplay.this, CustomLocationDisplay.class);
                                    startActivity(refresh);
                                    finish();
                                }
                            });
            alertDialogBuilder.setNegativeButton("Exit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }else{

        }
    }


    private void setUpMapIfNeeded() {

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
            if(mMap != null && currentNotShown){
                showCurrentLocation(location);
            }else{
                setUpMapIfNeeded();
            }
        }
    };
    private void setUpMap() {
        Log.d("msg","setupMAP");
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
                if(currentNotShown) {
                    showCurrentLocation(loc);

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


        locationManager.requestLocationUpdates(provider,1,1, locationListener);
        // Getting initial Location
        Location      location = locationManager.getLastKnownLocation(provider);


    }

    private void showCurrentLocation(Location loc){
        Log.d("msg", "current location");
        mMap.clear();
        LatLng currentPosition = new LatLng(loc.getLatitude(),loc.getLongitude());
        BitmapDescriptor bitmapMarker1 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .flat(true)
                .title("I'm here!")
                .icon(bitmapMarker1)).showInfoWindow();

        if(locationEntered==true) {


            LatLng senderPosition=new LatLng(senderLat,senderLon);
            BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            mMap.addMarker(new MarkerOptions()
                    .position(senderPosition)
                    .flat(true)
                    .title(destination)
                    .icon(bitmapMarker)).showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 10));
            String url = getDirectionsUrl(currentPosition, senderPosition);
            Log.d("url",url);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 3));
            currentNotShown=false;
            if(pdialog.isShowing()){
                pdialog.dismiss();
            }
        }
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
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
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
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }


            tvMessage.setText("Location Details: \nDistance: " +distance+ "\n ETA: " + duration);
            currentNotShown=false;
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
       finish();
    }
}
