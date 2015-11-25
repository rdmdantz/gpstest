package meshsol.locationsharing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by Wasiq Billah on 10/30/2015.
 */
public class LinkBroadCastReceiver extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent().getData();
        String tempStr = data.toString();
        String[] separated = tempStr.split("#");
        if(separated.length<=2){
            finish();
        }else {
            Intent intent1 = new Intent();
            intent1.setClassName("meshsol.locationsharing", "meshsol.locationsharing.LocationDisplay");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("lat", separated[1]);
            intent1.putExtra("lon", separated[2]);
            intent1.putExtra("extension", separated[3]);
            if (separated.length >= 5 && separated[3].equalsIgnoreCase("Response"))  {
               // intent1.putExtra("speed", separated[4]);
                intent1.putExtra("senderByLink",separated[4]);
            }else{
                intent1.putExtra("speed", separated[4]);
                intent1.putExtra("senderByLink", separated[5]);
            }
            intent1.putExtra("sender", "link");
            startActivity(intent1);
            finish();
        }
    }
}
