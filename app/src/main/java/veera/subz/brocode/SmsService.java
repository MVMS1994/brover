package veera.subz.brocode;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by MVMS1994 on 02/09/16.
 */
public class SmsService extends Service {

    SMSreceiver mSmsReceiver;
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("setting", ":setted");
        mSmsReceiver = new SMSreceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.registerReceiver(mSmsReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(mSmsReceiver != null) {
            this.unregisterReceiver(mSmsReceiver);
        }
        super.onDestroy();
    }

    public class SMSreceiver extends BroadcastReceiver {
        public SMSreceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                tryReceiveMessage(intent);
            }
        }
    }

    private void tryReceiveMessage(Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        // String msgFrom;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                // msgFrom = msgs[i].getOriginatingAddress();
                String msgBody = msgs[i].getMessageBody();
                if(checkMsg(msgBody)) {
                    sendMsg(msgBody);
                }
            }
        }
    }

    private void sendMsg(String msgBody) {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        String result = config.getString("config", ":0");
        try {
            int sim = Integer.parseInt(result.split(":")[1]);
            String msg = msgBody.substring(msgBody.indexOf('"')+1, msgBody.lastIndexOf('"'));
            String number = msgBody.substring(msgBody.lastIndexOf(' ')+1);
            SimUtils.sendSMS(this, sim, number, null, msg, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkMsg(String msgBody) {
        return msgBody.matches("Hey Bro! send \".*\" to \\d+");
    }
}