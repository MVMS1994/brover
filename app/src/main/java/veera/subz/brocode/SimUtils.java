package veera.subz.brocode;

/**
 * Created by MVMS1994 on 02/09/16.
 */

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimUtils
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static boolean sendSMS(Context ctx, int simID, String toNum, String centerNum, final String smsText, final PendingIntent sentIntent, final PendingIntent deliveryIntent) {
        String name;
        try {
            if (simID == 0) {
                name = Build.MODEL.equals("Philips T939")? "isms0" : "isms";
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            Log.e("send msg",smsText);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, toNum, centerNum, smsText, sentIntent, deliveryIntent);
            } else {
                if(isDualSim(ctx)) {
                    SmsManager.getSmsManagerForSubscriptionId(simID + 1).sendTextMessage(toNum, null, smsText, sentIntent, deliveryIntent);
                } else {
                    SmsManager.getDefault().sendTextMessage(toNum, null, smsText, sentIntent, deliveryIntent);
                }
            }

            return true;
        } catch (ClassNotFoundException e) {
            Log.e("SimUtl", "ClassNotFoundException:" + e.getMessage());
            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        } catch (NoSuchMethodException e) {
            Log.e("SimUtl", "NoSuchMethodException:" + e.getMessage());
            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        } catch (InvocationTargetException e) {
            Log.e("SimUtl", "InvocationTargetException:" + e.getMessage());
            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IllegalAccessException e) {
            Log.e("SimUtl", "IllegalAccessException:" + e.getMessage());
            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("SimUtl", "Exception:" + e.getMessage());
            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static boolean isDualSim(Context context){
        if(Build.VERSION.SDK_INT>=22){
            return SubscriptionManager.from(context).getActiveSubscriptionInfoCount()>1;
        }else{
            return false;
        }
    }
}
