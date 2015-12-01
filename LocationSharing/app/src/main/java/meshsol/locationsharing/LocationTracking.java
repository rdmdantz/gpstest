package meshsol.locationsharing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LocationTracking extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    static LocationManager locationManager;
    LocationListener locationListener;
    private static final String TAG = "RouteTracking";

    String receiver;
    String frequency="";
    String messageBody;
    private boolean messageSentStatus;
    GoogleApiClient mGoogleApiClient;
    String operation;
    float user_speed;
    Location location;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;

    @Override
    public void onCreate() {

        //   getRequestQueue();
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public Location getLocation() {

       if (mCurrentLocation != null) {
           Log.d("msg","returning mLast Location from get location");
            return mCurrentLocation;
        } else {

             LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                }
                Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocationGPS != null) {
                    Log.d("msg","returning gps Location from get location");
                    return lastKnownLocationGPS;
                }
                Location lastKnownLocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(lastKnownLocationNetwork!=null) {
                    Log.d("msg","returning network Location from get location");
                    return lastKnownLocationNetwork;
                }
                return null;
            } else {
                return null;
            }
        }
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
          //  mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
           // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mCurrentLocation=mLastLocation;
            Log.d("msg","Location not null in onConnected....");
            Log.d("msg","Lat: "+mLastLocation.getLatitude()+"  Lon"+mLastLocation.getLongitude());
        }else{
            Log.d("msg","mLastLocation null by api client");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private void TrackLocation(){
        Log.d("msg", "service started to track location");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        //Acquire a reference to the system Location Manager
        String provider = locationManager.getBestProvider(criteria, true);
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location)

                {
                    if(messageSentStatus==false) {
                        updateLocationToRequester(location);
                    }

                }
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}

            };


        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {


        }

            locationManager.requestLocationUpdates(provider,1,1,locationListener);
    }


    public void updateLocationToRequester(Location loc)
    {

        if(loc==null){
            Log.d("msg","Location is null, trying to get location again");
          //  TrackLocation();
        }else {
            if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                locationManager.removeUpdates(locationListener);
            }

            messageSentStatus = true;
            location=loc;
            if(SharePreferences.getPrefPrevLat(getApplicationContext())=="" || SharePreferences.getPrefPrevLon(getApplicationContext())=="" || SharePreferences.getPrefPrevTime(getApplicationContext())==""){
                SharePreferences.setPrevLat(getApplicationContext(), String.valueOf(loc.getLatitude()));
                SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
                SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));
            }
            try {
                 user_speed=0.0f;
                long cur_time = System.currentTimeMillis() / 1000L;
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
                    //setting values to pref if not set yet....

                    float[] result = new float[1];
                    loc.distanceBetween(Double.valueOf(SharePreferences.getPrefPrevLat(getApplicationContext())), Double.valueOf(SharePreferences.getPrefPrevLon(getApplicationContext())),
                            loc.getLatitude(), loc.getLongitude(),
                            result);
                    float loc_distance = result[0];
                    //speed if location.getSpeed is null
                    user_speed = loc_distance / (cur_time - Long.valueOf(SharePreferences.getPrefPrevTime(getApplicationContext())));

                }
                // converting to km/hr
                if(Float.isNaN(user_speed)){
                    user_speed=0.0f;
                }

                user_speed=(((user_speed*18)/5)*0.621f);

                //Updating previous values
                SharePreferences.setPrevLat(getApplicationContext(),String.valueOf(loc.getLatitude()));
                SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
                SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));






                SmsManager smsManager = SmsManager.getDefault();
                if (frequency.equalsIgnoreCase("normal")) {
                    operation="UpdatingNormal";
                    updateServer();
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#UpdatingNormal"+"#"+String.format("%.1f", user_speed)+"#"+receiver;

                } else if (frequency.equalsIgnoreCase("high")) {
                    operation="UpdatingHighFreq";
                    updateServer();
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#UpdatingHighFreq"+"#"+String.format("%.1f", user_speed)+"#"+receiver;
                } else if (frequency.equalsIgnoreCase("requestedUpdate")) {
                    operation="requestedUpdate";
                    updateServer();
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#requestedUpdate"+"#"+String.format("%.1f", user_speed)+"#"+receiver;
                }

                //smsManager.sendTextMessage(receiver, null, messageBody, null, null);
             //   DevicePolicyManager mDPM = (DevicePolicyManager) LocationTracking.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
              //  mDPM.lockNow();
            } catch (Exception e) {
                //Toast.makeText(this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            Log.d("msg", "service sending message "+messageBody);
            stopSelf();
        }
    }


    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        messageSentStatus=false;
        receiver=intent.getStringExtra("number");
        frequency=intent.getStringExtra("frequency");
        String specialMessage=intent.getStringExtra("specialMesssage");
        String address=intent.getStringExtra("address");
        String alarm_id=intent.getStringExtra("alarmId");
        if(specialMessage!=null && specialMessage.equalsIgnoreCase("startHighFreqAlarm")){
            long l=Long.parseLong(alarm_id);
            AlarmManager aManager1 = (AlarmManager)getSystemService(ALARM_SERVICE);
            Intent ReceiverNewIntent = new Intent(getApplicationContext(), HighFreqAlarm.class);
            ReceiverNewIntent.putExtra("sender", address);
            ReceiverNewIntent.putExtra("frequency", "high");
            int alarm_id_int=(int)l;
            SharePreferences.setPrefHighfrequencyAlarmId(getApplicationContext(),String.valueOf(alarm_id_int));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),alarm_id_int,ReceiverNewIntent, 0);
            aManager1.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 1000, 15 * 1000, pendingIntent);
            stopSelf();

        }
        else {
            mGoogleApiClient.connect();
            TrackLocation();
            Location l=getLocation();
            if(l!=null) {
                updateLocationToRequester(l);
            }else{
                TrackLocation();
            }

        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d("msg","services onDestroy");
        //    locationManager.removeUpdates(locationListener);
        }
        //locationManager=null;
        //locationListener=null;


        //Intent intent = new Intent(this, LocationTracking.class);
        //startService(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



    public  void updateServer(){
        Log.d("msg", "updating server  for operation from service Location Tracking :"+operation);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,AppManager.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        Log.d("response msg", response);
                        if(response.equalsIgnoreCase("false")){
                           //ShowToast("Notification not sent..");
                         }else{
                         //   ShowToast("Host notified successfully");
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
                params.put("operation",operation);
                params.put("speed",String.valueOf(user_speed));
                params.put("lat", String.valueOf(location.getLatitude()));
                params.put("lon",String.valueOf(location.getLongitude()));
                Log.d("msg "," service lat : "+location.getLatitude() +"  lon: "+location.getLongitude());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                formatter.format(calendar.getTimeInMillis());
                params.put("date_time", formatter.format(calendar.getTimeInMillis()));
                params.put("receiver",receiver);
                params.put("sender",SharePreferences.getPrefUserNumber(getApplicationContext()));
                params.put("host_server_id",SharePreferences.getPrefHostServerId(getApplicationContext()));
                Log.d("msg","sending params from service: "+params.toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                3,
                1));
        queue.add(stringRequest);
    }

    /**
     * Function using Handler to show Toast message
     */
    public void ShowToast(final String msg)
    {  final Context MyContext = getApplicationContext();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                // Create layout inflator object to inflate toast.xml file
                LayoutInflater inflater = (LayoutInflater) MyContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // Call toast.xml file for toast layout
                View toastRoot = inflater.inflate(R.layout.toast, null);
                TextView tvMessage = (TextView) toastRoot.findViewById(R.id.tvToastMessages);
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
