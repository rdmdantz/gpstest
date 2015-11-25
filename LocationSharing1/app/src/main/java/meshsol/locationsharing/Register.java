package meshsol.locationsharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wasiq Billah on 11/18/2015.
 */
public class Register extends Activity {
    EditText etContact;
    Button btnSubmit;
    ProgressDialog progressDialog;
    GoogleCloudMessaging gcm;

    String phoneNumber;
    String regId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("msg", "Register");
      /*  if(SharePreferences.getPrefUserServerId(getApplicationContext())!=""){
            Intent intent=new Intent(Register.this,MapsActivity.class);
            startActivity(intent);
            finish();
        }*/
        setContentView(R.layout.register);
        etContact=(EditText)findViewById(R.id.etPhone);
        btnSubmit=(Button)findViewById(R.id.btnRegister);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnectingToInternet()) {

                    // Internet Connection is not present
                    showAlertDialog(Register.this, "Internet Connection Error",
                            "Please connect to working Internet connection");
                    return;

                }
                 if (AppManager.project_id == null || AppManager.project_id.length() == 0) {

                        // GCM sernder id / server url is missing
                        showAlertDialog(Register.this, "configuration Error!",
                                "Please set your Server URL and GCM Sender ID");

                        // stop executing code by return
                        return;
                 }

                phoneNumber=etContact.getText().toString();
                if (phoneNumber.length()>0) {
                            //if (phoneNumber.matches(phoneNumberPattern)) {
                              //  operation="signup";
                               // RegGcm();
                            //} else {
                             //   etemail.setError("Provide valid Email");

                           // }
                            RegGcm();
                        }
                        else
                        {
                               etContact.setError("Provide valid Phone");
                        }

                    }
        });


    }

    public void RegGcm() {
        new AsyncTask<Void, Void, String>() {
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
                progressDialog = new ProgressDialog(Register.this);
                progressDialog.setMessage("Processing");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();

            }

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging
                                .getInstance(getApplicationContext());
                    }
                    regId = gcm.register(AppManager.project_id);
                    msg = "Device registered, registration ID=" + regId;


                } catch (IOException ex) {
                    // Toast.makeText(customer.this, "unable",
                    // Toast.LENGTH_LONG).show();
                    msg = "Error :" + ex.getMessage();

                }
                Log.i("GCM", msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                if (isConnectingToInternet()) {

                   RegisterUser();

                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            Register.this);
                    alertDialogBuilder.setTitle("Exiting Application");
                    alertDialogBuilder
                            .setMessage(
                                    "Internet Connection is required! Please make sure you are connected to internet and then start application.")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            android.os.Process
                                                    .killProcess(android.os.Process
                                                            .myPid());
                                            System.exit(0);
                                        }
                                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        }.execute(null, null, null);
    }

    private void RegisterUser(){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,AppManager.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }

                        Log.d("response msg",response);

                        Intent intent=new Intent(Register.this,MapsActivity.class);
                        startActivity(intent);
                        finish();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                // Error handling
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                Log.e("error msg", error.toString() + "");
                Log.d("res msg",response.data.toString()+"");

            }
        })
        {
           /* @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    // Check if it is JSONObject or JSONArray
                    Object json = new JSONTokener(jsonString).nextValue();
                    JSONObject jsonObject = new JSONObject();
                    if (json instanceof JSONObject) {
                        jsonObject = (JSONObject) json;
                    } else if (json instanceof JSONArray) {

                        jsonObject.put("success", json);
                    } else {
                        String message = "{\"error\":\"Unknown Error\",\"code\":\"failed\"}";
                        jsonObject = new JSONObject(message);
                    }
                    return Response.success(jsonObject,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
           */ //Code to send parameters to server
            @Override
            protected Map getParams() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("operation","register");
                params.put("userPhone", phoneNumber);
                params.put("regId",regId);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                3,
                1));
        queue.add(stringRequest);
    }


    public void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Set Dialog Title
        alertDialog.setTitle(title);

        // Set Dialog Message
        alertDialog.setMessage(message);

        // Set OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // Show Alert Message
        alertDialog.show();
    }


    // Checking for all possible internet providers
    public boolean isConnectingToInternet() {

        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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


}
