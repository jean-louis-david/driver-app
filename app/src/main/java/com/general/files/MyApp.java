package com.general.files;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.view.WindowManager;

import com.app85taxi.driver.LauncherActivity;
import com.app85taxi.driver.R;
import com.splunk.mint.Mint;
import com.utils.CommonUtilities;
import com.utils.Utils;

/**
 * Created by Admin on 28-06-2016.
 */
public class MyApp extends Application {
    GeneralFunctions generalFun;
    private Activity mCurrentActivity = new LauncherActivity();
    private GpsReceiver mGpsReceiver;
    protected static MyApp mMyApp;

    boolean isAppInBackground = true;

    public static Activity currentactivity = null;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        setScreenOrientation();
        Mint.initAndStartSession(this, CommonUtilities.MINT_APP_ID);
        mMyApp = (MyApp) this.getApplicationContext();
        generalFun = new GeneralFunctions(this);

        if (mGpsReceiver == null)
            registerReceiver();


        if (generalFun != null && generalFun.getMemberId() != null) {
            Mint.addExtraData("iMemberId", "" + generalFun.getMemberId());
        }


    }

    public static synchronized MyApp getInstance() {
        return mMyApp;
    }

    public void stopAlertService() {
        stopService(new Intent(getBaseContext(), ChatHeadService.class));
    }


    public boolean isMyAppInBackGround(){
        return this.isAppInBackground;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mGpsReceiver != null)
            this.unregisterReceiver(mGpsReceiver);
        this.mGpsReceiver = null;
    }

    private void registerReceiver() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

            this.mGpsReceiver = new GpsReceiver();
            this.registerReceiver(this.mGpsReceiver, mIntentFilter);
        }
    }

    public void setScreenOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.setTitle(getResources().getString(R.string.app_name));
                mMyApp.setCurrentActivity(activity);

                currentactivity = activity;

                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mMyApp.setCurrentActivity(activity);

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mMyApp.setCurrentActivity(activity);
                if (!activity.getLocalClassName().equalsIgnoreCase("LauncherActivity") && generalFun.isLocationEnabled() == false && generalFun.retrieveValue("isInLauncher").equalsIgnoreCase("false")) {
                //    new GeneralFunctions(activity).restartApp();
                }
                currentactivity = activity;
                isAppInBackground = false;

                Utils.printLog("AppBackground","FromResume");
                Utils.sendBroadCast(getApplicationContext(), CommonUtilities.BACKGROUND_APP_RECEIVER_INTENT_ACTION);
               Utils.dismissBackGroundNotification(getApplicationContext());

            }

            @Override
            public void onActivityPaused(Activity activity) {
                clearReferences();

                isAppInBackground = true;

                Utils.printLog("AppBackground","FromPause");
                Utils.sendBroadCast(getApplicationContext(), CommonUtilities.BACKGROUND_APP_RECEIVER_INTENT_ACTION);
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                clearReferences();

            }


        });
    }


    private void clearReferences() {
        Activity currActivity = mMyApp.getCurrentActivity();
        if (this.equals(currActivity))
            mMyApp.setCurrentActivity(null);
    }


    public static Activity getCurrentAct() {
        return currentactivity;
    }


}
