package veera.subz.brocode;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class BroActivity extends AppCompatActivity {

    Switch enabler;
    IntentFilter intentfilter;
    RadioButton simBtn;
    RadioGroup simGrp;
    SharedPreferences config;
    SharedPreferences.Editor configEditor;
    int defaultSim;
    String mode;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.bro_activity);

        ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
//        try {
            init();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void init() {
        defaultSim = 0;
        mode = "enabled";
        config = getSharedPreferences("config", Context.MODE_PRIVATE);
        simGrp = (RadioGroup) findViewById(R.id.sim_grp);
        enabler = (Switch) findViewById(R.id.enabler);
        String result = config.getString("config", null);

        if(result == null) {
            configEditor = config.edit();
            configEditor.putString("config", mode + ":" + defaultSim);
            configEditor.apply();
        } else {
            mode = result.split(":")[0];
            defaultSim = Integer.parseInt(result.split(":")[1]);
            if(mode.equals("enabled")) {
                enabler.setChecked(true);
                enabler.setText("Disable me, bro??");
                simGrp.setVisibility(View.VISIBLE);
            } else {
                enabler.setChecked(false);
                enabler.setText("Enable me, bro??");
                simGrp.setVisibility(View.GONE);
            }
            if(defaultSim == 0) {
                simGrp.check(R.id.sim_1);
            } else {
                simGrp.check(R.id.sim_2);
            }
        }
        if(!SimUtils.isDualSim(this)) {
            simGrp.setVisibility(View.GONE);
        }

        enabler.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(enabler.isChecked()) {
                    mode = "enabled";
                    configEditor = config.edit();
                    configEditor.putString("config", mode + ":" + defaultSim);
                    configEditor.apply();
                    simGrp.setVisibility(View.VISIBLE);

                    if(!isMyServiceRunning(SmsService.class)) {
                        enabler.setText("Disable me, bro??");
                        Toast.makeText(BroActivity.this, "At your service!",Toast.LENGTH_LONG).show();
                        Intent service = new Intent(BroActivity.this, SmsService.class);
                        BroActivity.this.startService(service);
                    }
                } else {
                    mode = "disabled";
                    configEditor = config.edit();
                    configEditor.putString("config", mode + ":" + defaultSim);
                    configEditor.apply();
                    simGrp.setVisibility(View.GONE);

                    if(isMyServiceRunning(SmsService.class)) {
                        enabler.setText("Enable me, bro??");
                        Toast.makeText(BroActivity.this, "Hoping to serve soon!",Toast.LENGTH_LONG).show();
                        Intent service = new Intent(BroActivity.this, SmsService.class);
                        BroActivity.this.stopService(service);
                    }
                }
            }
        });

        if(SimUtils.isDualSim(this) && mode.equals("enabled")) {
            simGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if(i == R.id.sim_1)
                        defaultSim = 0;
                    else
                        defaultSim = 1;
                    configEditor = config.edit();
                    configEditor.putString("config", mode + ":" + defaultSim);
                    configEditor.apply();
                }
            });
        } else if(SimUtils.isDualSim(this)) {
            simGrp.setEnabled(false);
        } else {
            simGrp.setVisibility(View.GONE);
        }

        if(!isMyServiceRunning(SmsService.class) && config.getString("config", "").split(":")[0].equals("enabled")) {
            Toast.makeText(this, "At your service!", Toast.LENGTH_LONG).show();
            Intent service = new Intent(this, SmsService.class);
            this.startService(service);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}