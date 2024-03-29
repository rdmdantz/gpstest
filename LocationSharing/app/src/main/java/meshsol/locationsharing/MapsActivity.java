package meshsol.locationsharing;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button btnSender;
    private Button btnMintPlaza;
    private ImageButton btMenuLocationDisplay;
    private double selectedLat;
    private double selectedLon;
    String operation;
    private boolean dialogFlag;
    String placesSearchStr;
    private int PROXIMITY_RADIUS = 500000;
    String selectedType="";
    ProgressDialog progressDialog;
    private Location userLastLocation;
    private boolean currentNotShown;
    String receiverNumber;
    private double lat=33.7472;
    private double lon=73.1389;
    String addressResult;
    //instance variables for Marker icon drawable resources
    private int userIcon;
    float user_speed=0.0f;
    //the map
    private GoogleMap theMap;
    String session_id;
    //location manager
    private LocationManager locMan;

    //user marker
    private Marker userMarker;

    static final int PICK_CONTACT= 0;
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;

    ProgressDialog pdialog;
    private boolean isSafeway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSender=(Button)findViewById(R.id.btnSender);
        btnMintPlaza=(Button)findViewById(R.id.btnMintPlaza);
        /*btMenuLocationDisplay=(ImageButton)findViewById(R.id.btMenuLocationDisplay);*/

        if(!SharePreferences.getPrefSession(getApplicationContext()).equalsIgnoreCase("active")){

        }
/*        btMenuLocationDisplay.setVisibility(View.GONE);*/
        currentNotShown=true;
        isSafeway=false;
        operation="request";
        setUpMapIfNeeded();

        SharePreferences.setPrefRequestedToChangeUpdateFrequency(getApplicationContext(), "false");
        //get drawable IDs
        userIcon = R.drawable.green_point;
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        pdialog=new ProgressDialog(this);
        pdialog.setTitle("Progress...");
        pdialog.setMessage("Getting Current Location");
        pdialog.setCancelable(false);
        pdialog.show();

        if(android.os.Build.VERSION.SDK_INT>=23) {
            testPermission();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(pdialog.isShowing()) {
                    pdialog.dismiss();
                }
                    showRefreshDialog();

            }
        }, 15000);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            //  ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
            //        LOCATION_SERVICE.MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }


        btnSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    isSafeway=false;
                    startActivityForResult(intent, PICK_CONTACT);
                } catch(Exception e){
                        e.printStackTrace();
                    }

            }
        });

        btnMintPlaza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent1=new Intent(MapsActivity.this,CustomLocationDisplay.class);
                intent1.putExtra("lat",lat+"");
                intent1.putExtra("lon", lon + "");
                startActivity(intent1);
                */
                //receiverNumber="03459236850"; //For Local Testing
                //receiverNumber="4082029450";  //For Client Testing
               /* receiverNumber="6026928808";  //For Client Testing
                SharePreferences.setInitLat(getApplicationContext(), String.valueOf(lat));
                SharePreferences.setInitLon(getApplicationContext(), String.valueOf(lon));
                SharePreferences.setPrefSession(getApplicationContext(), "active");
                String message="Clicke below link to get user location\n http://meshsol.com/LocationSharing#"+lat+"#"+lon+"#Request#"+String.format("%.1f", user_speed)+"#"+SharePreferences.getPrefUserServerId(getApplicationContext())+"#safeway";

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(receiverNumber, null, message, null, null);  //SENDING SMS HERE
                    Toast.makeText(MapsActivity.this, "Location Sharing Request Sent Successfully!",Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                getAddressFromLocation(lat, lon, MapsActivity.this, new GeocoderHandler());
*/
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    isSafeway=true;
                    startActivityForResult(intent, PICK_CONTACT);
                } catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

    }




    public void testPermission() {

            if (android.os.Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }

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
                                    Intent refresh = new Intent(MapsActivity.this, MapsActivity.class);
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
                             receiverNumber = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            //calculate speed and send along with message..

                                SharePreferences.setInitLat(getApplicationContext(), String.valueOf(lat));
                                SharePreferences.setInitLon(getApplicationContext(), String.valueOf(lon));
                            //    SharePreferences.setPrefSession(getApplicationContext(), "active");
                                String message="";
                                Random r = new Random();
                                session_id  = String.valueOf(r.nextInt(6 - 1) + 1);
                                SharePreferences.setPrefSessionid(getApplicationContext(),session_id);
                                if(isSafeway){
                                    message="Clicke below link to get user location\n http://meshsol.com/LocationSharing#"+lat+"#"+lon+"#Request#"+String.format("%.1f", user_speed)+"#"+SharePreferences.getPrefUserServerId(getApplicationContext())+"#safeway#"+session_id;
                                }
                                else {
                                    message = "Clicke below link to get user location\n http://meshsol.com/LocationSharing#" + lat + "#" + lon + "#Request#" + String.format("%.1f", user_speed) + "#" + SharePreferences.getPrefUserServerId(getApplicationContext())+ "#" +session_id;
                                }

                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                Log.d("msg","sending request: "+message+" to "+receiverNumber);
                                smsManager.sendTextMessage(receiverNumber, null,message, null, null);  //SENDING SMS HERE
                                Toast.makeText(this, "Location Sharing Request Sent Successfully!",
                                        Toast.LENGTH_LONG).show();
                               //DevicePolicyManager mDPM = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                //mDPM.lockNow();

                            } catch (Exception e) {
                                //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            operation="request";
                            getAddressFromLocation(lat, lon, MapsActivity.this, new GeocoderHandler());
                        }
                    }
                }
                     break;
                    case (ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE):
                        if (android.os.Build.VERSION.SDK_INT>=23 && Settings.canDrawOverlays(this)) {
                            // You have permission
                        }
                    break;
                }
        }


     @Override
    public void onResume() {
        super.onResume();
        currentNotShown=true;
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
                   // Log.d("msg","Map ready");
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
            if(mMap != null){
                showCurrentLocation(location);
            }else{
                setUpMapIfNeeded();
            }
        }
    };
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.clear();
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
      //  LatLng currentPosition = new LatLng(33.7472,73.1389);
        //userLastLocation=new LatLng(33.7472,73.1389);
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,15));
        //showCurrentLocation(loc);;
        if(mMap==null){
            setUpMapIfNeeded();
        }
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

/*
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
           // Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }
*/
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        String provider = locationManager.getBestProvider(criteria, true);
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {


               // if(currentNotShown) {
                    showCurrentLocation(loc);
                    lat=loc.getLatitude();
                    lon=loc.getLongitude();
                    currentNotShown=false;
                //}
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

        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        // Getting initial Location
        Location      location = locationManager.getLastKnownLocation(provider);


    }

    private void showCurrentLocation(Location loc) {
        currentNotShown=false;
        float accuracy=loc.getAccuracy();
        long cur_time = System.currentTimeMillis() / 1000L;
        if(SharePreferences.getPrefPrevLat(getApplicationContext())=="" || SharePreferences.getPrefPrevLon(getApplicationContext())=="" || SharePreferences.getPrefPrevTime(getApplicationContext())==""){
            SharePreferences.setPrevLat(getApplicationContext(), String.valueOf(loc.getLatitude()));
            SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
            SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));
        }
        if (loc.hasSpeed()) {
            user_speed = loc.getSpeed();
            if(user_speed==0.0f){
                float[] result = new float[1];
                loc.distanceBetween(Double.valueOf(SharePreferences.getPrefPrevLat(getApplicationContext())),Double.valueOf(SharePreferences.getPrefPrevLon(getApplicationContext())),
                        loc.getLatitude(),loc.getLongitude(),
                        result);
                float loc_distance = result[0];
                //speed if location.getSpeed is null
                user_speed = loc_distance / (cur_time - Long.valueOf(SharePreferences.getPrefPrevTime(getApplicationContext())));
            }
        } else {
            float[] result = new float[1];
            loc.distanceBetween(Double.valueOf(SharePreferences.getPrefPrevLat(getApplicationContext())),Double.valueOf(SharePreferences.getPrefPrevLon(getApplicationContext())),
                    loc.getLatitude(),loc.getLongitude(),
                    result);
            float loc_distance = result[0];
            //speed if location.getSpeed is null
            user_speed = loc_distance / (cur_time - Long.valueOf(SharePreferences.getPrefPrevTime(getApplicationContext())));
        }
        if(Float.isNaN(user_speed)){
            user_speed=0.0f;
        }
        // converting to km/hr
        user_speed=((user_speed*18)/5)*0.621f;
        SharePreferences.setPrevLat(getApplicationContext(),String.valueOf(loc.getLatitude()));
        SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
        SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));
            mMap.clear();
        LatLng currentPosition = new LatLng(loc.getLatitude(),loc.getLongitude());
        BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        lat=loc.getLatitude();
        lon=loc.getLongitude();
        mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .flat(true)
                .title("I'm here!").icon(bitmapMarker));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,8));
    if(pdialog.isShowing()) {
        pdialog.cancel();
    }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

            MenuItem menuItemSession;
            MenuItem menuItemForceStop;
            getMenuInflater().inflate(R.menu.main_menu, menu);
            menuItemSession = menu.findItem(R.id.action_session);
            menuItemForceStop = menu.findItem(R.id.action_forceStop);

            if(SharePreferences.getPrefMode(getApplicationContext()).equalsIgnoreCase(AppManager.guestMode)){
                menuItemSession.setVisible(false);
            }else if(SharePreferences.getPrefMode(getApplicationContext()).equalsIgnoreCase(AppManager.hostMode)){
                menuItemForceStop.setVisible(false);
            }else{
                menuItemSession.setVisible(false);
                menuItemForceStop.setVisible(false);
            }
        this.invalidateOptionsMenu();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_session:
                // Move to LocationDisplay
                Intent intent=new Intent(MapsActivity.this,LocationDisplay.class);
                intent.putExtra("caller","home");
                startActivity(intent);
                return true;
            case R.id.action_forceStop:
                // Stop all alarm
                cancelAllAlarms();
                try {
                    operation="stopUpdatingLocation1";
                    getAddressFromLocation(lat, lon, MapsActivity.this, new GeocoderHandler());
                    SharePreferences.setPrefMode(getApplicationContext(), "");
                    SharePreferences.setPrefSession(getApplicationContext(), "");
                    this.setUpMapIfNeeded();
                  } catch (Exception e) {
                    Log.e("msg", e.getMessage().toString());
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
        android.os.Process
                .killProcess(android.os.Process
                        .myPid());
        System.exit(0);
       finish();
    }

    private void sendRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,AppManager.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("request msg", response);
                        if(response.equalsIgnoreCase("false")){
                            Toast.makeText(getApplicationContext(),"Request Failed..",Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                Log.e("request error msg",error.toString());

            }
        })
        {
            //Code to send parameters to server
            @Override
            protected Map getParams() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("operation",operation);
                Log.d("msg","updating server for operation "+operation);
                params.put("message_type","request");
                params.put("lat",String.valueOf(lat));
                params.put("lon",String.valueOf(lon));
                params.put("speed", String.format("%.1f", user_speed));
                params.put("receiver", SharePreferences.getPrefHostPhone(getApplicationContext()));
                params.put("sender", SharePreferences.getPrefUserNumber(getApplicationContext()));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                formatter.format(calendar.getTimeInMillis());
                params.put("date_time", formatter.format(calendar.getTimeInMillis()));
                params.put("location",addressResult);
                params.put("host_server_id",SharePreferences.getPrefHostServerId(getApplicationContext()));
                params.put("session_id",SharePreferences.getPrefSessionid(getApplicationContext()));
                Log.d("msg params",params.toString());
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
            sendRequest();
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

       // SharePreferences.setPrefMode(getApplicationContext(), "");
      //  this.invalidateOptionsMenu();

        Toast.makeText(getApplicationContext(),"Forced Stop to send Updates",Toast.LENGTH_LONG).show();
       /* //SHOWING MESSAGE REACHED DESTINATION IN NOTIFICATION BOX.
        Intent intent1 = new Intent();
        intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.DialogActivity");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("msg", "You have reached Destination");
        startActivity(intent1);
*/
    }



}
