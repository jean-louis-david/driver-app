package com.app85taxi.driver;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("ResourceType")
public class CabRequestedActivity extends AppCompatActivity implements GenerateAlertBox.HandleAlertBtnClick {

    public GeneralFunctions generalFunc;
    MTextView leftTitleTxt;
    MTextView rightTitleTxt;
    ProgressBar mProgressBar;
    RelativeLayout progressLayout;
    String message_str;
    MTextView pNameTxtView;
    MTextView locationAddressTxt;
    MTextView destAddressTxt;
    String pickUpAddress = "";
    ConfigPubNub configPubNub;

    GenerateAlertBox generateAlert;
    int maxProgressValue = 30;
    MediaPlayer mp = new MediaPlayer();
    private MTextView textViewShowTime; // will show the time
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds = 10 * 1000; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    private MTextView locationAddressHintTxt;
    private MTextView destAddressHintTxt;
    private MTextView serviceType;

    SimpleRatingBar ratingBar;
    boolean istimerfinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_cab_requested);


        generalFunc = new GeneralFunctions(getActContext());
        generalFunc.removeValue(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY);


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        MyApp.getInstance().stopAlertService();


        message_str = getIntent().getStringExtra("Message");

        configPubNub = new ConfigPubNub(getActContext(), true);

        String msgCode = generalFunc.getJsonValue("MsgCode", message_str);

        if (generalFunc.containsKey(CommonUtilities.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode)) {
            // generalFunc.restartApp();
            generalFunc.restartwithGetDataApp();
            return;
        } else {
            generalFunc.storedata(CommonUtilities.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "true");
            generalFunc.storedata(CommonUtilities.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "" + System.currentTimeMillis());
        }
        generalFunc.storedata(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

        leftTitleTxt = (MTextView) findViewById(R.id.leftTitleTxt);
        rightTitleTxt = (MTextView) findViewById(R.id.rightTitleTxt);
        pNameTxtView = (MTextView) findViewById(R.id.pNameTxtView);
        locationAddressTxt = (MTextView) findViewById(R.id.locationAddressTxt);
        locationAddressHintTxt = (MTextView) findViewById(R.id.locationAddressHintTxt);
        destAddressHintTxt = (MTextView) findViewById(R.id.destAddressHintTxt);
        destAddressTxt = (MTextView) findViewById(R.id.destAddressTxt);
        progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
        progressLayout.setClickable(false);
        progressLayout.setEnabled(false);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);

        textViewShowTime = (MTextView) findViewById(R.id.tvTimeCount);
        serviceType = (MTextView) findViewById(R.id.serviceType);

        (findViewById(R.id.menuImgView)).setVisibility(View.GONE);
        leftTitleTxt.setVisibility(View.VISIBLE);
        rightTitleTxt.setVisibility(View.VISIBLE);
        rightTitleTxt.setEnabled(false);
        rightTitleTxt.setClickable(false);

        mProgressBar.setMax(maxProgressValue);
        mProgressBar.setProgress(maxProgressValue);

        setLabels();

        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        generateAlert.setCancelable(false);

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapV2_calling_driver);

        fm.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap map) {
                double user_lat = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
                double user_lon = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));

                map.getUiSettings().setZoomControlsEnabled(false);

                MarkerOptions marker_opt = new MarkerOptions().position(new LatLng(user_lat, user_lon));

                marker_opt.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_passanger)).anchor(0.5f, 0.5f);

                map.addMarker(marker_opt);

                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(user_lat, user_lon))
                        .zoom(16).build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });

        setData();

        startTimer();

        progressLayout.setOnClickListener(new setOnClickList());
        leftTitleTxt.setOnClickListener(new setOnClickList());
        rightTitleTxt.setOnClickListener(new setOnClickList());

        if (generalFunc.retrieveValue(CommonUtilities.APP_TYPE).equals("Ride") || generalFunc.retrieveValue(CommonUtilities.APP_TYPE).equals("Delivery")) {
            (findViewById(R.id.requestType)).setVisibility(View.GONE);
        }
    }

    public void setLabels() {
        leftTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        rightTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));
        locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
        destAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DROP_OFF_LOCATION_TXT"));
        ((MTextView) findViewById(R.id.hintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_HINT_TAP_TXT"));
    }

    public void setData() {
//        p_rate_nameTxt.setText(Html.fromHtml("<font color=\"#FFFFFF\">" + generalFunc.getJsonValue("PRating", message_str) + " | " + "</font>" +
//                "<font color=\"" + getResources().getColor(R.color.appThemeColor_1) + "\">" + generalFunc.getJsonValue("PName", message_str) + "</font>"));

        new CreateRoundedView(Color.parseColor("#000000"), Utils.dipToPixels(getActContext(), 122), 0, Color.parseColor("#FFFFFF"), findViewById(R.id.bgCircle));
        pNameTxtView.setText(generalFunc.getJsonValue("PName", message_str));
        ratingBar.setRating(generalFunc.parseFloatValue(0, generalFunc.getJsonValue("PRating", message_str)));

        double user_lat = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
        double user_lon = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));

        GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setLocation(user_lat, user_lon);
        getAddressFromLocation.setAddressList(new GetAddressFromLocation.AddressFound() {
            @Override
            public void onAddressFound(String address, double latitude, double longitude) {
                locationAddressTxt.setText(address);
                pickUpAddress = address;
                rightTitleTxt.setVisibility(View.VISIBLE);
                rightTitleTxt.setEnabled(true);
                rightTitleTxt.setClickable(true);
                progressLayout.setClickable(true);
                progressLayout.setEnabled(true);
            }
        });
        getAddressFromLocation.execute();

        destAddressTxt.setVisibility(View.GONE);
        destAddressHintTxt.setVisibility(View.GONE);

        if (!generalFunc.getJsonValue("destLatitude", message_str).isEmpty() && !generalFunc.getJsonValue("destLongitude", message_str).isEmpty()) {

            double user_lat1 = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("destLatitude", message_str));
            double user_lon1 = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("destLongitude", message_str));

            if (user_lat1 == 0.0 && user_lon1 == 0.0) {
                destAddressTxt.setVisibility(View.GONE);
                destAddressHintTxt.setVisibility(View.GONE);
            } else {
                destAddressTxt.setVisibility(View.VISIBLE);
                destAddressHintTxt.setVisibility(View.VISIBLE);
                GetAddressFromLocation getAddressToLocation = new GetAddressFromLocation(getActContext(), generalFunc);
                getAddressToLocation.setLocation(user_lat1, user_lon1);
                getAddressToLocation.setAddressList(new GetAddressFromLocation.AddressFound() {
                    @Override
                    public void onAddressFound(String address, double latitude, double longitude) {

                        destAddressTxt.setText(address);
                    }
                });
                getAddressToLocation.execute();
            }
        }


        String REQUEST_TYPE = generalFunc.getJsonValue("REQUEST_TYPE", message_str);

        Utils.printLog("REQUEST_TYPE", REQUEST_TYPE);

        LinearLayout packageInfoArea = (LinearLayout) findViewById(R.id.packageInfoArea);
        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            ((MTextView) findViewById(R.id.requestType)).setText(generalFunc.retrieveLangLBl("Job", "LBL_JOB_TXT") + "  " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST"));
            (findViewById(R.id.serviceType)).setVisibility(View.VISIBLE);
            serviceType.setText(generalFunc.getJsonValue("SelectedTypeName", message_str));
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            ((MTextView) findViewById(R.id.requestType)).setText(generalFunc.retrieveLangLBl("Job", "LBL_JOB_TXT") + "  " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST"));
            (findViewById(R.id.serviceType)).setVisibility(View.VISIBLE);
            serviceType.setText(generalFunc.getJsonValue("SelectedTypeName", message_str));
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equals("Deliver")) {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.packageInfoTxt)).setText(generalFunc.getJsonValue("PACKAGE_TYPE", message_str));
            ((MTextView) findViewById(R.id.requestType)).setText(/*generalFunc.retrieveLangLBl("Ride Type", "LBL_RIDE_TYPE") + ": " +*/
                    generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY") + " " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST"));
        } else {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.requestType)).setText(/*generalFunc.retrieveLangLBl("Ride Type", "LBL_RIDE_TYPE") + ": " +*/
                    generalFunc.retrieveLangLBl("Ride", "LBL_RIDE") + " " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (istimerfinish) {
            finish();
            istimerfinish = false;
        }

//        playMedia();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCustoNotiSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeSound();
    }

    @Override
    public void handleBtnClick(int btn_id) {
        cancelRequest();
    }

    public void acceptRequest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        removeCustoNotiSound();
        progressLayout.setClickable(false);
        rightTitleTxt.setEnabled(false);
        leftTitleTxt.setEnabled(false);
        rightTitleTxt.setVisibility(View.GONE);

        generateTrip();
    }

    public void generateTrip() {

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), generateTripParams());
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        Bundle bn = new Bundle();
                        String message_json = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Message", "CabRequested");
                        map.put("sourceLatitude", generalFunc.getJsonValue("sourceLatitude", message_str));
                        map.put("sourceLongitude", generalFunc.getJsonValue("sourceLongitude", message_str));
                        map.put("PPetId", generalFunc.getJsonValue("PPetId", message_str));
                        map.put("PassengerId", generalFunc.getJsonValue("PassengerId", message_str));
                        map.put("PName", generalFunc.getJsonValue("PName", message_str));
                        map.put("PPicName", generalFunc.getJsonValue("PPicName", message_str));
                        map.put("PFId", generalFunc.getJsonValue("PFId", message_str));
                        map.put("PRating", generalFunc.getJsonValue("PRating", message_str));
                        map.put("PPhone", generalFunc.getJsonValue("PPhone", message_str));
                        map.put("PPhoneC", generalFunc.getJsonValue("PPhoneC", message_str));
                        map.put("TripId", generalFunc.getJsonValue("iTripId", message_json));
                        map.put("DestLocLatitude", generalFunc.getJsonValue("tEndLat", message_json));
                        map.put("DestLocLongitude", generalFunc.getJsonValue("tEndLong", message_json));
                        map.put("DestLocAddress", generalFunc.getJsonValue("tDaddress", message_json));
                        map.put("PAppVersion", generalFunc.getJsonValue("PAppVersion", message_json));
                        map.put("REQUEST_TYPE", generalFunc.getJsonValue("REQUEST_TYPE", message_str));
                        map.put("eFareType", generalFunc.getJsonValue("eFareType", message_json));
                        map.put("iTripId", generalFunc.getJsonValue("iTripId", message_json));

                        bn.putSerializable("TRIP_DATA", map);
                        new StartActProcess(getActContext()).startActWithData(DriverArrivedActivity.class, bn);


                        ActivityCompat.finishAffinity(CabRequestedActivity.this);
                    } else {

                        String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        if (msg_str.equals(CommonUtilities.GCM_FAILED_KEY) || msg_str.equals(CommonUtilities.APNS_FAILED_KEY)) {
                            //generalFunc.restartApp();
                            generalFunc.restartwithGetDataApp();
                        } else {
                            rightTitleTxt.setEnabled(true);
                            leftTitleTxt.setEnabled(true);
                            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
                            generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", msg_str));
                            generateAlert.showAlertBox();
                        }

                    }
                } else {
                    rightTitleTxt.setEnabled(true);
                    leftTitleTxt.setEnabled(true);
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void declineTripRequest() {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "DeclineTripRequest");
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                cancelRequest();
            }
        });
        exeWebServer.execute();
    }

    public HashMap<String, String> generateTripParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));
        parameters.put("start_lat", generalFunc.getJsonValue("sourceLatitude", message_str));
        parameters.put("start_lon", generalFunc.getJsonValue("sourceLongitude", message_str));
        parameters.put("iCabBookingId", generalFunc.getJsonValue("iBookingId", message_str));
        parameters.put("sAddress", pickUpAddress);
        parameters.put("GoogleServerKey", getResources().getString(R.string.google_api_get_address_from_location_serverApi));

        return parameters;
    }

    public void cancelRequest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        generalFunc.storedata(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        cancelCabReq();

        try {
            CabRequestedActivity.super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTimer() {
//        playMedia();
        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 1000) {
            // 1000 means, onTick function will be called at every 1000
            // milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                // i++;
                // Setting the Progress Bar to decrease wih the timer
                mProgressBar.setProgress((int) (leftTimeInMilliseconds / 1000));
                textViewShowTime.setTextAppearance(getActContext(), android.R.color.holo_green_dark);

                if ((seconds % 5) == 0) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {

                    if (blink) {
                        textViewShowTime.setVisibility(View.VISIBLE);
                    } else {
                        textViewShowTime.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink;
                }

                textViewShowTime
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));

            }

            @Override
            public void onFinish() {
                istimerfinish = true;
                textViewShowTime.setVisibility(View.VISIBLE);
//                textViewShowTime.setText("" + generalFunc.retrieveLangLBl("", "LBL_TIMER_FINISHED_TXT"));
                progressLayout.setClickable(false);
                rightTitleTxt.setEnabled(false);
                cancelRequest();
            }

        }.start();

    }


    public void playMedia() {
        removeSound();
        try {
            mp = new MediaPlayer();
            AssetFileDescriptor afd;
            afd = getAssets().openFd("ringtone.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.setLooping(true);
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //milan code for working all app

//        try { Utils.printLog("MediaPlayer", "MediaPlayer");
//            mp = MediaPlayer.create(getActContext(), R.raw.ringdriver); mp.setLooping(true); mp.start(); }
//        catch (IllegalStateException e) { } catch (Exception e) { }
    }


    private void removeCustoNotiSound() {
        if (mp != null) {
            mp.stop();
            mp = null;
        }


        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    public void removeSound() {
        if (mp != null) {
            mp.stop();
        }

    }

    public void cancelCabReq() {


        if (configPubNub != null) {
            configPubNub.publishMsg("PASSENGER_" + generalFunc.getJsonValue("PassengerId", message_str),
                    generalFunc.buildRequestCancelJson(generalFunc.getJsonValue("PassengerId", message_str)));
            configPubNub = null;
        }
        generalFunc.storedata(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY, "false");
    }

    public Context getActContext() {
        return CabRequestedActivity.this;
    }

    @Override
    public void onBackPressed() {
        cancelCabReq();
        removeCustoNotiSound();
        super.onBackPressed();


    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.progressLayout:
                    acceptRequest();
                    break;
                case R.id.leftTitleTxt:
                    //cancelRequest();
                    declineTripRequest();
                    break;
                case R.id.rightTitleTxt:
                    acceptRequest();
                    break;
            }
        }
    }

}
