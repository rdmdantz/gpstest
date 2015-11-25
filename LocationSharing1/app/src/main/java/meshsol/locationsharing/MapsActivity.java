package meshsol.locationsharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button btnSender;
    private Button btnMintPlaza;
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
    private double lat=33.7472;
    private double lon=73.1389;
    //instance variables for Marker icon drawable resources
    private int userIcon;
    float user_speed=0.0f;
    //the map
    private GoogleMap theMap;

    //location manager
    private LocationManager locMan;

    //user marker
    private Marker userMarker;

    static final int PICK_CONTACT= 0;
    ProgressDialog pdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSender=(Button)findViewById(R.id.btnSender);
        btnMintPlaza=(Button)findViewById(R.id.btnMintPlaza);
        currentNotShown=true;

        operation="requestingPeoples";
        setUpMapIfNeeded();
        //get drawable IDs
        userIcon = R.drawable.green_point;
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        pdialog=new ProgressDialog(this);
        pdialog.setTitle("Progress...");
        pdialog.setMessage("Getting Current Location");
        pdialog.setCancelable(false);
        pdialog.show();
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
                    startActivityForResult(intent, PICK_CONTACT);
                } catch(Exception e){
                        e.printStackTrace();
                    }

            }
        });

        btnMintPlaza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(MapsActivity.this,MintPlazaEtaDisplay.class);
                intent1.putExtra("lat",lat+"");
                intent1.putExtra("lon", lon + "");
                startActivity(intent1);
            }
        });

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
                            String number = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            //calculate speed and send along with message..

                                SharePreferences.setInitLat(getApplicationContext(),String.valueOf(lat));
                                SharePreferences.setInitLon(getApplicationContext(), String.valueOf(lon));
                                SharePreferences.setPrefSession(getApplicationContext(),"active");
                                String message="Clicke below link to get user location \n http://meshsol.com/LocationSharing#"+lat+"#"+lon+"#Request#"+String.format("%.1f", user_speed)+"#"+number;
                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(number, null,message, null, null);
                                Toast.makeText(this, "Location Shared Successfully!",
                                        Toast.LENGTH_LONG).show();
                                DevicePolicyManager mDPM = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                mDPM.lockNow();
                                //finish();



                            } catch (Exception e) {
                                //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
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

        locationManager.requestLocationUpdates(provider,0,0, locationListener);

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
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){

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
}
