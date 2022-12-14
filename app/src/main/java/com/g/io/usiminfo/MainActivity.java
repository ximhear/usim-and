package com.g.io.usiminfo;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.g.io.usiminfo.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    loadUsimInfo();
                } else {
                    loadPhoneNumber();
                }
            }
        });
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestPermissions(new String[]{READ_SMS, READ_PHONE_STATE}, 100);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestPermissions(new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE}, 100);
        }
    }

    @SuppressLint("HardwareIds")
    public void loadPhoneNumber() {
        Log.d("usim", "loadPhoneNumber");
        TelephonyManager oTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String number = oTelephonyManager.getLine1Number();
        Log.d("usim", number);
    }

    // https://android.googlesource.com/platform/packages/providers/TelephonyProvider/+/master/assets/latest_carrier_id/carrier_list.textpb
    /*
    carrier_id {
  canonical_id: 1890
  carrier_name: "KT"
  carrier_attribute {
    mccmnc_tuple: "45002"
    mccmnc_tuple: "45004"
    mccmnc_tuple: "45008"
  }
}
carrier_id {
  canonical_id: 1891
  carrier_name: "SK Telecom"
  carrier_attribute {
    mccmnc_tuple: "45005"
  }
}
carrier_id {
  canonical_id: 1892
  carrier_name: "LG U+"
  carrier_attribute {
    mccmnc_tuple: "45006"
    mccmnc_tuple: "450006"
  }
}
     */

    //TODO: TelephonyManager ?????? ??????(X)?????? ???????????? ???????????????? SubscriptionManager ??? default data sub id??? ???????????? ?????? ????????? 24????????? ???????????? ????????? ?????? ????????? sub id??? ?????????????
    //TODO: ???, X <= Version < 24 ?????? ????????? default data sub id??? ?????????????
    //TODO: esim??? ???????????? ?????? z fold 4???????????? ?????? api ???????????? ???????????? ?????? ????????? ???????????? ??? ??? ??????.
    // https://stackoverflow.com/questions/39823886/check-mobile-data-is-on-from-sim-one-or-sim-two-android
    //

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void loadUsimInfo() {
        Log.d("usim", "loadUsimInfo");
        TelephonyManager oTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            SubscriptionManager subscriptionManager = (SubscriptionManager) this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d("usim", "not permitted");
                return;
            }
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int subId = SubscriptionManager.getDefaultSubscriptionId();
            Log.d("usim", "default subid : " + subId);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int dataSubscriptionId = 0;
            dataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
            // INVALID_SUBSCRIPTION_ID
            Log.d("usim", "dataSubscriptionId : " + dataSubscriptionId);
        }
        int dataSimId = MainActivity.getDataSimId(this);
        Log.d("usim", "dataSimId : " + dataSimId);

        if (subscriptionInfoList != null && subscriptionInfoList.size() > 0) {
                for (int i = 0; i < subscriptionInfoList.size(); i++) {
                    SubscriptionInfo info = subscriptionInfoList.get(i);
                    String carrierName = info.getCarrierName().toString();
                    int subscriptionId = info.getSubscriptionId();
                    Log.d("usim", "==========");
                    Log.d("usim", info.getCarrierName().toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.d("usim", info.getMccString());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.d("usim", info.getMncString());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.d("usim", "carrier id : " + info.getCarrierId());
                    }
                    Log.d("usim", "subscriptionId : " + info.getSubscriptionId());
                    Log.d("usim", "number : " + info.getNumber());
                    Log.d("usim", info.toString());
                }
            }
    }

    public static int getDataSimId(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                    return dataSubId;
                } else {
                    return getDataSimIdBeforeN(context);
                }
            }
        }
        return -1;
    }

    public static String getDataSimOperator(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                    TelephonyManager dataSimManager = tm.createForSubscriptionId(dataSubId);
                    return dataSimManager.getSimOperator();
                } else {
                    String operator = getDataSimOperatorBeforeN(context);
                    if (operator != null) {
                        return operator;
                    } else {
                        return tm.getSimOperator();
                    }
                }
            } else {
                return tm.getSimOperator();
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private static int getDataSimIdBeforeN(Context context) {

        int dataSubId = -1;
        try {
            Method getDefaultDataSubId = SubscriptionManager.class.getDeclaredMethod("getDefaultDataSubId");
            if (getDefaultDataSubId != null) {
                getDefaultDataSubId.setAccessible(true);
                dataSubId = (int) getDefaultDataSubId.invoke(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSubId;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private static String getDataSimOperatorBeforeN(Context context) {
        if (context == null) {
            return null;
        }

        int dataSubId = -1;
        try {
            Method getDefaultDataSubId = SubscriptionManager.class.getDeclaredMethod("getDefaultDataSubId");
            if (getDefaultDataSubId != null) {
                getDefaultDataSubId.setAccessible(true);
                dataSubId = (int) getDefaultDataSubId.invoke(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dataSubId != -1) {
            SubscriptionManager sm = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (sm != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                SubscriptionInfo si = sm.getActiveSubscriptionInfo(dataSubId);
                if (si != null) {
                    // format keep the same with android.telephony.TelephonyManager#getSimOperator
                    // MCC + MNC format
                    return String.valueOf(si.getMcc()) + si.getMnc();
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}