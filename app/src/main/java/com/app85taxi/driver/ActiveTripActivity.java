package com.app85taxi.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;

import com.general.files.CancelTripDialog;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.ImageFilePath;
import com.general.files.OpenPassengerDetailDialog;
import com.general.files.StartActProcess;
import com.general.files.TripMessageReceiver;
import com.general.files.UpdateDirections;
import com.general.files.UpdateDriverLocationService;
import com.general.files.UpdateFrequentTask;
import com.general.files.UpdateTripLocationsService;
import com.general.files.UploadProfileImage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.utils.CommonUtilities.APP_TYPE;

public class ActiveTripActivity extends AppCompatActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdates {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    private static final int SELECT_PICTURE = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    String tripId = "";
    String eType = "";
    HashMap<String, String> data_trip;
    SupportMapFragment map;
    GoogleMap gMap;
    GetLocationUpdates getLocationUpdates;
    boolean isFirstLocation = true;
    public Location userLocation;
    TripMessageReceiver tripMsgReceiver;
    Intent startLocationUpdateService;
    MTextView addressTxt;
    boolean isDestinationAdded = false;
    double destLocLatitude = 0.0;
    double destLocLongitude = 0.0;
    Marker destLocMarker = null;
    Polyline route_polyLine;
    ExecuteWebServerUrl routeExeWebServer;
    boolean killRouteDrawn = false;
    ConfigPubNub configPubNub;
    Intent tripLocService_intent;
    UpdateTripLocationsService tripLocService;
    boolean mServiceBound = false;
    LinearLayout tripStartBtnArea;
    LinearLayout timerarea;
    LinearLayout tripEndBtnArea;
    boolean isTripCancelPressed = false;
    boolean isTripStart = false;
    String reason = "";
    String comment = "";
    String REQUEST_TYPE = "";
    String deliveryVerificationCode = "";
    android.support.v7.app.AlertDialog deliveryEndDialog;
    String SITE_TYPE = "";
    String SITE_TYPE_DEMO_MSG = "";
    String imageType = "";
    String isFrom = "";
    Dialog uploadServicePicAlertBox = null;
    LinearLayout destLocSearchArea;
    UpdateFrequentTask timerrequesttask;
    ArrayList<HashMap<String, String>> list;
    ArrayList<HashMap<String, String>> tripDetail;
    HashMap<String, String> tempMap;

    RecyclerView onGoingTripsDetailListRecyclerView;
    RatingBar ratingBar;
    SelectableRoundedImageView user_img;
    ArrayList<Double> additonallist = new ArrayList<>();
    String currencetprice = "0.0";
    MTextView userNameTxt, userAddressTxt, progressHinttext, timerHinttext, tollTxtView;
    MTextView txt_TimerHour, txt_TimerMinute, txt_TimerSecond;
    LinearLayout timerlayoutarea;
    String required_str = "";
    String invalid_str = "";
    android.support.v7.app.AlertDialog alertDialog;
    boolean isresume = false;

    int i = 0;
    private MTextView tvHour, tvMinute, tvSecond, btntimer;
    private String selectedImagePath = "";
    private Uri fileUri;
    private Handler handler;
    private Runnable runnable;
    private String TripTimeId = "";
    public ImageView emeTapImgView;

    View slideback;
    ImageView imageslide;
    android.support.v7.app.AlertDialog list_navigation;
    NestedScrollView scrollview;
    Menu menu;
    boolean isendslide = false;
    UpdateDirections updateDirections;
    // public MTextView timeTxt;

    Marker driverMarker;
    Location currentRotatedLocation;
    boolean isnotification = false;

    UpdateFrequentTask updatepubnubtask;

    ConfigPubNub pubNub;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateTripLocationsService.MyBinder myBinder = (UpdateTripLocationsService.MyBinder) service;
            tripLocService = myBinder.getService();
            tripLocService.startUpdate(tripId);
            mServiceBound = true;


        }
    };

    private void defaultAddtionalprice() {

        additonallist.add(0, 0.0);
        additonallist.add(1, 0.0);
        additonallist.add(2, 0.0);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_trip);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = new GeneralFunctions(getActContext());

        isnotification = getIntent().getBooleanExtra("isnotification", isnotification);

        defaultAddtionalprice();

        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        this.data_trip = data;
        Utils.printLog("Api", ":: arrived" + data.toString());

        scrollview = (NestedScrollView) findViewById(R.id.scrollview);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        onGoingTripsDetailListRecyclerView = (RecyclerView) findViewById(R.id.onGoingTripsDetailListRecyclerView);
        userNameTxt = (MTextView) findViewById(R.id.userNameTxt);
        userAddressTxt = (MTextView) findViewById(R.id.userAddressTxt);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        tvHour = (MTextView) findViewById(R.id.txtTimerHour);
        tvMinute = (MTextView) findViewById(R.id.txtTimerMinute);
        tvSecond = (MTextView) findViewById(R.id.txtTimerSecond);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        progressHinttext = (MTextView) findViewById(R.id.progressHinttext);
        timerHinttext = (MTextView) findViewById(R.id.timerHinttext);
        btntimer = (MTextView) findViewById(R.id.btn_timer);
        btntimer.setOnClickListener(new setOnClickAct());
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        tripStartBtnArea = (LinearLayout) findViewById(R.id.tripStartBtnArea);
        timerarea = (LinearLayout) findViewById(R.id.timerarea);
        timerlayoutarea = (LinearLayout) findViewById(R.id.timerlayoutarea);
        tripEndBtnArea = (LinearLayout) findViewById(R.id.tripEndBtnArea);
        destLocSearchArea = (LinearLayout) findViewById(R.id.destLocSearchArea);
        //timeTxt = (MTextView) findViewById(timeTxt);

        txt_TimerHour = (MTextView) findViewById(R.id.txt_TimerHour);
        txt_TimerMinute = (MTextView) findViewById(R.id.txt_TimerMinute);
        txt_TimerSecond = (MTextView) findViewById(R.id.txt_TimerSecond);
        tollTxtView = (MTextView) findViewById(R.id.tollTxtView);

        user_img = (SelectableRoundedImageView) findViewById(R.id.user_img);

        emeTapImgView = (ImageView) findViewById(R.id.emeTapImgView);
        emeTapImgView.setOnClickListener(new setOnClickList());

        slideback = (View) findViewById(R.id.slideback);
        imageslide = (ImageView) findViewById(R.id.imageslide);

        (findViewById(R.id.backImgView)).setVisibility(View.GONE);

        tripMsgReceiver = new TripMessageReceiver((Activity) getActContext(), true);
        tripLocService_intent = new Intent(getActContext(), UpdateTripLocationsService.class);


        generalFunc.storedata(CommonUtilities.IsTripStarted, "No");

        tripLocService_intent.putExtra("GeneratedTripID", "" + data_trip.get("TripId"));

        currencetprice = data_trip.get("fVisitFee");
        startLocationUpdateService = new Intent(getApplicationContext(), UpdateDriverLocationService.class);
        startLocationUpdateService.putExtra("PAppVersion", data_trip.get("PAppVersion"));

        new CreateRoundedView(getResources().getColor(android.R.color.transparent), Utils.dipToPixels(getActContext(), 15), 0,
                Color.parseColor("#00000000"), user_img);


        setLabels();
        setData();

        new CreateRoundedView(Color.parseColor("#2c3032"), Utils.dipToPixels(getActContext(), 60), 0, 0, findViewById(R.id.slideback));


        map.getMapAsync(this);


        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        tripStartBtnArea.setOnTouchListener(new setOnTouchList());
        tripEndBtnArea.setOnTouchListener(new setOnTouchList());

        registerTripMsgReceiver();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(startLocationUpdateService);
            }
        }, 4000);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        Utils.printLog("Active trip", ":tripId:" + tripId);
        Utils.printLog("Data", "::" + data_trip.size());

        if (generalFunc.isRTLmode()) {
            (findViewById(R.id.navStripImgView)).setRotation(180);
        }

        //calculateData("", finalvalTxt);

        if (isnotification) {
            //new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, isnotification);

        }
        if (generalFunc.retrieveValue("OPEN_CHAT").equals("Yes")) {
            generalFunc.storedata("OPEN_CHAT", "No");
            Bundle bnChat = new Bundle();

            bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
            bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
            bnChat.putString("iTripId", data_trip.get("iTripId"));
            bnChat.putString("FromMemberName", data_trip.get("PName"));

            new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
        }

        if (isPubNubEnabled()) {
            pubNub = new ConfigPubNub(getActContext());
        }

        updatepubnubtask = new UpdateFrequentTask(5 * 1000);
//            this.updateRequest = updateRequest;
        updatepubnubtask.setTaskRunListener(new UpdateFrequentTask.OnTaskRunCalled() {
            @Override
            public void onTaskRun() {


                if (pubNub != null) {
                    pubNub.subscribeToPrivateChannel();
                }

                generalFunc.sendHeartBeat();

            }
        });
        updatepubnubtask.startRepeatingTask();
    }

    public void setTimetext(String distance, String time) {
        try {
            if (!generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
//                timeTxt.setText(time + " " + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT") + " & " + distance + "  " + generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));
            } else {
                // timeTxt.setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }

    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            if (view.getId() == emeTapImgView.getId()) {
                Bundle bn = new Bundle();
                bn.putString("TripId", tripId);
                new StartActProcess(getActContext()).startActWithData(ConfirmEmergencyTapActivity.class, bn);
            }
        }
    }

    public boolean isPubNubEnabled() {
        String ENABLE_PUBNUB = generalFunc.retrieveValue(Utils.ENABLE_PUBNUB_KEY);

        return ENABLE_PUBNUB.equalsIgnoreCase("Yes");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("RESTART_STATE", "true");
        outState.putParcelable("file_uri", fileUri);
        super.onSaveInstanceState(outState);
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("En Route", "LBL_EN_ROUTE_TXT"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        invalid_str = generalFunc.retrieveLangLBl("Invalid value", "LBL_DIGIT_REQUIRE");
        ((MTextView) findViewById(R.id.startTripTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_TRIP_TXT"));
        ((MTextView) findViewById(R.id.endTripTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_END_TRIP_TXT"));
        ((MTextView) findViewById(R.id.placeTxtView)).setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
        ((MTextView) findViewById(R.id.navigateTxt)).setText(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));

        timerHinttext.setText(generalFunc.retrieveLangLBl("JOB TIMER", "LBL_JOB_TIMER_HINT"));
        progressHinttext.setText(generalFunc.retrieveLangLBl("JOB PROGRESS", "LBL_PROGRESS_HINT"));

        txt_TimerHour.setText(generalFunc.retrieveLangLBl("", "LBL_HOUR_TXT"));
        txt_TimerMinute.setText(generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT"));
        txt_TimerSecond.setText(generalFunc.retrieveLangLBl("", "LBL_SECONDS_TXT"));

        tollTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_SKIP_HELP"));

        setButtonName();
    }

    public void setButtonName() {
        if (REQUEST_TYPE.equals("Deliver")) {
            ((MTextView) findViewById(R.id.startTripTxt)).setText(generalFunc.retrieveLangLBl("Slide to begin delivery", "LBL_SLIDE_BEGIN_DELIVERY"));
            ((MTextView) findViewById(R.id.endTripTxt)).setText(generalFunc.retrieveLangLBl("Slide to end delivery", "LBL_SLIDE_END_DELIVERY"));
        } else {
            ((MTextView) findViewById(R.id.startTripTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_TRIP_TXT"));
            ((MTextView) findViewById(R.id.endTripTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_END_TRIP_TXT"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;
        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(false);
        }

        getMap().getUiSettings().setTiltGesturesEnabled(false);
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);

        if (isDestinationAdded == true && destLocMarker == null) {
            addDestinationMarker();
        }

        if (isDestinationAdded == true && userLocation != null && route_polyLine == null) {
            drawRoute("" + destLocLatitude, "" + destLocLongitude);
        }

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();
                return false;
            }
        });

        //addSourceMarker();
        getLocationUpdates = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true);
        getLocationUpdates.setLocationUpdatesListener(this);
    }

    public void updateDriverMarker() {
        if (userLocation == null) {
            return;
        }

        LatLng userlatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        boolean isRotate = false;

        LatLng currentLatlng = null;
        double angle = -1.000;
        double distance = 0;

        if (currentRotatedLocation != null) {
            distance = Utils.CalculationByLocation(currentRotatedLocation.getLatitude(), currentRotatedLocation.getLongitude(), userLocation.getLatitude(), userLocation.getLongitude(), "METER");


            Utils.printLog("distance", currentRotatedLocation.distanceTo(userLocation) + "");

        }
        if (currentRotatedLocation != null) {
            isRotate = true;
            currentRotatedLocation = userLocation;

            if (driverMarker != null) {
                Projection proj = gMap.getProjection();
                Point startPoint = proj.toScreenLocation(driverMarker.getPosition());
                final LatLng startLatLng = proj.fromScreenLocation(startPoint);
                currentLatlng = new LatLng(currentRotatedLocation.getLatitude(), currentRotatedLocation.getLongitude());
                angle = Utils.bearingBetweenLocations(startLatLng, userlatlng);
            }

        }


        if (driverMarker == null) {
            MarkerOptions markerOptions_driver = new MarkerOptions();
            markerOptions_driver.position(userlatlng);
            markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_driver)).anchor(0.5f,
                    0.5f).flat(true);
            driverMarker = gMap.addMarker(markerOptions_driver);
        }


        Utils.animateMarker(driverMarker, userlatlng, false, gMap, isRotate, angle);
    }

    public void addDestinationMarker() {
        if (getMap() == null) {
            return;
        }
        MarkerOptions markerOptions_destLocation = new MarkerOptions();
        markerOptions_destLocation.position(new LatLng(destLocLatitude, destLocLongitude));
        markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f,
                0.5f);
        destLocMarker = getMap().addMarker(markerOptions_destLocation);
    }

    public void addSourceMarker() {
        if (getMap() == null) {
            return;
        }
        double latitude = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double longitude = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));
        MarkerOptions markerOptions_destLocation = new MarkerOptions();
        markerOptions_destLocation.position(new LatLng(latitude, longitude));
        markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f,
                0.5f);
        getMap().addMarker(markerOptions_destLocation);
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    private void setDriverDetail() {


        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/" + tripDetail.get(0).get("iDriverId") + "/"
                + tripDetail.get(0).get("driverImage");

        Picasso.with(getActContext())
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((ImageView) findViewById(R.id.user_img)));

        userNameTxt.setText(tripDetail.get(0).get("driverName"));
        userAddressTxt.setText(tripDetail.get(0).get("tSaddress"));
        Utils.printLog("Api", "driverRating" + generalFunc.parseFloatValue(0, tripDetail.get(0).get("driverRating")));
        ratingBar.setRating(generalFunc.parseFloatValue(0, tripDetail.get(0).get("driverRating")));

    }

    public void setData() {
        Utils.printLog("datatrip", data_trip.toString());

        tripId = data_trip.get("TripId");
        eType = data_trip.get("REQUEST_TYPE");
        deliveryVerificationCode = data_trip.get("vDeliveryConfirmCode");

        if (!data_trip.get("DestLocLatitude").equals("") && !data_trip.get("DestLocLatitude").equals("0")
                && !data_trip.get("DestLocLongitude").equals("") && !data_trip.get("DestLocLongitude").equals("0")) {

            setDestinationPoint(data_trip.get("DestLocLatitude"), data_trip.get("DestLocLongitude"), data_trip.get("DestLocAddress"), true);
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);


        } else {
            (findViewById(R.id.destLocSearchArea)).setOnClickListener(new setOnClickAct());
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.navigationViewArea)).setVisibility(View.GONE);
            tollTxtView.setVisibility(View.GONE);
            if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
                Utils.printLog("APP_TYPE", "APP_TYPE" + generalFunc.retrieveValue(APP_TYPE));
                destLocSearchArea.setVisibility(View.GONE);

                if (generalFunc.retrieveValue(CommonUtilities.APP_DESTINATION_MODE).equalsIgnoreCase(CommonUtilities.STRICT_DESTINATION) || generalFunc.retrieveValue(CommonUtilities.APP_DESTINATION_MODE).equalsIgnoreCase(CommonUtilities.NON_STRICT_DESTINATION)) {
                    Utils.printLog("APP_DESTINATION_MODEfragment", "" + generalFunc.retrieveValue(CommonUtilities.APP_DESTINATION_MODE));
                    if (destLocSearchArea.getVisibility() == View.GONE) {
                        destLocSearchArea.setVisibility(View.VISIBLE);
                    }
                }

            }
        }

        if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly) || data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);
            (findViewById(R.id.navigationViewArea)).setVisibility(View.GONE);
            tollTxtView.setVisibility(View.GONE);
        }


        if (!data_trip.get("vTripStatus").equals("Arrived")) {
            tripStartBtnArea.setVisibility(View.GONE);
            tripEndBtnArea.setVisibility(View.VISIBLE);
            isendslide = true;
            invalidateOptionsMenu();
            imageslide.setImageResource(R.mipmap.ic_trip_btn);


            configTripStartView();

            if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {

                countDownStart();
                if (data_trip.get("TimeState") != null && !data_trip.get("TimeState").equals("")) {
                    if (data_trip.get("TimeState").equalsIgnoreCase("resume")) {

//
//                        if (timerrequesttask != null) {
//                            timerrequesttask.stopRepeatingTask();
//                        }
                        isresume = true;
                        btntimer.setText(generalFunc.retrieveLangLBl("", "LBL_PAUSE_TEXT"));
                        btntimer.setVisibility(View.VISIBLE);


                    } else {
                        // countDownStart();
                        if (timerrequesttask != null) {
                            timerrequesttask.stopRepeatingTask();
                            timerrequesttask = null;
                        }

                        isresume = false;
                        btntimer.setText(generalFunc.retrieveLangLBl("", "LBL_RESUME_TEXT"));
                        btntimer.setVisibility(View.VISIBLE);

                    }
                }

                if (data_trip.get("TotalSeconds") != null && !data_trip.get("TotalSeconds").equals("")) {
                    i = Integer.parseInt(data_trip.get("TotalSeconds"));
                    tvHour.setText("" + String.format("%02d", i / 3600));
                    tvMinute.setText("" + String.format("%02d", (i % 3600) / 60));
                    tvSecond.setText("" + String.format("%02d", i % 60));

                }
                if (data_trip.get("iTripTimeId") != null && !data_trip.get("iTripTimeId").equals("")) {
                    TripTimeId = data_trip.get("iTripTimeId");
                    countDownStart();

                } else {


                }
            }

        }

        REQUEST_TYPE = data_trip.get("REQUEST_TYPE");
        SITE_TYPE = data_trip.get("SITE_TYPE");
        deliveryVerificationCode = data_trip.get("vDeliveryConfirmCode");

        setButtonName();
        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            getTripDeliveryLocations();
            if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
                timerarea.setVisibility(View.GONE);
                scrollview.setVisibility(View.GONE);

            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
                timerarea.setVisibility(View.VISIBLE);
                emeTapImgView.setVisibility(View.GONE);
                scrollview.setVisibility(View.VISIBLE);
                timerlayoutarea.setVisibility(View.GONE);
                //btntimer.setVisibility(View.GONE);


            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {

                //btntimer.setVisibility(View.VISIBLE);
                timerarea.setVisibility(View.VISIBLE);
                emeTapImgView.setVisibility(View.GONE);
                scrollview.setVisibility(View.VISIBLE);
                timerlayoutarea.setVisibility(View.VISIBLE);

            } else {
                timerarea.setVisibility(View.GONE);
                scrollview.setVisibility(View.GONE);


            }
        } else if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            getTripDeliveryLocations();
            if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
                timerarea.setVisibility(View.GONE);

            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
                timerarea.setVisibility(View.VISIBLE);
                scrollview.setVisibility(View.VISIBLE);
                timerlayoutarea.setVisibility(View.GONE);
                emeTapImgView.setVisibility(View.GONE);
                //btntimer.setVisibility(View.GONE);


            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {

                //btntimer.setVisibility(View.VISIBLE);
                timerarea.setVisibility(View.VISIBLE);
                scrollview.setVisibility(View.VISIBLE);
                emeTapImgView.setVisibility(View.GONE);
                timerlayoutarea.setVisibility(View.VISIBLE);

            } else {
                timerarea.setVisibility(View.GONE);


            }
        } else {
            try {
//                if (data_trip.get("eFareType") != null && !data_trip.get("eFareType").equals("")) {
//                    getTripDeliveryLocations();
//                    if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
//                        timerarea.setVisibility(View.GONE);
//
//                    } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
//                        timerarea.setVisibility(View.VISIBLE);
//                        timerlayoutarea.setVisibility(View.GONE);
//                        //btntimer.setVisibility(View.GONE);
//
//
//                    } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
//
//                        //btntimer.setVisibility(View.VISIBLE);
//                        timerarea.setVisibility(View.VISIBLE);
//                        timerlayoutarea.setVisibility(View.VISIBLE);
//
//                    } else {
//                        timerarea.setVisibility(View.GONE);
//
//
//                    }
//                }
                timerarea.setVisibility(View.GONE);
                scrollview.setVisibility(View.GONE);
                timerlayoutarea.setVisibility(View.GONE);
                emeTapImgView.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    }

    public void onDestAddedByPassenger(String json_message) {
        String DLatitude = generalFunc.getJsonValue("DLatitude", json_message);
        String DLongitude = generalFunc.getJsonValue("DLongitude", json_message);
        String DAddress = generalFunc.getJsonValue("DAddress", json_message);

        setDestinationPoint(DLatitude, DLongitude, DAddress, true);

        if (destLocMarker == null) {
            addDestinationMarker();
        }
        drawRoute(DLatitude, DLongitude);

        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Destination is added by passenger", "LBL_DEST_ADD_BY_PASSENGER"));
    }

    public void drawRoute(final String dest_lat, final String dest_lon) {

        String originLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destLoc = dest_lat + "," + dest_lon;
        String serverKey = getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        Utils.printLog("url Destination", "url:" + url);

        if (this.routeExeWebServer != null) {
            this.routeExeWebServer.cancel(true);
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        this.routeExeWebServer = exeWebServer;
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunc.getJsonValue("status", responseString);

                    if (status.equals("OK")) {

                        PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.appThemeColor_2));

                        if (lineOptions != null) {
                            if (route_polyLine != null) {
                                route_polyLine.remove();
                            }
                            route_polyLine = gMap.addPolyline(lineOptions);
                        }

                    } else {
                        // Notify cubetaxiplus that Route is not drawn.
                        killRouteDrawn = true;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route drawn failed", "LBL_ROUTE_DRAW_FAILED"));
                    }

                } else {
                    // Notify cubetaxiplus that Route is not drawn.
                    killRouteDrawn = true;
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Route drawn failed", "LBL_ROUTE_DRAW_FAILED"));
                }
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;

        if (currentRotatedLocation == null) {
            currentRotatedLocation = location;

        }
        CameraPosition cameraPosition = cameraForUserPosition();

        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);


//        if (!data_trip.get("REQUEST_TYPE").equalsIgnoreCase("UberX")) {
        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
                if (updateDirections == null) {
                    Location destLoc = new Location("temp");
                    destLoc.setLatitude(destLocLatitude);
                    destLoc.setLongitude(destLocLongitude);


                    updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                    updateDirections.scheduleDirectionUpdate();
                }

            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
                //    timeTxt.setVisibility(View.GONE);
                return;

            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                // timeTxt.setVisibility(View.GONE);
                return;
            } else {
                if (updateDirections == null) {
                    Location destLoc = new Location("temp");
                    destLoc.setLatitude(destLocLatitude);
                    destLoc.setLongitude(destLocLongitude);


                    updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                    updateDirections.scheduleDirectionUpdate();
                }

            }
        } else {
            if (updateDirections == null) {
                Location destLoc = new Location("temp");
                destLoc.setLatitude(destLocLatitude);
                destLoc.setLongitude(destLocLongitude);


                updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                updateDirections.scheduleDirectionUpdate();
            }
        }

//            if (updateDirections == null) {
//                Location destLoc = new Location("temp");
//                destLoc.setLatitude(destLocLatitude);
//                destLoc.setLongitude(destLocLongitude);
//
//
//                updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
//                updateDirections.scheduleDirectionUpdate();
//            }
//        } else {
//            //timeTxt.setVisibility(View.GONE);
//        }

        updateDriverMarker();

    }

    public CameraPosition cameraForUserPosition() {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                .zoom((float) currentZoomLevel).build();

        return cameraPosition;
    }

    public void tripCancelled() {

        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                generalFunc.saveGoOnlineInfo();
                // generalFunc.restartApp();
                generalFunc.restartwithGetDataApp();
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void getTripDeliveryLocations() {

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTripDeliveryLocations");
        parameters.put("iTripId", data_trip.get("iTripId"));
        // parameters.put("iCabBookingId", "");
        parameters.put("userType", "Driver");

        //        parameters.put("iUserId", generalFunc.getMemberId());

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Api", "responseString" + responseString);
//                if (responseString != null && !responseString.equals("")) {
//
//                    closeLoader();
//
//                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {
//                        list = new ArrayList<>();
//
//                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
//                        server_time = generalFunc.getJsonValue("SERVER_TIME", responseString);
//
//                        JSONArray tripLocations = generalFunc.getJsonArray("tripLocations", message);
//
//                        if (tripLocations != null)
//                            for (int i = 0; i < tripLocations.length(); i++) {
//
//                                JSONObject jobject1 = generalFunc.getJsonObject(tripLocations, i);
//
//                                if (tempMap.size() == 0 && !generalFunc.getJsonValue("tArrivedTime", jobject1.toString()).equalsIgnoreCase("0000-00-00 00:00:00")) {
//                                    tempMap.put("status", "DriverArrived");
//                                    tempMap.put("value", getTimeDiffrence(generalFunc.getJsonValue("tArrivedTime", jobject1.toString())));
//                                    tempMap.put("Booking_LBL", generalFunc.retrieveLangLBl("", "LBL_BOOKING_NO_TXT"));
//                                    tempMap.put("iTripId", generalFunc.getJsonValue("iTripId", jobject1.toString()));
//                                    tempMap.put("time", formateDateFromstring("yyyy-MM-dd HH:mm:ss", "HH:mm aa", generalFunc.getJsonValue("tArrivedTime", jobject1.toString())));
//                                    tempMap.put("msg", generalFunc.retrieveLangLBl("Driver arrive to", "LBL_DRIVER_ARRIVED_TO_LOC_TXT") + " " + generalFunc.getJsonValue("tAddress", jobject1.toString()));
//                                    list.add(tempMap);
//                                }
//                                if (!generalFunc.getJsonValue("tStartTime", jobject1.toString()).equalsIgnoreCase("0000-00-00 00:00:00")) {
//                                    HashMap<String, String> map = new HashMap<>();
//                                    map.put("status", "On the way");
//                                    map.put("Booking_LBL", generalFunc.retrieveLangLBl("", "LBL_BOOKING_NO_TXT"));
//                                    map.put("value", getTimeDiffrence(generalFunc.getJsonValue("tStartTime", jobject1.toString())));
//                                    map.put("iTripId", generalFunc.getJsonValue("iTripId", jobject1.toString()));
//                                    map.put("time", formateDateFromstring("yyyy-MM-dd HH:mm:ss", "HH:mm aa", generalFunc.getJsonValue("tStartTime", jobject1.toString())));
//                                    map.put("msg", generalFunc.retrieveLangLBl("Driver is on the way to deliver parcel.", "LBL_DRIVER_ON_THE_WAY_TXT") + "\n" + generalFunc.getJsonValue("tAddress", jobject1.toString()));
//                                    list.add(map);
//                                }
//                                if (!generalFunc.getJsonValue("tEndTime", jobject1.toString()).equalsIgnoreCase("0000-00-00 00:00:00") && !generalFunc.getJsonValue("eCancelled", jobject1.toString()).equalsIgnoreCase("Yes")) {
//                                    HashMap<String, String> map = new HashMap<>();
//                                    map.put("status", "Delivered");
//                                    map.put("iTripId", generalFunc.getJsonValue("iTripId", jobject1.toString()));
//                                    map.put("Booking_LBL", generalFunc.retrieveLangLBl("", "LBL_BOOKING_NO_TXT"));
//                                    map.put("value", getTimeDiffrence(generalFunc.getJsonValue("tEndTime", jobject1.toString())));
//                                    map.put("time", formateDateFromstring("yyyy-MM-dd HH:mm:ss", "HH:mm aa", generalFunc.getJsonValue("tEndTime", jobject1.toString())));
//                                    map.put("msg", generalFunc.retrieveLangLBl("Driver delivered the parcel to", "LBL_DRIVER_DELIVERED_PARCEL_TXT") + " " + generalFunc.getJsonValue("vName", jobject1.toString()));
//                                    list.add(map);
//                                }
//
//                                String msg = "";
//                                String reason = "";
//                                String comment = "";
//
//                                if (generalFunc.getJsonValue("eCancelBy", jobject1.toString()).equals("Driver")) {
//                                    msg = generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_DRIVER");
//                                } else {
//                                    msg = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
//                                }
//                                msg = generalFunc.retrieveLangLBl("Your trip has been cancelled.", "LBL_TRIP_CANCELLED_TXT");
//
//
//                                if (!TextUtils.isEmpty(generalFunc.getJsonValue("vCancelReason", jobject1.toString()))) {
//                                    reason = generalFunc.retrieveLangLBl("Reason", "LBL_REASON") + ":" + generalFunc.getJsonValue("vCancelReason", jobject1.toString());
//                                }
//                                if (!TextUtils.isEmpty(generalFunc.getJsonValue("vCancelComment", jobject1.toString()))) {
//                                    comment = generalFunc.retrieveLangLBl("COMMENT", "LBL_COMMENT_TXT") + ":" + generalFunc.getJsonValue("vCancelComment", jobject1.toString());
//                                }
//
//                                Utils.printLog("Api", "eCancelled" + generalFunc.getJsonValue("eCancelled", jobject1.toString()));
//                                if (generalFunc.getJsonValue("eCancelled", jobject1.toString()).equalsIgnoreCase("Yes") && !generalFunc.getJsonValue("tEndTime", jobject1.toString()).equalsIgnoreCase("0000-00-00 00:00:00")) {
//                                    HashMap<String, String> map = new HashMap<>();
//                                    map.put("status", "Cancelled");
//                                    map.put("iTripId", generalFunc.getJsonValue("iTripId", jobject1.toString()));
//                                    map.put("value", getTimeDiffrence(generalFunc.getJsonValue("tEndTime", jobject1.toString())));
//                                    map.put("time", formateDateFromstring("yyyy-MM-dd HH:mm:ss", "HH:mm aa", generalFunc.getJsonValue("tEndTime", jobject1.toString())));
//                                    map.put("Booking_LBL", generalFunc.retrieveLangLBl("Booking No", "LBL_BOOKING_NO_TXT"));
//                                    map.put("msg", msg + "\n" + reason + "\n" + comment);
//                                    list.add(map);
//                                }
//
//
//
//        /*if (!map.get("tArrivedTime").equalsIgnoreCase("0000-00-00 00:00:00"))
//        {
//            map.put("status","On the way");
//            map.put("value",getTimeDiffrence(map.get("tDeliveredTime")));
//            map.put("time",formateDateFromstring("MM/dd/yyyy HH:mm:ss", "HH:mm aa", map.get("tArrivedTime")));
//            map.put("address",map.get("tDaddress"));
//            map.put("msg",generalFunc.retrieveLangLBl("","LBL_DRIVER_ON_THE_WAY_TXT"));
//        }
//*/  /*if (!map.get("tArrivedTime").equalsIgnoreCase("0000-00-00 00:00:00"))
//        {
//            map.put("status","Request accepted");
//            map.put("value",getTimeDiffrence(map.get("tDeliveredTime")));
//            map.put("time",formateDateFromstring("MM/dd/yyyy HH:mm:ss", "HH:mm aa", map.get("tArrivedTime")));
//            map.put("address",map.get("tDaddress"));
//            map.put("msg",generalFunc.retrieveLangLBl("","LBL_DRIVER_ON_THE_WAY_TXT"));
//        }
//*/
//
//
//                                Collections.sort(list, new Comparator<HashMap<String,String>>(){
//                                    public int compare(HashMap<String,String> emp1, HashMap<String,String> emp2) {
//                                        // ## Ascending order
//                                        return emp1.get("time").compareToIgnoreCase(emp2.get("time")); // To compare string values
//                                        // return Integer.valueOf(emp1.getId()).compareTo(emp2.getId()); // To compare integer values
//
//                                        // ## Descending order
//                                        // return emp2.getFirstName().compareToIgnoreCase(emp1.getFirstName()); // To compare string values
//                                        // return Integer.valueOf(emp2.getId()).compareTo(emp1.getId()); // To compare integer values
//                                    }
//                                });
//                                Collections.reverse(list);
//                            }
//
//                        setView();
//                    } else {
//                        generateErrorView();
//                    }
//                } else {
//                    generateErrorView();
//                }
                if (responseString != null && !responseString.equals("")) {


                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {
                        list = new ArrayList<>();

                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);


                        tripDetail = new ArrayList<HashMap<String, String>>();
                        JSONArray tripLocations = generalFunc.getJsonArray("States", message);
                        String driverdetails = generalFunc.getJsonValue("driverDetails", message);
                        tempMap = new HashMap<>();
                        tempMap.put("driverImage", generalFunc.getJsonValue("riderImage", driverdetails));
                        tempMap.put("driverName", generalFunc.getJsonValue("riderName", driverdetails));
                        tempMap.put("driverRating", generalFunc.getJsonValue("riderRating", driverdetails));
                        tempMap.put("tSaddress", generalFunc.getJsonValue("tSaddress", driverdetails));
                        tempMap.put("iDriverId", generalFunc.getJsonValue("iUserId", driverdetails));
                        // tempMap.put("eHailTrip",generalFunc.getJsonValue("iUserId", driverdetails))

                        tripDetail.add(tempMap);


                        list.clear();
                        //tempMap.clear();
                        if (tripLocations != null)
                            for (int i = 0; i < tripLocations.length(); i++) {
                                tempMap = new HashMap<>();

                                JSONObject jobject1 = generalFunc.getJsonObject(tripLocations, i);
                                tempMap.put("status", generalFunc.getJsonValue("type", jobject1.toString()));
                                tempMap.put("iTripId", generalFunc.getJsonValue("text", jobject1.toString()));

                                tempMap.put("value", generalFunc.getJsonValue("timediff", jobject1.toString()));
                                tempMap.put("Booking_LBL", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                                tempMap.put("time", generalFunc.getJsonValue("time", jobject1.toString()));
                                tempMap.put("msg", generalFunc.getJsonValue("text", jobject1.toString()));
                                list.add(tempMap);
                            }
                        setView();

                        setDriverDetail();
                    } else {

                    }
                } else {

                }
            }
        });
        exeWebServer.execute();
    }

    public void buildMsgOnDeliveryEnd() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Delivery Confirmation", "LBL_DELIVERY_CONFIRM"));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_end_delivery_design, null);

        final MaterialEditText verificationCodeBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        String contentMsg = generalFunc.retrieveLangLBl("Please enter the confirmation code received from recipient.", "LBL_DELIVERY_END_NOTE");
        if (SITE_TYPE.equalsIgnoreCase("Demo")) {
            contentMsg = contentMsg + " \n" +
                    generalFunc.retrieveLangLBl("For demo purpose, please enter confirmation code in text box as shown below.", "LBL_DELIVERY_END_NOTE_DEMO")
                    + " \n" + generalFunc.retrieveLangLBl("Confirmation Code", "LBL_CONFIRMATION_CODE") + ": " + deliveryVerificationCode;
        }

        ((MTextView) dialogView.findViewById(R.id.contentMsgTxt)).setText(contentMsg);

        builder.setView(dialogView);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        deliveryEndDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(deliveryEndDialog);
        }
        deliveryEndDialog.show();

        deliveryEndDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.checkText(verificationCodeBox) == false) {
                    verificationCodeBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                    return;
                }

                if (!Utils.getText(verificationCodeBox).equals(deliveryVerificationCode)) {
                    verificationCodeBox.setError(generalFunc.retrieveLangLBl("Invalid code", "LBL_INVALID_DELIVERY_CONFIRM_CODE"));
                    return;
                }

                deliveryEndDialog.dismiss();

                if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX") && generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                    takeAndUploadPic(getActContext(), "after");
                } else {
                    endTrip();
                }

            }
        });

        deliveryEndDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deliveryEndDialog.dismiss();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        if (REQUEST_TYPE.equals("Deliver")) {

            menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View Delivery Details", "LBL_VIEW_DELIVERY_DETAILS"));
            if (!isendslide) {
                menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
            } else {
                MenuItem item = menu.findItem(R.id.menu_cancel_trip);
                item.setVisible(false);
                //  menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY")).setVisible(false);
            }
        } else {
            try {
                if (data_trip.get("eHailTrip").equalsIgnoreCase("Yes")) {
                    menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(false);
                } else {
                    menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(true);
                }
            } catch (Exception e) {
                menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(true);
            }
            menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel trip", "LBL_CANCEL_TRIP"));
        }


        if (!REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {
            menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(true);

        } else {
            menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL")).setVisible(false);


        }

        Utils.setMenuTextColor(menu.findItem(R.id.menu_passenger_detail), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_cancel_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_waybill_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        return true;
    }

    double finaltotal = 0.0;
    double matrialfee = 0.0;
    double miscfee = 0.0;
    double discount = 0.0;


    public void showAddtionalChargeBox() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_addtional_charge, null);

        MTextView additionalchargeHTxt = (MTextView) dialogView.findViewById(R.id.additionalchargeHTxt);
        MTextView matrialfeeHTxt = (MTextView) dialogView.findViewById(R.id.matrialfeeHTxt);
        MTextView miscfeeHTxt = (MTextView) dialogView.findViewById(R.id.miscfeeHTxt);
        MTextView discountHTxt = (MTextView) dialogView.findViewById(R.id.discountHTxt);

        MTextView btnskip = (MTextView) dialogView.findViewById(R.id.btnskip);
        MTextView btnsubmit = (MTextView) dialogView.findViewById(R.id.btnsubmit);
        final MTextView finalvalTxt = (MTextView) dialogView.findViewById(R.id.finalvalTxt);
        MTextView finalHTxt = (MTextView) dialogView.findViewById(R.id.finalHTxt);

        MTextView currentchargeHTxt = (MTextView) dialogView.findViewById(R.id.currentchargeHTxt);
        MTextView currentchargeVTxt = (MTextView) dialogView.findViewById(R.id.currentchargeVTxt);

        calculateData("", finalvalTxt);


        ImageView imagecancel = (ImageView) dialogView.findViewById(R.id.imagecancel);


        final MaterialEditText timatrialfeeVTxt = (MaterialEditText) dialogView.findViewById(R.id.timatrialfeeVTxt);
        final MaterialEditText miscfeeVTxt = (MaterialEditText) dialogView.findViewById(R.id.miscfeeVTxt);
        final MaterialEditText discountVTxt = (MaterialEditText) dialogView.findViewById(R.id.discountVTxt);

        timatrialfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // timatrialfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        miscfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // miscfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        discountVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        // discountVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        discountVTxt.setShowClearButton(false);
        miscfeeVTxt.setShowClearButton(false);
        timatrialfeeVTxt.setShowClearButton(false);


        additionalchargeHTxt.setText(generalFunc.retrieveLangLBl("whould you like to addtional charge and discount ?", "LBL_ADDITONAL_CHARGE_HINT1"));
        matrialfeeHTxt.setText(generalFunc.retrieveLangLBl("Material fee", "LBL_MATERIAL_FEE"));
        miscfeeHTxt.setText(generalFunc.retrieveLangLBl("Misc fee", "LBL_MISC_FEE"));
        discountHTxt.setText(generalFunc.retrieveLangLBl("Provider Discount", "LBL_PROVIDER_DISCOUNT"));
        finalHTxt.setText(generalFunc.retrieveLangLBl("FINAL TOTAL", "LBL_FINAL_TOTAL_HINT"));
        btnskip.setText(generalFunc.retrieveLangLBl("SKIP", "LBL_SKIP"));
        btnsubmit.setText(generalFunc.retrieveLangLBl("SUBMIT", "LBL_SUBMIT"));
        currentchargeHTxt.setText(generalFunc.retrieveLangLBl("Current Charges", "LBL_CURREANT_HINT"));
        discountVTxt.setText("" + discount);
        miscfeeVTxt.setText("" + miscfee);
        timatrialfeeVTxt.setText("" + matrialfee);
        finalvalTxt.setText(generalFunc.getJsonValue("CurrencySymbol", generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON)) + " " + finaltotal);
        currentchargeVTxt.setText(generalFunc.getJsonValue("CurrencySymbol", generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON)) + " " + currencetprice);


        builder.setView(dialogView);
//        builder.setPositiveButton(generalFunc.retrieveLangLBl("cancel", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });

        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//            }
//        });
//
//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });

        imagecancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                defaultAddtionalprice();
                discountVTxt.setText("0.0");
                miscfeeVTxt.setText("0.0");
                timatrialfeeVTxt.setText("0.0");
                alertDialog.dismiss();

                if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                    if (isresume) {
                        callsetTimeApi(true);

                    }
                }

            }
        });
        btnskip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultAddtionalprice();

                discountVTxt.setText("0.0");
                miscfeeVTxt.setText("0.0");
                timatrialfeeVTxt.setText("0.0");
                endTrip();
            }
        });
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check data and pass date

                boolean discountVTxtenter = Utils.checkText(discountVTxt) ? true
                        : Utils.setErrorFields(discountVTxt, "");

                boolean miscfeeVTxtenter = Utils.checkText(miscfeeVTxt) ?
                        true
                        : Utils.setErrorFields(miscfeeVTxt, "");

                boolean timatrialfeeVTxtEnter = Utils.checkText(timatrialfeeVTxt) ?
                        true
                        : Utils.setErrorFields(timatrialfeeVTxt, "");

                if (discountVTxtenter == false || miscfeeVTxtenter == false || timatrialfeeVTxtEnter == false) {
                    return;
                }


                endTrip();
            }
        });

        timatrialfeeVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    matrialfee = 0.0;
                    matrialfee = Double.parseDouble(s.toString());
                    additonallist.remove(0);
                    additonallist.add(0, matrialfee);
                    calculateData(s.toString(), finalvalTxt);
                    // finalvalTxt.setText("$ "+finaltotal);
                } else {
                    additonallist.remove(0);
                    additonallist.add(0, 0.0);
                    calculateData(s.toString(), finalvalTxt);
                }

            }
        });
        // timatrialfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        miscfeeVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    miscfee = 0.0;
                    miscfee = Double.parseDouble(s.toString());
                    additonallist.remove(1);
                    additonallist.add(1, miscfee);
                    calculateData(s.toString(), finalvalTxt);
                    // finaltotal+=miscfee;
                    //finalvalTxt.setText("$ "+finaltotal);
                } else {
                    additonallist.remove(1);
                    additonallist.add(1, 0.0);
                    calculateData(s.toString(), finalvalTxt);
                }

            }
        });
        // miscfeeVTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);

        discountVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    discount = 0.0;
                    discount = Double.parseDouble(s.toString());
                    additonallist.remove(2);
                    additonallist.add(2, discount);
                    calculateData(s.toString(), finalvalTxt);
                    //  finaltotal+=discount;
                    // finalvalTxt.setText("$ "+finaltotal);
                } else {
                    additonallist.remove(2);
                    additonallist.add(2, 0.0);
                    calculateData(s.toString(), finalvalTxt);

                }


            }
        });


//


    }

    private void calculateData(String s, MTextView finalvalTxt) {
        finaltotal = 0.0;
        finaltotal = Double.parseDouble(currencetprice) + Double.parseDouble(additonallist.get(0).toString()) + Double.parseDouble(additonallist.get(1).toString()) - Double.parseDouble(additonallist.get(2).toString());
        finalvalTxt.setText(generalFunc.getJsonValue("CurrencySymbol", generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON)) + " " + String.format("%.2f", finaltotal));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_passenger_detail:


                new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, false);


                return true;

            case R.id.menu_cancel_trip:
                new CancelTripDialog(getActContext(), data_trip, generalFunc, isTripStart);
                return true;

            case R.id.menu_waybill_trip:
                new StartActProcess(getActContext()).startAct(WayBillActivity.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void registerTripMsgReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonUtilities.passenger_message_arrived_intent_action_trip_msg);

        registerReceiver(tripMsgReceiver, filter);

        if (isPubNubEnabled()) {
            configPubNub = new ConfigPubNub(getActContext());
        }
    }

    public Context getActContext() {
        return ActiveTripActivity.this; // Must be context of activity not application
    }

    public void addDestination(final String latitude, final String longitude, final String address) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addDestination");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("Latitude", latitude);
        parameters.put("Longitude", longitude);
        parameters.put("Address", address);
        parameters.put("UserId", data_trip.get("PassengerId"));
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        setDestinationPoint(latitude, longitude, address, true);

                        if (destLocMarker == null) {
                            addDestinationMarker();
                        }
                        drawRoute(latitude, longitude);
                    } else {

                        String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        if (msg_str.equals(CommonUtilities.GCM_FAILED_KEY) || msg_str.equals(CommonUtilities.APNS_FAILED_KEY)) {
                            generalFunc.restartApp();
                        } else {
                            generalFunc.showGeneralMessage("",
                                    generalFunc.retrieveLangLBl("", msg_str));
                        }

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void setDestinationPoint(String latitude, String longitude, String address, boolean isDestinationAdded) {
        double dest_lat = generalFunc.parseDoubleValue(0.0, latitude);
        double dest_lon = generalFunc.parseDoubleValue(0.0, longitude);

        (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);
        (findViewById(R.id.navigationViewArea)).setVisibility(View.VISIBLE);
        try {
            if (data_trip.get("eTollSkipped").equalsIgnoreCase("yes")) {
                tollTxtView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {

        }

        if (address.equals("")) {
            addressTxt.setText(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"));
            GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
            getAddressFromLocation.setLocation(dest_lat, dest_lon);
            getAddressFromLocation.setAddressList(new GetAddressFromLocation.AddressFound() {
                @Override
                public void onAddressFound(String address, double latitude, double longitude) {
                    addressTxt.setText(address);
                }
            });
            getAddressFromLocation.execute();
        } else {
            addressTxt.setText(address);
        }

        (findViewById(R.id.navigateArea)).setOnClickListener(new setOnClickAct("" + dest_lat, "" + dest_lon));

        this.isDestinationAdded = isDestinationAdded;
        this.destLocLatitude = dest_lat;
        this.destLocLongitude = dest_lon;
    }

    public void setTripStart() {

        if (!TextUtils.isEmpty(isFrom) && imageType.equalsIgnoreCase("before")) {

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("type", "StartTrip"));
            paramsList.add(generalFunc.generateImageParams("iDriverId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("TripID", tripId));
            paramsList.add(generalFunc.generateImageParams("iUserId", data_trip.get("PassengerId")));
            paramsList.add(generalFunc.generateImageParams("UserType", CommonUtilities.app_type));

            Utils.printLog("Api", "selectedImagePath" + selectedImagePath);

            if (isFrom.equalsIgnoreCase("Gallary")) {
                new UploadProfileImage(ActiveTripActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList, imageType).execute();
            } else if (isFrom.equalsIgnoreCase("Camera")) {
                new UploadProfileImage(ActiveTripActivity.this, fileUri.getPath(), Utils.TempProfileImageName, paramsList, imageType).execute();
            }
        } else {


            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "StartTrip");
            parameters.put("iDriverId", generalFunc.getMemberId());
            parameters.put("TripID", tripId);
            parameters.put("iUserId", data_trip.get("PassengerId"));
            parameters.put("UserType", CommonUtilities.app_type);


            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
                @Override
                public void setResponse(String responseString) {
                    startTripResponse(responseString);
                }
            });
            exeWebServer.execute();
        }
    }

    private void startTripResponse(String responseString) {


        Utils.printLog("Response", "::" + responseString);

        if (responseString != null && !responseString.equals("")) {

            if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                getTripDeliveryLocations();

            } else {
                try {
                    if (data_trip.get("eFareType") != null && !data_trip.get("eFareType").equals("")) {
                        if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {

                            getTripDeliveryLocations();
                        } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                            getTripDeliveryLocations();

                        }
                    }
                } catch (Exception e) {

                }
            }

            boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

            if (isDataAvail == true) {
                closeuploadServicePicAlertBox();

                currencetprice = generalFunc.getJsonValue("fVisitFee", responseString);
                if (REQUEST_TYPE.equals("Deliver")) {
                    SITE_TYPE = generalFunc.getJsonValue("SITE_TYPE", responseString);
                    deliveryVerificationCode = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                }
                if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                    callsetTimeApi(true);
                }
                configTripStartView();
                // countDownStart();
            } else {
                String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                if (msg_str.equals(CommonUtilities.GCM_FAILED_KEY) || msg_str.equals(CommonUtilities.APNS_FAILED_KEY)) {
                    generalFunc.restartApp();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", msg_str));
                }

            }
        } else {
            generalFunc.showError();
        }

    }

    public void configTripStartView() {

        isresume = true;
        btntimer.setVisibility(View.VISIBLE);
        //countDownStart();

        isTripStart = true;
        tripStartBtnArea.setVisibility(View.GONE);
        tripEndBtnArea.setVisibility(View.VISIBLE);
        isendslide = true;
        invalidateOptionsMenu();
        imageslide.setImageResource(R.mipmap.ic_trip_btn);

        startService(tripLocService_intent);
        bindService();
    }

    public void bindService() {
        bindService(tripLocService_intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void cancelTrip(String reason, String comment) {
        isTripCancelPressed = true;
        this.reason = reason;
        this.comment = comment;

        if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX") && generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
            takeAndUploadPic(getActContext(), "after");
        } else {
            if (eType.equals("UberX")) {
                getCurrentPriceApi();

            } else {
                endTrip();
            }
        }
        //endTrip();
    }

    public void endTrip() {
        if (userLocation == null) {
            generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_NO_LOCATION_FOUND_TXT"));
            return;
        }

        ArrayList<LatLng> store_locations = new ArrayList<>();
        ArrayList<String> store_locations_latitude = new ArrayList<String>();
        ArrayList<String> store_locations_longitude = new ArrayList<String>();

        if (tripLocService != null) {
            tripLocService.endTrip();
            store_locations = tripLocService.getListOfLocations();
        }

        if (store_locations.size() > 0) {

            for (int i = 0; i < store_locations.size(); i++) {

                LatLng locations = store_locations.get(i);

                double latitude = locations.latitude;
                double longitude = locations.longitude;

                store_locations_latitude.add("" + latitude);
                store_locations_longitude.add("" + longitude);
            }

        }

        getDestinationAddress(store_locations_latitude, store_locations_longitude, "" + userLocation.getLatitude(), "" + userLocation.getLongitude());

    }

    public void getCurrentPriceApi() {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "displaytripcharges");
        parameters.put("TripID", tripId);

        if (TripTimeId != null && !TripTimeId.equals("")) {
            if (isresume) {
                parameters.put("iTripTimeId", TripTimeId);
            }
        }
        if (userLocation != null) {
            parameters.put("dest_lat", userLocation.getLatitude() + "");
            parameters.put("dest_lon", userLocation.getLongitude() + "");
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                if (responseString != null && !responseString.equals("")) {


                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        currencetprice = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        showAddtionalChargeBox();
                        if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                            if (isresume) {
                                countDownStop();
                            }
                        }

                    } else {

                    }
                } else {
                    generalFunc.showError();
                }

            }
        });
        exeWebServer.execute();
    }

    public void getDestinationAddress(final ArrayList<String> store_locations_latitude, final ArrayList<String> store_locations_longitude,
                                      String endLatitude, String endLongitude) {


        final MyProgressDialog myPDialog = showLoader();

        GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setLocation(generalFunc.parseDoubleValue(0.0, endLatitude), generalFunc.parseDoubleValue(0.0, endLongitude));
        getAddressFromLocation.setIsDestination(true);
        getAddressFromLocation.setAddressList(new GetAddressFromLocation.AddressFound() {
            @Override
            public void onAddressFound(String address, double latitude, double longitude) {

                closeLoader(myPDialog);

                if (address.equals("")) {
                    generalFunc.showError();
                } else {
                    setTripEnd(store_locations_latitude, store_locations_longitude,
                            "" + userLocation.getLatitude(), "" + userLocation.getLongitude(), address);
                }
            }
        });
        getAddressFromLocation.execute();
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void closeLoader(MyProgressDialog myPDialog) {
        myPDialog.close();
    }

    public void setTripEnd(ArrayList<String> store_locations_latitude, ArrayList<String> store_locations_longitude, String endLatitude, String endLongitude, String destAddress) {

        if (!TextUtils.isEmpty(isFrom) && imageType.equalsIgnoreCase("after")) {

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("type", "ProcessEndTrip"));
            paramsList.add(generalFunc.generateImageParams("TripId", tripId));
            paramsList.add(generalFunc.generateImageParams("latList", store_locations_latitude.toString().replace("[", "").replace("]", "")));
            paramsList.add(generalFunc.generateImageParams("lonList", store_locations_longitude.toString().replace("[", "").replace("]", "")));
            paramsList.add(generalFunc.generateImageParams("PassengerId", data_trip.get("PassengerId")));
            paramsList.add(generalFunc.generateImageParams("DriverId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("dAddress", destAddress));
            paramsList.add(generalFunc.generateImageParams("dest_lat", endLatitude));
            paramsList.add(generalFunc.generateImageParams("dest_lon", endLongitude));
            paramsList.add(generalFunc.generateImageParams("waitingTime", "" + getWaitingTime()));
            paramsList.add(generalFunc.generateImageParams("fMaterialFee", additonallist.get(0).toString()));
            paramsList.add(generalFunc.generateImageParams("fMiscFee", additonallist.get(1).toString()));
            paramsList.add(generalFunc.generateImageParams("fDriverDiscount", additonallist.get(2).toString()));
            if (isTripCancelPressed == true) {
                paramsList.add(generalFunc.generateImageParams("isTripCanceled", "true"));
                paramsList.add(generalFunc.generateImageParams("Comment", comment));
                paramsList.add(generalFunc.generateImageParams("Reason", reason));
            }
            if (isFrom.equalsIgnoreCase("Gallary")) {
                new UploadProfileImage(ActiveTripActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList, imageType).execute();
            } else if (isFrom.equalsIgnoreCase("Camera")) {
                new UploadProfileImage(ActiveTripActivity.this, fileUri.getPath(), Utils.TempProfileImageName, paramsList, imageType).execute();
            }
        } else {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "ProcessEndTrip");
            parameters.put("TripId", tripId);
            parameters.put("latList", store_locations_latitude.toString().replace("[", "").replace("]", ""));
            parameters.put("lonList", store_locations_longitude.toString().replace("[", "").replace("]", ""));
            parameters.put("PassengerId", data_trip.get("PassengerId"));
            parameters.put("DriverId", generalFunc.getMemberId());
            parameters.put("dAddress", destAddress);
            parameters.put("dest_lat", endLatitude);
            parameters.put("dest_lon", endLongitude);
            parameters.put("waitingTime", "" + getWaitingTime());

            parameters.put("fMaterialFee", additonallist.get(0).toString());
            parameters.put("fMiscFee", additonallist.get(1).toString());
            parameters.put("fDriverDiscount", additonallist.get(2).toString());


            if (isTripCancelPressed == true) {
                parameters.put("isTripCanceled", "true");
                parameters.put("Comment", comment);
                parameters.put("Reason", reason);
            }

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
                @Override
                public void setResponse(String responseString) {
                    endTripResponse(responseString);
                }
            });
            exeWebServer.execute();
        }
    }

    private void endTripResponse(String responseString) {

        Utils.printLog("Response", "::" + responseString);

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

            if (isDataAvail == true) {
                if (timerrequesttask != null) {
                    try {
                        timerrequesttask.stopRepeatingTask();
                    } catch (Exception e) {

                    }
                }
                closeuploadServicePicAlertBox();
                stopProcess();

                if (updateDirections != null) {
                    updateDirections.releaseTask();
                    updateDirections = null;
                }

                if (updatepubnubtask != null) {
                    if (pubNub != null) {
                        pubNub.unSubscribeToPrivateChannel();
                    }
                    updatepubnubtask.stopRepeatingTask();
                    updatepubnubtask = null;
                }
                stopPubNub();

                Bundle bn = new Bundle();
                bn.putSerializable("TRIP_DATA", data_trip);
                new StartActProcess(getActContext()).startActWithData(CollectPaymentActivity.class, bn);


                ActivityCompat.finishAffinity(ActiveTripActivity.this);


            } else {

                String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                if (msg_str.equals(CommonUtilities.GCM_FAILED_KEY) || msg_str.equals(CommonUtilities.APNS_FAILED_KEY)) {
                    generalFunc.restartApp();
                } else {
                    if (tripLocService != null) {
                        tripLocService.tripEndRevoked();
                    }
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                }

            }
        } else {
            if (tripLocService != null) {
                tripLocService.tripEndRevoked();
            }
            generalFunc.showError();
        }
    }

    private long getWaitingTime() {
        long waitingTime = generalFunc.parseLongValue(0, generalFunc.retrieveValue(CommonUtilities.DriverWaitingTime)) / 60000;

        return waitingTime;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (mServiceBound == false && isTripStart == true) {
            bindService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    public void stopProcess() {
        if (mServiceBound) {
            unbindService(mConnection);
            mServiceBound = false;
        }

        unRegisterReceiver();
        stopDriverLocationUpdateService();
    }

    public void unRegisterReceiver() {
        if (tripMsgReceiver != null) {
            try {
                unregisterReceiver(tripMsgReceiver);
            } catch (Exception e) {

            }
        }
    }

    public void stopDriverLocationUpdateService() {
        try {
            stopService(startLocationUpdateService);
            stopService(tripLocService_intent);
        } catch (Exception e) {

        }
    }

    public void stopPubNub() {
        if (configPubNub != null) {
            configPubNub.unSubscribeToPrivateChannel();
            configPubNub = null;
            Utils.runGC();
        }
    }

    @Override
    protected void onDestroy() {
        stopProcess();
        stopPubNub();
        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        if (updatepubnubtask != null) {
            if (pubNub != null) {
                pubNub.unSubscribeToPrivateChannel();
            }
            updatepubnubtask.stopRepeatingTask();
            updatepubnubtask = null;
        }
        super.onDestroy();
    }

    public void takeAndUploadPic(final Context mContext, final String picType) {
        imageType = picType;
        isFrom = "";
        selectedImagePath = "";

        uploadServicePicAlertBox = new Dialog(mContext, R.style.Theme_Dialog);
        uploadServicePicAlertBox.requestWindowFeature(Window.FEATURE_NO_TITLE);

        uploadServicePicAlertBox.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        uploadServicePicAlertBox.setContentView(R.layout.design_upload_service_pic);

        MTextView titleTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.titleTxt);
        final MTextView uploadStatusTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.uploadStatusTxt);
        MTextView uploadTitleTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.uploadTitleTxt);
        ImageView backImgView = (ImageView) uploadServicePicAlertBox.findViewById(R.id.backImgView);
        MTextView skipTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.skipTxt);
        final ImageView uploadImgVIew = (ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew);
        LinearLayout uploadImgArea = (LinearLayout) uploadServicePicAlertBox.findViewById(R.id.uploadImgArea);
        MButton btn_type2 = ((MaterialRippleLayout) uploadServicePicAlertBox.findViewById(R.id.btn_type2)).getChildView();

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_UPLOAD_IMAGE_SERVICE"));
        skipTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        if (picType.equalsIgnoreCase("before")) {
            uploadTitleTxt.setText(generalFunc.retrieveLangLBl("Click and upload photo of your car before your service", "LBL_UPLOAD_SERVICE_BEFORE_TXT"));
            btn_type2.setText(generalFunc.retrieveLangLBl("Save Photo", "LBL_SAVE_PHOTO_START_SERVICE_TXT"));
        } else {
            uploadTitleTxt.setText(generalFunc.retrieveLangLBl("Click and upload photo of your car after your service", "LBL_UPLOAD_SERVICE_AFTER_TXT"));
            btn_type2.setText(generalFunc.retrieveLangLBl("Save Photo", "LBL_SAVE_PHOTO_END_SERVICE_TXT"));
        }

        btn_type2.setId(Utils.generateViewId());

        uploadImgArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (generalFunc.isCameraPermissionGranted()) {
                    uploadServicePicAlertBox.findViewById(R.id.uploadStatusTxt).setVisibility(View.GONE);
                    new ImageSourceDialog().run();
                } else {
                    uploadStatusTxt.setVisibility(View.VISIBLE);
                    generalFunc.showMessage(uploadStatusTxt, "Allow this app to use camera.");
                }
            }
        });
        btn_type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(selectedImagePath)) {
                    uploadStatusTxt.setVisibility(View.VISIBLE);
                    generalFunc.showMessage(uploadStatusTxt, "Please select image");

                } else if (picType.equalsIgnoreCase("after")) {
                    uploadStatusTxt.setVisibility(View.GONE);
                    //showAddtionalChargeBox();
                    if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
                        getCurrentPriceApi();
                    } else {
                        endTrip();
                    }
                    //endTrip();
                } else {
                    uploadStatusTxt.setVisibility(View.GONE);
                    setTripStart();
                }
            }
        });

        skipTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isFrom = "";
                selectedImagePath = "";
                uploadImgVIew.setImageURI(null);

                if (picType.equalsIgnoreCase("after")) {
                    // endTrip();
                    //showAddtionalChargeBox();
                    if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
                        getCurrentPriceApi();
                    } else {
                        endTrip();
                    }
                } else {
                    setTripStart();
                }

            }
        });
        backImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeuploadServicePicAlertBox();
            }
        });

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(uploadServicePicAlertBox);
        }

        uploadServicePicAlertBox.show();

    }

//    Upload Service Pic

    public void chooseFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

//    OVER UPLOAD SERVICE PIC AREA

    public String getPath(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        try {
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                return filePath;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Utils.printLog(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void chooseFromGallery() {
        // System.out.println("Gallery pressed");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void closeuploadServicePicAlertBox() {
        if (uploadServicePicAlertBox != null) {
            uploadServicePicAlertBox.dismiss();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean isStoragePermissionAvail = generalFunc.isCameraStoragePermissionGranted();

        if (requestCode == Utils.SEARCH_DEST_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            addDestination(data.getStringExtra("Latitude"), data.getStringExtra("Longitude"), data.getStringExtra("Address"));
        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {

            // successfully captured the image
            // display it in image view

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("iMemberId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("MemberType", CommonUtilities.app_type));
            paramsList.add(generalFunc.generateImageParams("type", "uploadImage"));

            if (isStoragePermissionAvail) {

                isFrom = "Camera";
                if (fileUri != null && uploadServicePicAlertBox != null) {
                    selectedImagePath = ImageFilePath.getPath(getApplicationContext(), fileUri);

                    ((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)).setImageURI(fileUri);
                    uploadServicePicAlertBox.findViewById(R.id.camImgVIew).setVisibility(View.GONE);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {

        } else {
            generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_FAILED_CAPTURE_IMAGE_TXT"));
        }
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("iMemberId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("type", "uploadImage"));
            paramsList.add(generalFunc.generateImageParams("MemberType", CommonUtilities.app_type));

            Uri selectedImageUri = data.getData();

            selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);

            if (isStoragePermissionAvail) {
                isFrom = "Gallary";
                if (selectedImageUri != null && uploadServicePicAlertBox != null) {
                    ((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)).setImageURI(selectedImageUri);
                    uploadServicePicAlertBox.findViewById(R.id.camImgVIew).setVisibility(View.GONE);
                }
            }
        }
    }

    public void handleImgUploadResponse(String responseString, String imageUploadedType) {

        Utils.printLog("responseString", "::" + responseString);
        if (responseString != null && !responseString.equals("")) {

            if (imageType.equalsIgnoreCase("after")) {
                endTripResponse(responseString);
            } else if (imageType.equalsIgnoreCase(imageUploadedType)) {
                startTripResponse(responseString);
            }
        } else {
            generalFunc.showError();
        }
    }

    public void countDownStop() {
        if (timerrequesttask != null) {
            callsetTimeApi(false);

        }

    }

    public void countDownStart() {

        if (timerrequesttask != null) {
            timerrequesttask.stopRepeatingTask();
            timerrequesttask = null;
        }
        timerrequesttask = new UpdateFrequentTask(1000);
        timerrequesttask.startRepeatingTask();
        timerrequesttask.setTaskRunListener(new UpdateFrequentTask.OnTaskRunCalled() {
            @Override
            public void onTaskRun() {
                i++;

//                hours = totalSecs / 3600;
                //   minutes = (i % 3600) / 60;
//                seconds = totalSecs % 60;
                tvHour.setText("" + String.format("%02d", i / 3600));
                tvMinute.setText("" + String.format("%02d", (i % 3600) / 60));
                tvSecond.setText("" + String.format("%02d", i % 60));


            }
        });

    }

    private void setView() {

    }

    private void callsetTimeApi(final boolean isresume) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "SetTimeForTrips");
        parameters.put("iTripId", tripId);
        if (!isresume) {
            parameters.put("iTripTimeId", TripTimeId);
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {


                boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                if (isDataAvail == true) {
                    String msg_str = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                    if (!msg_str.equals("true") && !msg_str.equals("") && msg_str != null) {
                        TripTimeId = msg_str;
                    }
                    String temptime = generalFunc.getJsonValue("totalTime", responseString);
                    i = Integer.parseInt(temptime);

                    if (isresume) {
                        countDownStart();
                        btntimer.setText("pause");
                    } else {
                        if (timerrequesttask != null) {
                            timerrequesttask.stopRepeatingTask();
                        }
                    }

                }
            }
        });
        exeWebServer.execute();
    }

    public class setOnClickAct implements View.OnClickListener {

        String dest_lat = "";
        String dest_lon = "";

        public setOnClickAct() {
        }


        public setOnClickAct(String dest_lat, String dest_lon) {
            this.dest_lat = dest_lat;
            this.dest_lon = dest_lon;
        }

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.navigateArea) {
//                String url_view = "http://maps.google.com/maps?daddr=" + dest_lat + "," + dest_lon;
//                (new StartActProcess(getActContext())).openURL(url_view, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                openNavigationDialog(dest_lat, dest_lon);
            } else if (i == R.id.destLocSearchArea) {
                Bundle bn = new Bundle();
                bn.putString("isPickUpLoc", "false");

                if (userLocation != null) {
                    bn.putString("PickUpLatitude", "" + userLocation.getLatitude());
                    bn.putString("PickUpLongitude", "" + userLocation.getLongitude());
                }
                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.SEARCH_DEST_LOC_REQ_CODE);
            } else if (i == tripStartBtnArea.getId()) {
                imageType = "before";

                if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX") && generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                    takeAndUploadPic(getActContext(), "before");
                } else {
                    setTripStart();
                }


            } else if (i == tripEndBtnArea.getId()) {
                imageType = "after";

                isTripCancelPressed = false;
                reason = "";
                comment = "";

                if (REQUEST_TYPE.equals("Deliver")) {
                    buildMsgOnDeliveryEnd();
                } else {
                    if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX") && generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                        takeAndUploadPic(getActContext(), "after");
                    } else {
                        // endTrip();
                        //showAddtionalChargeBox();
                        if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
                            getCurrentPriceApi();
                        } else {
                            getCurrentPriceApi();
                        }
                    }
                }


            } else if (i == btntimer.getId()) {

                if (btntimer.getText().toString().equalsIgnoreCase("resume")) {
                    callsetTimeApi(true);
                    //  countDownStart();
                    btntimer.setText("pause");
                    isresume = true;

                } else if (btntimer.getText().toString().equalsIgnoreCase("pause")) {
                    countDownStop();
                    btntimer.setText("resume");
                    isresume = false;
                }


            }
        }
    }

    public void openNavigationDialog(final String dest_lat, final String dest_lon) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectnavigation_view, null);

        final MTextView NavigationTitleTxt = (MTextView) dialogView.findViewById(R.id.NavigationTitleTxt);
        final MTextView wazemapTxtView = (MTextView) dialogView.findViewById(R.id.wazemapTxtView);
        final MTextView googlemmapTxtView = (MTextView) dialogView.findViewById(R.id.googlemmapTxtView);
        final RadioButton radiogmap = (RadioButton) dialogView.findViewById(R.id.radiogmap);
        final RadioButton radiowazemap = (RadioButton) dialogView.findViewById(R.id.radiowazemap);

        radiogmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googlemmapTxtView.performClick();
            }
        });
        radiowazemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wazemapTxtView.performClick();
            }
        });

        builder.setView(dialogView);
        NavigationTitleTxt.setText(generalFunc.retrieveLangLBl("Choose Option", "LBL_CHOOSE_OPTION"));
        googlemmapTxtView.setText(generalFunc.retrieveLangLBl("Google map navigation", "LBL_NAVIGATION_GOOGLE_MAP"));
        wazemapTxtView.setText(generalFunc.retrieveLangLBl("Waze navigation", "LBL_NAVIGATION_WAZE"));


        googlemmapTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String url_view = "http://maps.google.com/maps?daddr=" + dest_lat + "," + dest_lon;
                    (new StartActProcess(getActContext())).openURL(url_view, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    list_navigation.dismiss();
                } catch (Exception e) {
                    generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Google Maps in your device.", "LBL_INSTALL_GOOGLE_MAPS"));
                }

            }
        });

        wazemapTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String uri = "waze://?ll=" + dest_lat + "," + dest_lon + "&navigate=yes";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                    list_navigation.dismiss();
                } catch (Exception e) {

                    generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Waze navigation app in your device.", "LBL_INSTALL_WAZE"));


                }


            }
        });


        list_navigation = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_navigation);
        }
        list_navigation.show();
        list_navigation.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Utils.hideKeyboard(getActContext());
            }
        });
    }

    public class setOnTouchList implements View.OnTouchListener {
        float x1, x2, y1, y2, startX, movedX;

        DisplayMetrics display = getResources().getDisplayMetrics();

        final int width = display.widthPixels;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Utils.printLog("onTouch", "called");
            switch (event.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
                    x1 = event.getX();
                    y1 = event.getY();

                    startX = event.getRawX();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    x2 = event.getX();
                    y2 = event.getY();
                    movedX = event.getRawX() - startX;

                    if (movedX > width / 2) {

                        if (x1 < x2) {

                            isTripCancelPressed = false;

                            if (view.getId() == tripStartBtnArea.getId()) {
                                // Trip start btn called

//                                takeAndUploadPic(getActContext(), "before");
//                                setTripStart();
                                if (generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                                    takeAndUploadPic(getActContext(), "before");
                                } else {
                                    setTripStart();
                                }
                            } else if (view.getId() == tripEndBtnArea.getId()) {
                                // Trip end btn called

                                if (REQUEST_TYPE.equals("Deliver")) {
                                    buildMsgOnDeliveryEnd();
                                } else {

                                    if (generalFunc.retrieveValue(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                                        takeAndUploadPic(getActContext(), "after");
                                    } else {
                                        //showAddtionalChargeBox();
                                        // endTrip();
                                        Utils.printLog("AppType", generalFunc.retrieveValue(APP_TYPE));
                                        Utils.printLog("etype", eType);
                                        if (generalFunc.retrieveValue(APP_TYPE).equalsIgnoreCase("UberX")) {
                                            getCurrentPriceApi();
                                        } else {
                                            if (!eType.equals("")) {
                                                if (eType.equals("UberX")) {
                                                    getCurrentPriceApi();
                                                } else {
                                                    endTrip();
                                                }

                                            } else {
                                                endTrip();
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    break;
                }
            }
            return false;
        }
    }

    class ImageSourceDialog implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            final Dialog dialog_img_update = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);

            dialog_img_update.setContentView(R.layout.design_image_source_select);

            MTextView chooseImgHTxt = (MTextView) dialog_img_update.findViewById(R.id.chooseImgHTxt);
            chooseImgHTxt.setText(generalFunc.retrieveLangLBl("Choose option", "LBL_CHOOSE_OPTION"));

            SelectableRoundedImageView cameraIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.cameraIconImgView);
            SelectableRoundedImageView galleryIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.galleryIconImgView);

            ImageView closeDialogImgView = (ImageView) dialog_img_update.findViewById(R.id.closeDialogImgView);

            closeDialogImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }
                }
            });

            new CreateRoundedView(getResources().getColor(R.color.appThemeColor_Dark_1), Utils.dipToPixels(getActContext(), 25), 0,
                    Color.parseColor("#00000000"), cameraIconImgView);

            cameraIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));

            new CreateRoundedView(getResources().getColor(R.color.appThemeColor_Dark_1), Utils.dipToPixels(getActContext(), 25), 0,
                    Color.parseColor("#00000000"), galleryIconImgView);

            galleryIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));


            cameraIconImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }

                    if (!isDeviceSupportCamera()) {
                        generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_NOT_SUPPORT_CAMERA_TXT"));
                    } else {
                        chooseFromCamera();
                    }

                }
            });

            galleryIconImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }
                    chooseFromGallery();
                }
            });

            dialog_img_update.setCanceledOnTouchOutside(true);

            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);

            window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.show();

        }

    }


}
