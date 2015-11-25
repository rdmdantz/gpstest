package meshsol.locationsharing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationTracking extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    static LocationManager locationManager;
    LocationListener locationListener;
    private static final String TAG = "RouteTracking";
    private String url="http://server/index.php";
    String receiver;
    String frequency="";
    String messageBody;
    private boolean messageSentStatus;
    GoogleApiClient mGoogleApiClient;
    @Override
    public void onCreate() {
     //   receiver=intent.getStringExtra("number");

        //   getRequestQueue();
        buildGoogleApiClient();
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
        Log.d("msg","Location not null in onConnected....");
        if (mLastLocation != null) {
          //  mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
           // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    /* public RequestQueue getRequestQueue() {
         if (mRequestQueue == null) {
             mRequestQueue = Volley.newRequestQueue(getApplicationContext());
         }

         return mRequestQueue;
     }
 */


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("msg", "Location  by googleMap.onlocationchangelistener in service");
                    if(messageSentStatus==false) {
                        updateLocationToRequester(location);
                    }

        }
    };

        private void startRouteTracking(){
        Log.d("msg", "service started to track location");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
      //  criteria.setSpeedRequired(true);
        //criteria.setAltitudeRequired(false);
        //criteria.setBearingRequired(false);
        //criteria.setCostAllowed(true);
        //criteria.setPowerRequirement(Criteria.POWER_LOW);
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


            // Define a listener that responds to location updates

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {


        }
            locationManager.requestLocationUpdates(provider,0,0,locationListener);

            //  Location loc=locationManager.getLastKnownLocation(provider);

    }
    public void updateLocationToRequester(Location loc)
    {
        if(loc==null){
            Log.d("msg","Location is null, trying to get location again");
            startRouteTracking();
        }else {
            messageSentStatus = true;
            if(SharePreferences.getPrefPrevLat(getApplicationContext())=="" || SharePreferences.getPrefPrevLon(getApplicationContext())=="" || SharePreferences.getPrefPrevTime(getApplicationContext())==""){
                SharePreferences.setPrevLat(getApplicationContext(), String.valueOf(loc.getLatitude()));
                SharePreferences.setPrevLon(getApplicationContext(), String.valueOf(loc.getLongitude()));
                SharePreferences.setPrevTime(getApplicationContext(),String.valueOf((System.currentTimeMillis() / 1000L)));
            }
            try {
                float user_speed=0.0f;
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
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#UpdatingNormal"+"#"+String.format("%.1f", user_speed)+"#"+receiver;

                } else if (frequency.equalsIgnoreCase("high")) {
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#UpdatingHighFreq"+"#"+String.format("%.1f", user_speed)+"#"+receiver;
                } else if (frequency.equalsIgnoreCase("requestedUpdate")) {
                    messageBody = "Clicke below link to get user location \n http://meshsol.com/LocationSharing#" + loc.getLatitude() + "#" + loc.getLongitude() + "#requestedUpdate"+"#"+String.format("%.1f", user_speed)+"#"+receiver;
                }

                smsManager.sendTextMessage(receiver, null, messageBody, null, null);
                DevicePolicyManager mDPM = (DevicePolicyManager) LocationTracking.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDPM.lockNow();
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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),(int)l,ReceiverNewIntent, 0);
            aManager1.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 1000, 15 * 1000, pendingIntent);
            stopSelf();

        }
        else {
            startRouteTracking();
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d("msg","services onDestroy");
            locationManager.removeUpdates(locationListener);
        }
        //locationManager=null;
        //locationListener=null;


        //Intent intent = new Intent(this, LocationTracking.class);
        //startService(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class MyThread implements Runnable	{

        @Override
        public void run() {
          //  startRouteTracking();

        }

    }

    private void updateServer(){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null,  new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.e("Response => ",response.toString());
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        })
        { //Code to send parameters to server
            @Override
            protected Map getParams() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("name","YOUR NAME VALUE");
                params.put("description", "YOUR description");
                params.put("price", "YOUR price");
                return params;
            }
        };
        queue.add(jsonObjReq);
      }

}
