package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.app85taxi.driver.AccountverificationActivity;
import com.app85taxi.driver.ActiveTripActivity;
import com.app85taxi.driver.CollectPaymentActivity;
import com.app85taxi.driver.DriverArrivedActivity;
import com.app85taxi.driver.MainActivity;
import com.app85taxi.driver.SuspendedDriver_Activity;
import com.app85taxi.driver.TripRatingActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;

import java.util.HashMap;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile {
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;
    boolean isnotification = false;

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
    }

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun, boolean isnotification) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.isnotification = isnotification;
    }

    public void startProcess() {
        generalFun.sendHeartBeat();

        responseString = generalFun.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        setGeneralData();


        Bundle bn = new Bundle();
        bn.putString("USER_PROFILE_JSON", responseString);
        bn.putString("IsAppReStart", "true"); // flag for retrieving data to en route trip pages

        String vTripStatus = generalFun.getJsonValue("vTripStatus", responseString);

        boolean lastTripExist = false;

        if (vTripStatus.contains("Not Active")) {


            String ratings_From_Driver_str = generalFun.getJsonValue("Ratings_From_Driver", responseString);

            if (!ratings_From_Driver_str.equals("Done")) {
                lastTripExist = true;
            }
        }
        if (generalFun.getJsonValue("vPhone", responseString).equals("") || generalFun.getJsonValue("vEmail", responseString).equals("")) {
            if (generalFun.getMemberId() != null && !generalFun.getMemberId().equals("")) {
                new StartActProcess(mContext).startActWithData(AccountverificationActivity.class, bn);
            }
        } else if (vTripStatus != null && !vTripStatus.equals("NONE") && !vTripStatus.equals("Cancelled")
                && (vTripStatus.trim().equals("Active") || vTripStatus.contains("On Going Trip")
                || vTripStatus.contains("Arrived") || lastTripExist == true)) {

            String last_trip_data = generalFun.getJsonValue("TripDetails", responseString);

            String passenger_data = generalFun.getJsonValue("PassengerDetails", responseString);

            HashMap<String, String> map = new HashMap<>();

            map.put("TotalSeconds", generalFun.getJsonValue("TotalSeconds", responseString));
            map.put("TimeState", generalFun.getJsonValue("TimeState", responseString));
            map.put("iTripTimeId", generalFun.getJsonValue("iTripTimeId", responseString));

            map.put("Message", "CabRequested");
            map.put("sourceLatitude", generalFun.getJsonValue("tStartLat", last_trip_data));
            map.put("sourceLongitude", generalFun.getJsonValue("tStartLong", last_trip_data));
            map.put("drivervName", generalFun.getJsonValue("vName", responseString));
            map.put("drivervLastName", generalFun.getJsonValue("vLastName", responseString));

            map.put("PassengerId", generalFun.getJsonValue("iUserId", last_trip_data));
            map.put("PName", generalFun.getJsonValue("vName", passenger_data) + " " + generalFun.getJsonValue("vLastName", passenger_data));
            map.put("PPicName", generalFun.getJsonValue("vImgName", passenger_data));
            map.put("PFId", generalFun.getJsonValue("vFbId", passenger_data));
            map.put("PRating", generalFun.getJsonValue("vAvgRating", passenger_data));
            map.put("PPhone", generalFun.getJsonValue("vPhone", passenger_data));
            map.put("PPhoneC", generalFun.getJsonValue("vPhoneCode", passenger_data));
            map.put("PAppVersion", generalFun.getJsonValue("iAppVersion", passenger_data));
            map.put("TripId", generalFun.getJsonValue("iTripId", last_trip_data));
            map.put("DestLocLatitude", generalFun.getJsonValue("tEndLat", last_trip_data));
            map.put("DestLocLongitude", generalFun.getJsonValue("tEndLong", last_trip_data));
            map.put("DestLocAddress", generalFun.getJsonValue("tDaddress", last_trip_data));
            map.put("REQUEST_TYPE", generalFun.getJsonValue("eType", last_trip_data));
            map.put("eFareType", generalFun.getJsonValue("eFareType", last_trip_data));
            map.put("iTripId", generalFun.getJsonValue("iTripId", last_trip_data));
            map.put("fVisitFee", generalFun.getJsonValue("fVisitFee", last_trip_data));
            map.put("eHailTrip", generalFun.getJsonValue("eHailTrip", last_trip_data));
            map.put("eTollSkipped", generalFun.getJsonValue("eTollSkipped", last_trip_data));

            map.put("vDeliveryConfirmCode", generalFun.getJsonValue("vDeliveryConfirmCode", last_trip_data));
            map.put("SITE_TYPE", generalFun.getJsonValue("SITE_TYPE", responseString));
            if (generalFun.getJsonValue("APP_TYPE", responseString).equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                map.put("PPetName", generalFun.getJsonValue("PetName", generalFun.getJsonValue("PetDetails", last_trip_data)));
                map.put("PPetId", generalFun.getJsonValue("iUserPetId", last_trip_data));
                map.put("PPetTypeName", generalFun.getJsonValue("PetTypeName", generalFun.getJsonValue("PetDetails", last_trip_data)));
            }
            if (generalFun.getJsonValue("APP_TYPE", responseString).equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
                map.put("PPetName", generalFun.getJsonValue("PetName", generalFun.getJsonValue("PetDetails", last_trip_data)));
                map.put("PPetId", generalFun.getJsonValue("iUserPetId", last_trip_data));
                map.put("PPetTypeName", generalFun.getJsonValue("PetTypeName", generalFun.getJsonValue("PetDetails", last_trip_data)));
            }

            if (vTripStatus.contains("Not Active") && lastTripExist == true) {
                // Open rating page
                bn.putSerializable("TRIP_DATA", map);

                String ePaymentCollect = generalFun.getJsonValue("ePaymentCollect", last_trip_data);
                if (ePaymentCollect.equals("No")) {
                    new StartActProcess(mContext).startActWithData(CollectPaymentActivity.class, bn);
                } else {
                    new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                }

            } else {

                if (vTripStatus.contains("Arrived")) {

                    if (!generalFun.isLocationEnabled()) {
                        generalFun.restartApp();
                        return;

                    }
                    // Open active trip page
                    map.put("vTripStatus", "Arrived");
                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);
                    new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("On Going Trip")) {
                    // Open active trip page

                    if (!generalFun.isLocationEnabled()) {
                        generalFun.restartApp();
                        return;

                    }
                    map.put("vTripStatus", "EN_ROUTE");
                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);
                    new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("Active")) {
                    // Open cubetaxiplus arrived page
                    if (!generalFun.isLocationEnabled()) {
                        generalFun.restartApp();
                        return;

                    }
                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);

                    new StartActProcess(mContext).startActWithData(DriverArrivedActivity.class, bn);
                }
            }

        } else {

            String eStatus = generalFun.getJsonValue("eStatus", responseString);

            if (eStatus.equalsIgnoreCase("suspend")) {
                new StartActProcess(mContext).startAct(SuspendedDriver_Activity.class);
            } else {
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);

            }
        }


        ActivityCompat.finishAffinity((Activity) mContext);
    }

    public void setGeneralData() {
        generalFun.storedata(Utils.ENABLE_PUBNUB_KEY, generalFun.getJsonValue("ENABLE_PUBNUB", responseString));
        generalFun.storedata(Utils.SESSION_ID_KEY, generalFun.getJsonValue("tSessionId", responseString));
        generalFun.storedata(Utils.DEVICE_SESSION_ID_KEY, generalFun.getJsonValue("tDeviceSessionId", responseString));

        Utils.printLog("sessionid", generalFun.retrieveValue(Utils.SESSION_ID_KEY));
        generalFun.storedata(CommonUtilities.PUBNUB_PUB_KEY, generalFun.getJsonValue("PUBNUB_PUBLISH_KEY", responseString));
        generalFun.storedata(CommonUtilities.PUBNUB_SUB_KEY, generalFun.getJsonValue("PUBNUB_SUBSCRIBE_KEY", responseString));
        generalFun.storedata(CommonUtilities.PUBNUB_SEC_KEY, generalFun.getJsonValue("PUBNUB_SECRET_KEY", responseString));
        generalFun.storedata(CommonUtilities.SITE_TYPE_KEY, generalFun.getJsonValue("SITE_TYPE", responseString));

        generalFun.storedata(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY, generalFun.getJsonValue("MOBILE_VERIFICATION_ENABLE", responseString));
        generalFun.storedata("LOCATION_ACCURACY_METERS", generalFun.getJsonValue("LOCATION_ACCURACY_METERS", responseString));
        generalFun.storedata("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFun.getJsonValue("DRIVER_LOC_UPDATE_TIME_INTERVAL", responseString));
        generalFun.storedata(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, generalFun.getJsonValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, responseString));

        generalFun.storedata(Utils.ENABLE_PUBNUB_KEY, generalFun.getJsonValue("ENABLE_PUBNUB", responseString));

        generalFun.storedata(CommonUtilities.WALLET_ENABLE, generalFun.getJsonValue("WALLET_ENABLE", responseString));
        generalFun.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFun.getJsonValue("REFERRAL_SCHEME_ENABLE", responseString));

        generalFun.storedata(CommonUtilities.APP_DESTINATION_MODE, generalFun.getJsonValue("APP_DESTINATION_MODE", responseString));
        generalFun.storedata(CommonUtilities.APP_TYPE, generalFun.getJsonValue("APP_TYPE", responseString));
        generalFun.storedata(CommonUtilities.HANDICAP_ACCESSIBILITY_OPTION, generalFun.getJsonValue("HANDICAP_ACCESSIBILITY_OPTION", responseString));
        generalFun.storedata(CommonUtilities.FEMALE_RIDE_REQ_ENABLE, generalFun.getJsonValue("FEMALE_RIDE_REQ_ENABLE", responseString));


    }
}
