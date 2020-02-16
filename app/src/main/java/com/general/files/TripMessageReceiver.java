package com.general.files;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app85taxi.driver.ActiveTripActivity;
import com.app85taxi.driver.ChatActivity;
import com.app85taxi.driver.DriverArrivedActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;

/**
 * Created by Admin on 19-07-2016.
 */
public class TripMessageReceiver extends BroadcastReceiver {

    GeneralFunctions generalFunc;

    Activity activity;
    boolean isTripStartPage;

    public TripMessageReceiver(Activity activity, boolean isTripStartPage) {
        this.activity = activity;
        this.isTripStartPage = isTripStartPage;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (generalFunc == null) {
            generalFunc = new GeneralFunctions(context);
        }

        if (intent.getAction().equals(CommonUtilities.passenger_message_arrived_intent_action_trip_msg) && intent != null) {
            String json_message = intent.getExtras().getString(CommonUtilities.passenger_message_arrived_intent_key);

            if (generalFunc.getJsonValue("Message", json_message).equals("TripCancelled")) {

                Utils.generateNotification(context, generalFunc.retrieveLangLBl("",
                        "LBL_PASSENGER_CANCEL_TRIP_TXT"));


                if (activity instanceof ChatActivity) {
                    ((ChatActivity) activity).tripCancelled();
                } else if (activity instanceof ActiveTripActivity && isTripStartPage == true) {
                    ((ActiveTripActivity) activity).tripCancelled();
                } else if (activity instanceof DriverArrivedActivity) {
                    ((DriverArrivedActivity) activity).tripCancelled();
                }



//                if (isTripStartPage == true) {
//
//                    if (activity instanceof ChatActivity) {
//                        ((ChatActivity) activity).tripCancelled();
//                    } else {
//                        ((ActiveTripActivity) activity).tripCancelled();
//                    }
//                } else {
//                    if (activity instanceof ChatActivity) {
//                        ((ChatActivity) activity).tripCancelled();
//
//                    } else {
//                        ((DriverArrivedActivity) activity).tripCancelled();
//                    }
//                }

        } else if (generalFunc.getJsonValue("Message", json_message).equals("DestinationAdded")) {

            Utils.generateNotification(context, generalFunc.retrieveLangLBl("",
                    "LBL_DEST_ADD_BY_PASSENGER"));

            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                    "LBL_DEST_ADD_BY_PASSENGER"));
            if (isTripStartPage == true) {
                ((ActiveTripActivity) activity).onDestAddedByPassenger(json_message);
            }
        }
    }

}
}
