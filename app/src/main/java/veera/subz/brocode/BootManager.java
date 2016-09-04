package veera.subz.brocode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by MVMS1994 on 02/09/16.
 */
public class BootManager extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            this.context = context;
            Toast.makeText(context, "helloooooo", Toast.LENGTH_LONG).show();
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent service = new Intent(context, SmsService.class);
                context.startService(service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}