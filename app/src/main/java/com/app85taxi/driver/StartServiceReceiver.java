package com.app85taxi.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.general.files.MyBackGroundService;
import com.general.files.StartActProcess;
import com.utils.Utils;

/**
 * Created by Admin on 27-01-2016.
 */
public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        Utils.printLog("StartServiceReceiver","OnReceive");
        /*if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, MyBackGroundService.class);
            context.startService(i);
        }else */
//        if(Utils.isMyServiceRunning(MyBackGroundService.class,context) == false){
            new StartActProcess(context).startService(MyBackGroundService.class);
//        }

    }
}