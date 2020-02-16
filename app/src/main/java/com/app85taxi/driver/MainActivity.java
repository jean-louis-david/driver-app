package com.app85taxi.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.adapter.files.DrawerAdapter;
import com.adapter.files.ManageVehicleListAdapter;
import com.fragments.InactiveFragment;
import com.general.files.BackgroundAppReceiver;
import com.general.files.ConfigPubNub;
import com.general.files.DividerItemDecoration;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GcmBroadCastReceiver;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UpdateDriverStatus;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.kyleduo.switchbutton.SwitchButton;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.app85taxi.driver.R.id.hileImageview;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, OnMapReadyCallback, GetLocationUpdates.LocationUpdates, GoogleMap.OnCameraChangeListener, UpdateFrequentTask.OnTaskRunCalled,
        ManageVehicleListAdapter.OnItemClickList, GetLocationUpdates.LastLocationListner {

    public GeneralFunctions generalFunc;
    public DrawerLayout mDrawerLayout;
    public String userProfileJson = "";
    public Location userLocation;
    MTextView titleTxt;
    ImageView menuImgView;
    ListView menuListView;
    DrawerAdapter drawerAdapter;
    ArrayList<String[]> list_menu_items;
    SupportMapFragment map;
    GoogleMap gMap;
    GetLocationUpdates getLastLocation;
    boolean isFirstLocation = true;
    ImageView userLocBtnImgView;
    ImageView userHeatmapBtnImgView;

    MTextView onlineOfflineTxtView;
    MTextView carNumPlateTxt;
    MTextView carNameTxt;
    MTextView changeCarTxt;
    SwitchButton onlineOfflineSwitch;

    boolean isDriverOnline = false;

    Intent startUpdatingStatus;

    ArrayList<String> items_txt_car = new ArrayList<String>();
    ArrayList<String> items_txt_car_json = new ArrayList<String>();
    ArrayList<String> items_car_id = new ArrayList<String>();

    android.support.v7.app.AlertDialog list_car;
    android.support.v7.app.AlertDialog no_loaction;
    android.support.v7.app.AlertDialog gender;

    boolean isOnlineAvoid = false;

    GcmBroadCastReceiver gcmBroadCastReceiver;

    ConfigPubNub pubNub;
    String assignedTripId = "";
    String ENABLE_HAIL_RIDES = "";

    // Heat Map Data

    ExecuteWebServerUrl heatMapAsyncTask;
    HashMap<String, String> onlinePassengerLocList = new HashMap<String, String>();
    HashMap<String, String> historyLocList = new HashMap<String, String>();
    ArrayList<TileOverlay> mapOverlayList = new ArrayList<>();

    double radius_map = 0;

    Boolean isShowNearByPassengers = false;

    String app_type = "Ride";
    int currentRequestPositions = 0;
    UpdateFrequentTask updateRequest;
    UpdateFrequentTask updatepubnubtask;
    BackgroundAppReceiver bgAppReceiver;

    boolean isCurrentReqHandled = false;

    LinearLayout left_linear;

    ImageView imgSetting;

    LinearLayout logoutarea;
    ImageView logoutimage;
    MTextView logoutTxt;
    public String selectedcar = "";

    LinearLayout mapbottomviewarea;
    RelativeLayout mapviewarea;
    boolean iswallet = false;

    SelectableRoundedImageView hileimagview;

    InternetConnection intCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalFunc = new GeneralFunctions(getActContext());
        intCheck = new InternetConnection(this);

        userProfileJson = getIntent().getStringExtra("USER_PROFILE_JSON");
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        menuImgView = (ImageView) findViewById(R.id.menuImgView);
        menuListView = (ListView) findViewById(R.id.menuListView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        userHeatmapBtnImgView = (ImageView) findViewById(R.id.userHeatmapBtnImgView);

        hileimagview = (SelectableRoundedImageView) findViewById(hileImageview);
        hileimagview.setOnClickListener(new setOnClickList());

        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 35), 2,
                getActContext().getResources().getColor(R.color.appThemeColor_1), hileimagview);

        hileimagview.setColorFilter(getActContext().getResources().getColor(R.color.white));


        mapviewarea = (RelativeLayout) findViewById(R.id.mapviewarea);
        mapbottomviewarea = (LinearLayout) findViewById(R.id.mapbottomviewarea);


        logoutarea = (LinearLayout) findViewById(R.id.logoutarea);
        logoutimage = (ImageView) findViewById(R.id.logoutimage);
        logoutTxt = (MTextView) findViewById(R.id.logoutTxt);
        logoutTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SIGNOUT_TXT"));
        logoutarea.setOnClickListener(new setOnClickList());

        left_linear = (LinearLayout) findViewById(R.id.left_linear);

        onlineOfflineTxtView = (MTextView) findViewById(R.id.onlineOfflineTxtView);
        carNumPlateTxt = (MTextView) findViewById(R.id.carNumPlateTxt);
        carNameTxt = (MTextView) findViewById(R.id.carNameTxt);
        changeCarTxt = (MTextView) findViewById(R.id.changeCarTxt);
        onlineOfflineSwitch = (SwitchButton) findViewById(R.id.onlineOfflineSwitch);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        imgSetting = (ImageView) findViewById(R.id.imgSetting);
        imgSetting.setOnClickListener(new setOnClickList());

        startUpdatingStatus = new Intent(getApplicationContext(), UpdateDriverStatus.class);
        bgAppReceiver = new BackgroundAppReceiver(getActContext());

        getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true);
        getLastLocation.setLocationUpdatesListener(this);

        android.view.Display display = ((android.view.WindowManager) getActContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        left_linear.getLayoutParams().width = display.getWidth() * 75 / 100;
        left_linear.requestLayout();


        if (isPubNubEnabled()) {
            pubNub = new ConfigPubNub(getActContext());
        }


        setGeneralData();

        buildMenu();

        setUserInfo();

        map.getMapAsync(MainActivity.this);

        menuImgView.setOnClickListener(new setOnClickList());
        changeCarTxt.setOnClickListener(new setOnClickList());

        userLocBtnImgView.setOnClickListener(new setOnClickList());
        userHeatmapBtnImgView.setOnClickListener(new setOnClickList());

        onlineOfflineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                if (b == true) {
                    onlineOfflineSwitch.setThumbColorRes(R.color.Green);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);

                } else {
                    onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);
                }

                if (isOnlineAvoid == true) {
                    isOnlineAvoid = false;
                    return;
                }

                goOnlineOffline(b, true);
                MainActivity.super.onResume();
            }
        });


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }
        generalFunc.storedata(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", userProfileJson);
        Utils.printLog("DataReq", "::" + arr_CurrentRequests.toString());
        if (arr_CurrentRequests.length() > 0) {

            registerBroadCastReceiver();

            updateRequest = new UpdateFrequentTask(5 * 1000);
//            this.updateRequest = updateRequest;
            updateRequest.setTaskRunListener(this);
            updateRequest.startRepeatingTask();
        } else {
            removeOldRequestsCode();
            isCurrentReqHandled = true;
        }

        registerBackgroundAppReceiver();

        if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("UberX")) {
            changeCarTxt.setText(generalFunc.getJsonValue("vName", userProfileJson) + " "
                    + generalFunc.getJsonValue("vLastName", userProfileJson));
            changeCarTxt.setOnClickListener(null);

            carNumPlateTxt.setVisibility(View.GONE);
            carNameTxt.setVisibility(View.GONE);
        }

        String eStatus = generalFunc.getJsonValue("eStatus", userProfileJson);

        ENABLE_HAIL_RIDES = generalFunc.getJsonValue("ENABLE_HAIL_RIDES", userProfileJson);
        if (ENABLE_HAIL_RIDES.equalsIgnoreCase("No")) {
            hileimagview.setVisibility(View.GONE);

        } else {
            hileimagview.setVisibility(View.VISIBLE);
        }

        if (eStatus.equalsIgnoreCase("inactive")) {
            mapbottomviewarea.setVisibility(View.GONE);
            mapviewarea.setVisibility(View.GONE);
            hileimagview.setVisibility(View.GONE);
            InactiveFragment inactiveFragment = new InactiveFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, inactiveFragment);
            ft.commit();
        } else {
            mapbottomviewarea.setVisibility(View.VISIBLE);
            mapviewarea.setVisibility(View.VISIBLE);
            hileimagview.setVisibility(View.VISIBLE);
        }
    }

    public void removeOldRequestsCode() {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActContext());
        Map<String, ?> keys = mPrefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Utils.printLog("map values", entry.getKey() + ": " + entry.getValue().toString());

            if (entry.getKey().contains(CommonUtilities.DRIVER_REQ_CODE_PREFIX_KEY)) {
                //generalFunc.removeValue(entry.getKey());
                Long CURRENTmILLI = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 1);
                long value = generalFunc.parseLongValue(0, entry.getValue().toString());
                if (CURRENTmILLI >= value) {
                    generalFunc.removeValue(entry.getKey());
                }
            }
        }
    }

    boolean isFirstRunTaskSkipped = false;

    @Override
    public void onTaskRun() {
        if (isFirstRunTaskSkipped == false) {
            isFirstRunTaskSkipped = true;
            return;
        }
        if (generalFunc.retrieveValue(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY).equals("true")) {
            return;
        }

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", userProfileJson);

        Utils.printLog("Task", "Run");

        if (currentRequestPositions < arr_CurrentRequests.length()) {
            JSONObject obj_temp = generalFunc.getJsonObject(arr_CurrentRequests, currentRequestPositions);

            String message_str = generalFunc.getJsonValue("tMessage", obj_temp.toString()).replace("\\\"", "\"");
            Utils.printLog("message_str", "::" + message_str);
            Utils.printLog("Data", "::" + obj_temp.toString());

            String codeKey = CommonUtilities.DRIVER_REQ_CODE_PREFIX_KEY + generalFunc.getJsonValue("MsgCode", message_str);
            Utils.printLog("codeKey", "::" + codeKey);
            Utils.printLog("codeStore", "::" + generalFunc.retrieveValue(codeKey));

            if (generalFunc.retrieveValue(codeKey).equals("") && !generalFunc.containsKey(CommonUtilities.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + generalFunc.getJsonValue("MsgCode", message_str))) {
                Utils.printLog("codeStore", ":Done:" + generalFunc.retrieveValue(codeKey));

//                Utils.sendBroadCast(getActContext(), CommonUtilities.passenger_message_arrived_intent_action, message_str);
//                generalFunc.storedata(CommonUtilities.DRIVER_REQ_CODE_PREFIX_KEY + generalFunc.getJsonValue("MsgCode", obj_temp.toString()), "true");

                generalFunc.storedata(codeKey, "true");
                Bundle bn = new Bundle();
                bn.putString("Message", message_str);
                Utils.printLog("Dismiss", "Start");
                (new StartActProcess(getActContext())).startActWithData(CabRequestedActivity.class, bn);
            }
            currentRequestPositions++;
        } else if (updateRequest != null) {
            updateRequest.stopRepeatingTask();
            updateRequest = null;
            isCurrentReqHandled = true;
        }
    }

    public boolean isPubNubEnabled() {
        String ENABLE_PUBNUB = generalFunc.getJsonValue("ENABLE_PUBNUB", userProfileJson);
        return ENABLE_PUBNUB.equalsIgnoreCase("Yes");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    public void setUserInfo() {
        ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValue("vName", userProfileJson) + " "
                + generalFunc.getJsonValue("vLastName", userProfileJson));

        ((MTextView) findViewById(R.id.walletbalncetxt)).setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE") + ": " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("user_available_balance", userProfileJson)));

        generalFunc.checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userImgView), userProfileJson, "vImage");
        generalFunc.checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userPicImgView), userProfileJson, "vImage");
        onlineOfflineTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_GO_ONLINE_TXT"));
        changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));

        String iDriverVehicleId = generalFunc.getJsonValue("iDriverVehicleId", userProfileJson);
        setCarInfo(iDriverVehicleId);

        if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") ||
                !generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {

            Bundle bn = new Bundle();
            if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") &&
                    !generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {
                bn.putString("msg", "DO_EMAIL_PHONE_VERIFY");
            } else if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES")) {
                bn.putString("msg", "DO_EMAIL_VERIFY");
            } else if (!generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {
                bn.putString("msg", "DO_PHONE_VERIFY");
            }

            bn.putString("UserProfileJson", userProfileJson);
            showMessageWithAction(onlineOfflineTxtView, generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_RIDER_TXT"), bn);
        }
    }

    public void showMessageWithAction(View view, String message, final Bundle bn) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(generalFunc.retrieveLangLBl("", "LBL_BTN_VERIFY_TXT"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);

                    }
                });
        snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
        snackbar.setDuration(10000);
        snackbar.show();
    }

    public void setGeneralData() {
        generalFunc.storedata(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValue("MOBILE_VERIFICATION_ENABLE", userProfileJson));
        generalFunc.storedata("LOCATION_ACCURACY_METERS", generalFunc.getJsonValue("LOCATION_ACCURACY_METERS", userProfileJson));
        generalFunc.storedata("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFunc.getJsonValue("DRIVER_LOC_UPDATE_TIME_INTERVAL", userProfileJson));
        generalFunc.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));
        generalFunc.storedata(Utils.ENABLE_PUBNUB_KEY, generalFunc.getJsonValue("ENABLE_PUBNUB", userProfileJson));

        generalFunc.storedata(CommonUtilities.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", userProfileJson));
        generalFunc.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));

    }

    public void setCarInfo(String iDriverVehicleId) {
        if (!iDriverVehicleId.equals("") && !iDriverVehicleId.equals("0")) {
            String vLicencePlateNo = generalFunc.getJsonValue("vLicencePlateNo", userProfileJson);
            carNumPlateTxt.setText(vLicencePlateNo);
            carNumPlateTxt.setVisibility(View.VISIBLE);

            String vMake = generalFunc.getJsonValue("vMake", userProfileJson);
            String vModel = generalFunc.getJsonValue("vModel", userProfileJson);

            carNameTxt.setText(vMake + " " + vModel);
            selectedcar = iDriverVehicleId;
            carNameTxt.setVisibility(View.VISIBLE);
            changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
        } else {
            changeCarTxt.setText(generalFunc.retrieveLangLBl("Choose car", "LBL_CHOOSE_CAR"));
        }
    }

    public void buildMenu() {
        if (list_menu_items == null) {
            list_menu_items = new ArrayList();
            drawerAdapter = new DrawerAdapter(list_menu_items, getActContext(), generalFunc);

            menuListView.setAdapter(drawerAdapter);
            menuListView.setOnItemClickListener(this);
        } else {
            list_menu_items.clear();
        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_profile, generalFunc.retrieveLangLBl("", "LBL_MY_PROFILE_HEADER_TXT"), "" + Utils.MENU_PROFILE});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_car, generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), "" + Utils.MENU_MANAGE_VEHICLES});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_doc, generalFunc.retrieveLangLBl("Your Documents", "LBL_MANAGE_DOCUMENT"), "" + Utils.MENU_YOUR_DOCUMENTS});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_yourtrip, generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS"), "" + Utils.MENU_YOUR_TRIPS});
        if (!generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_card, generalFunc.retrieveLangLBl("Payment", "LBL_PAYMENT"), "" + Utils.MENU_PAYMENT});
        }
        if (!generalFunc.getJsonValue(CommonUtilities.WALLET_ENABLE, userProfileJson).equals("") && generalFunc.getJsonValue(CommonUtilities.WALLET_ENABLE, userProfileJson).equalsIgnoreCase("Yes")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_wallet, generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"), "" + Utils.MENU_WALLET});
        }
        if (app_type.equals(Utils.CabGeneralType_Ride) || app_type.equals(Utils.CabGeneralType_Deliver) || app_type.equals(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_waybill, generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL"), "" + Utils.MENU_WAY_BILL});
        }

        if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") ||
                !generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_privacy, generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_TXT"), "" + Utils.MENU_ACCOUNT_VERIFY});

        }


        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_my_heat_view, generalFunc.retrieveLangLBl("", "LBL_MENU_MY_HEATVIEW"), "" + Utils.MENU_MY_HEATVIEW});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_emergency, generalFunc.retrieveLangLBl("Emergency Contact", "LBL_EMERGENCY_CONTACT"), "" + Utils.MENU_EMERGENCY_CONTACT});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_feedback, generalFunc.retrieveLangLBl("Rider Feedback", "LBL_RIDER_FEEDBACK"), "" + Utils.MENU_FEEDBACK});
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_chart, generalFunc.retrieveLangLBl("Trip Statistics", "LBL_TRIP_STATISTICS_TXT"), "" + Utils.MENU_TRIP_STATISTICS});

        if (!generalFunc.getJsonValue(CommonUtilities.REFERRAL_SCHEME_ENABLE, userProfileJson).equals("") && generalFunc.getJsonValue(CommonUtilities.REFERRAL_SCHEME_ENABLE, userProfileJson).equalsIgnoreCase("Yes")) {

            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_invite, generalFunc.retrieveLangLBl("Invite Friends", "LBL_INVITE_FRIEND_TXT"), "" + Utils.MENU_INVITE_FRIEND});

        }


        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_support, generalFunc.retrieveLangLBl("Support", "LBL_SUPPORT_HEADER_TXT"), "" + Utils.MENU_SUPPORT});


//
//        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_history, generalFunc.retrieveLangLBl("", "LBL_RIDE_HISTORY"), "" + Utils.MENU_RIDE_HISTORY});


//        if (generalFunc.getJsonValue("RIIDE_LATER", userProfileJson).equalsIgnoreCase("YES")) {
//            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_bookings, generalFunc.retrieveLangLBl("My Bookings", "LBL_MY_BOOKINGS"), "" + Utils.MENU_BOOKINGS});
//        }


        //  list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_about_us, generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_TXT"), "" + Utils.MENU_ABOUT_US});
        //   list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_privacy, generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY_TEXT"), "" + Utils.MENU_POLICY});

        //  list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_contact_us, generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"), "" + Utils.MENU_CONTACT_US});
        //  list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_help, generalFunc.retrieveLangLBl("", "LBL_HELP_TXT"), "" + Utils.MENU_HELP});
        /// list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_logout, generalFunc.retrieveLangLBl("", "LBL_SIGNOUT_TXT"), "" + Utils.MENU_SIGN_OUT});

        drawerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if (this.userLocation == null || isShowNearByPassengers == false) {
            return;
        }

        VisibleRegion vr = getMap().getProjection().getVisibleRegion();
        final LatLng mainCenter = vr.latLngBounds.getCenter();
        final LatLng northeast = vr.latLngBounds.northeast;
        final LatLng southwest = vr.latLngBounds.southwest;

        final double radius_map = generalFunc.CalculationByLocation(mainCenter.latitude, mainCenter.longitude, southwest.latitude, southwest.longitude);

        boolean isWithin1m = radius_map > this.radius_map + 0.001;

        if (isWithin1m == true)
            getNearByPassenger(String.valueOf(radius_map), mainCenter.latitude, mainCenter.longitude);

        this.radius_map = radius_map;

    }

    public void configHeatMapView(boolean isShowNearByPassengers) {
        this.isShowNearByPassengers = isShowNearByPassengers;
        userHeatmapBtnImgView.setImageResource(isShowNearByPassengers ? R.mipmap.ic_heatmap_on : R.mipmap.ic_heatmap_off);
        if (mapOverlayList.size() > 0) {
            for (int i = 0; i < mapOverlayList.size(); i++) {
                if (mapOverlayList.get(i) != null) {

                    mapOverlayList.get(i).setVisible(isShowNearByPassengers);
                }

            }
        }

        if (cameraForUserPosition() != null)
            onCameraChange(cameraForUserPosition());

    }

    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingMapProgressBar)).setVisibility(View.GONE);

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(true);
            getMap().setPadding(0, 0, 0, Utils.dipToPixels(getActContext(), 90));
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        getMap().setOnCameraChangeListener(this);

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();
                return true;
            }
        });
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void callgederApi(String egender)

    {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "updateUserGender");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eGender", egender);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Utils.printLog("Response", "::" + responseString);


                boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);


                String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                if (isDataAvail) {
                    generalFunc.storedata(CommonUtilities.USER_PROFILE_JSON, message);
                    userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
                    imgSetting.performClick();
                }


            }
        });
        exeWebServer.execute();
    }

    public void genderDailog() {
        closeDrawer();


        final Dialog builder = new Dialog(getActContext(), R.style.Theme_Dialog);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.gender_view);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

//        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.gender_view, null);

        final MTextView genderTitleTxt = (MTextView) builder.findViewById(R.id.genderTitleTxt);
        final MTextView maleTxt = (MTextView) builder.findViewById(R.id.maleTxt);
        final MTextView femaleTxt = (MTextView) builder.findViewById(R.id.femaleTxt);
        final ImageView gendercancel = (ImageView) builder.findViewById(R.id.gendercancel);
        final ImageView gendermale = (ImageView) builder.findViewById(R.id.gendermale);
        final ImageView genderfemale = (ImageView) builder.findViewById(R.id.genderfemale);
        final LinearLayout male_area = (LinearLayout) builder.findViewById(R.id.male_area);
        final LinearLayout female_area = (LinearLayout) builder.findViewById(R.id.female_area);

        genderTitleTxt.setText(generalFunc.retrieveLangLBl("Select your gender to continue", "LBL_SELECT_GENDER"));
        maleTxt.setText(generalFunc.retrieveLangLBl("Male", "LBL_MALE_TXT"));
        femaleTxt.setText(generalFunc.retrieveLangLBl("FeMale", "LBL_FEMALE_TXT"));

        gendercancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });

        male_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callgederApi("Male");
                builder.dismiss();

            }
        });
        female_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callgederApi("Female");
                builder.dismiss();

            }
        });


//        gender = builder.create();
//        gender.setCancelable(false);
//        if (generalFunc.isRTLmode() == true) {
//            generalFunc.forceRTLIfSupported(gender);
//        }
//        if (!gender.isShowing()) {
//            gender.show();
//        }

        builder.show();

    }

    public void noLocationDailog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.no_location_view, null);

        final MTextView noLocTitleTxt = (MTextView) dialogView.findViewById(R.id.noLocTitleTxt);
        final MTextView noLocMesageTxt = (MTextView) dialogView.findViewById(R.id.noLocMesageTxt);
        final MTextView settingBtn = (MTextView) dialogView.findViewById(R.id.settingBtn);
        final MTextView RetryBtn = (MTextView) dialogView.findViewById(R.id.RetryBtn);

        builder.setView(dialogView);
        noLocTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ENABLE_LOC_SERVICE"));
        noLocMesageTxt.setText(generalFunc.retrieveLangLBl("This app requires location services. Please enabled location service from device settings. Go to Settings >> Location >>Turn on", "LBL_NO_LOCATION_ANDROID_TXT"));
        settingBtn.setText(generalFunc.retrieveLangLBl("Settings", "LBL_SETTINGS"));
        RetryBtn.setText(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));


        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                no_loaction.dismiss();
                new StartActProcess(getActContext()).
                        startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);

            }
        });

        RetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                no_loaction.dismiss();

                if (!generalFunc.isLocationEnabled()) {
                    noLocationDailog();
                }


            }
        });


        no_loaction = builder.create();
        no_loaction.setCancelable(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(no_loaction);
        }
        if (!no_loaction.isShowing()) {
            no_loaction.show();
        }

//        final GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());
//        generateAlertBox.setCancelable(false);
//        generateAlertBox.setContentMessage(generalFunc.retrieveLangLBl("", "LBL_ENABLE_LOC_SERVICE"), generalFunc.retrieveLangLBl("This app requires location services. Please enabled location service from device settings. Go to Settings >> Location >>Turn on", "LBL_NO_LOCATION_ANDROID_TXT"));
//        generateAlertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
//            @Override
//            public void handleBtnClick(int btn_id) {
//                generateAlertBox.closeAlertBox();
//
//                if (btn_id == 1) {
//
//                    new StartActProcess(getActContext()).
//                            startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
//
//                } else if (btn_id == 0) {
//
//                }
//            }
//        });
//
//        generateAlertBox.setPositiveBtn(generalFunc.retrieveLangLBl("Settings", "LBL_SETTINGS"));
//        generateAlertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));
//
//        generateAlertBox.showAlertBox();
    }

    public void goOnlineOffline(final boolean isGoOnline, final boolean isMessageShown) {
        if (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0) {

            if (generalFunc.isLocationEnabled() == false) {
                if (!no_loaction.isShowing()) {
                    noLocationDailog();
                    return;
                }
            }
            // generalFunc.showMessage();
            return;
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverStatus");
        parameters.put("iDriverId", generalFunc.getMemberId());

        if (isGoOnline == true) {
            parameters.put("Status", "Available");
            parameters.put("isUpdateOnlineDate", "true");
        } else {
            parameters.put("Status", "Not Available");
        }
        parameters.put("latitude", "" + userLocation.getLatitude());
        parameters.put("longitude", "" + userLocation.getLongitude());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        if (isMessageShown == true) {
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        }

        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (isMessageShown == false) {
                    return;
                }

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);
                    String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                    if (message.equals("SESSION_OUT")) {
                        generalFunc.notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }
                    if (message.equals("REQUIRED_MINIMUM_BALNCE")) {
                        generalFunc.showGeneralMessage("", generalFunc.getJsonValue("Msg", responseString));
                        return;
                    }
                    if (isDataAvail == true) {

                        if (isGoOnline == true) {
                            setOnlineState();
                        } else {
                            setOfflineState();
                        }
                    } else {
                        isOnlineAvoid = true;
                        if (isGoOnline == true) {
                            onlineOfflineSwitch.setChecked(false);
                        } else {
                            onlineOfflineSwitch.setChecked(true);
                        }
                        Bundle bn = new Bundle();
                        bn.putString("msg", "" + message);
                        bn.putString("UserProfileJson", userProfileJson);

                        if (message.equals("DO_EMAIL_PHONE_VERIFY") || message.equals("DO_PHONE_VERIFY") || message.equals("DO_EMAIL_VERIFY")) {
                            accountVerificationAlert(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_TXT"), bn);
                            return;
                        }
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl(generalFunc.getJsonValue(CommonUtilities.message_str, responseString),
                                        generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {

                    isOnlineAvoid = true;
                    if (isGoOnline == true) {
                        onlineOfflineSwitch.setChecked(false);
                    } else {
                        onlineOfflineSwitch.setChecked(true);
                    }

                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void setOfflineState() {
        isDriverOnline = false;
        onlineOfflineTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_GO_ONLINE_TXT"));

        stopService(startUpdatingStatus);

        generalFunc.storedata(CommonUtilities.DRIVER_ONLINE_KEY, "false");

        if (pubNub != null) {
            pubNub.unSubscribeToCabRequestChannel();
            pubNub.unSubscribeToPrivateChannel();
        }
        if (updatepubnubtask != null) {
            updatepubnubtask.stopRepeatingTask();
            updatepubnubtask = null;
        }

        Utils.printLog("setOfflineState", "called");
        unRegisterReceiver();
    }

    public void setOnlineState() {
        isDriverOnline = true;
        onlineOfflineTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_GO_OFFLINE_TXT"));

        if (!generalFunc.isServiceRunning(UpdateDriverStatus.class)) {
            startService(startUpdatingStatus);
        }

        generalFunc.storedata(CommonUtilities.DRIVER_ONLINE_KEY, "true");

        registerBroadCastReceiver();

        updateLocationToPubNub();

        if (pubNub != null) {
            pubNub.subscribeToCabRequestChannel();
        }


        if (updatepubnubtask != null) {
            updatepubnubtask.stopRepeatingTask();
            updatepubnubtask = null;
        }
        updatepubnubtask = new UpdateFrequentTask(5 * 1000);
//            this.updateRequest = updateRequest;
        updatepubnubtask.setTaskRunListener(new UpdateFrequentTask.OnTaskRunCalled() {
            @Override
            public void onTaskRun() {

                Utils.printLog("isDriverOnline", "::" + isDriverOnline + "::" + getApp().isMyAppInBackGround());
                if (isDriverOnline && !getApp().isMyAppInBackGround()) {
                    if (pubNub != null) {
                        Utils.printLog("isDriverOnlinePubnub", "::" + "notnull");
                        pubNub.subscribeToCabRequestChannel();
                        pubNub.subscribeToPrivateChannel();
                    }
                } else if (getApp().isMyAppInBackGround()) {
                    if (pubNub != null) {
                        Utils.printLog("isDriverOnlinePubnub", "::" + "notnull");
                        pubNub.unSubscribeToCabRequestChannel();
                        pubNub.unSubscribeToPrivateChannel();
                    }
                }

                generalFunc.sendHeartBeat();

            }
        });
        updatepubnubtask.startRepeatingTask();
    }

    public void accountVerificationAlert(String message, final Bundle bn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                if (btn_id == 1) {
                    generateAlert.closeAlertBox();
                    (new StartActProcess(getActContext())).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);
                } else if (btn_id == 0) {
                    generateAlert.closeAlertBox();
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
        generateAlert.showAlertBox();
    }

    public void updateLocationToPubNub() {
        if (pubNub != null && isDriverOnline == true && userLocation != null && userLocation.getLongitude() != 0.0 && userLocation.getLatitude() != 0.0) {
            pubNub.publishMsg(generalFunc.getLocationUpdateChannel(), generalFunc.buildLocationJson(userLocation));
        }
    }

    public void getNearByPassenger(String radius, double center_lat, double center_long) {

        if (heatMapAsyncTask != null) {
            heatMapAsyncTask.cancel(true);
            heatMapAsyncTask = null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadPassengersLocation");
        parameters.put("Radius", radius);
        parameters.put("Latitude", String.valueOf(center_lat));
        parameters.put("Longitude", String.valueOf(center_long));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        this.heatMapAsyncTask = exeWebServer;

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = generalFunc.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        JSONArray dataLocArr = generalFunc.getJsonArray(CommonUtilities.message_str, responseString);

                        ArrayList<LatLng> listTemp = new ArrayList<LatLng>();
                        ArrayList<LatLng> Online_listTemp = new ArrayList<LatLng>();
                        for (int i = 0; i < dataLocArr.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(dataLocArr, i);

                            String type = generalFunc.getJsonValue("Type", obj_temp.toString());

                            double lat = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("Latitude", obj_temp.toString()));
                            double longi = generalFunc.parseDoubleValue(0.0, generalFunc.getJsonValue("Longitude", obj_temp.toString()));


                            if (type.equalsIgnoreCase("Online")) {

                                String iUserId = generalFunc.getJsonValue("iUserId", obj_temp.toString());

                                if (onlinePassengerLocList.containsKey("ID_" + type + "_" + iUserId) == false) {
                                    onlinePassengerLocList.put("ID_" + type + "_" + iUserId, "True");

                                    Online_listTemp.add(new LatLng(lat, longi));
                                }


                            } else {
                                String iTripId = generalFunc.getJsonValue("iTripId", obj_temp.toString());
                                if (historyLocList.containsKey("ID_" + type + "_" + iTripId) == false) {
                                    historyLocList.put("ID_" + type + "_" + iTripId, "True");

                                    listTemp.add(new LatLng(lat, longi));
                                }
                            }
                        }

                        if (listTemp.size() > 0) {
                            mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                    new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(153, 0, 0), Color.WHITE}, new float[]{0.2f, 1.5f})).data(listTemp).build())));
                        }
                        if (Online_listTemp.size() > 0) {
                            mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                    new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(0, 51, 0), Color.WHITE}, new float[]{0.2f, 1.5f}, 1000)).data(Online_listTemp).build())));
                        }
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void configCarList(final boolean isCarUpdate, final String selectedCarId, final int position) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        if (isCarUpdate == false) {
            parameters.put("type", "LoadAvailableCars");
        } else {
            parameters.put("type", "SetDriverCarID");
            parameters.put("iDriverVehicleId", selectedCarId);
        }
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        if (isCarUpdate == false) {
                            LoadCarList(generalFunc.getJsonArray(CommonUtilities.message_str, responseString));
                        } else {

                            String vLicencePlateNo = generalFunc.getJsonValue("vLicencePlate", items_txt_car_json.get(position));
                            carNumPlateTxt.setText(vLicencePlateNo);
                            carNumPlateTxt.setVisibility(View.VISIBLE);

                            String vMake = generalFunc.getJsonValue("vMake", items_txt_car_json.get(position));
                            String vModel = generalFunc.getJsonValue("vTitle", items_txt_car_json.get(position));

                            carNameTxt.setText(vMake + " " + vModel);
                            selectedcar = selectedCarId;
                            carNameTxt.setVisibility(View.VISIBLE);
                            changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));


                            generalFunc.showMessage(generalFunc.getCurrentView(MainActivity.this), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
                        }

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void LoadCarList(JSONArray array) {


        items_txt_car.clear();
        items_car_id.clear();
        items_txt_car_json.clear();
        final ArrayList list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(array, i);

            items_txt_car.add(generalFunc.getJsonValue("vMake", obj_temp.toString()) + " " + generalFunc.getJsonValue("vTitle", obj_temp.toString()));
            items_car_id.add(generalFunc.getJsonValue("iDriverVehicleId", obj_temp.toString()));
            items_txt_car_json.add(obj_temp.toString());

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("car", items_txt_car.get(i).toString());
            map.put("iDriverVehicleId", items_car_id.get(i).toString());
            list.add(map);

        }
        // CharSequence[] cs_currency_txt = items_txt_car.toArray(new CharSequence[items_txt_car.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectcar_view, null);

        final MTextView vehTitleTxt = (MTextView) dialogView.findViewById(R.id.VehiclesTitleTxt);
        final MTextView mangeVehiclesTxt = (MTextView) dialogView.findViewById(R.id.mangeVehiclesTxt);
        final MTextView addVehiclesTxt = (MTextView) dialogView.findViewById(R.id.addVehiclesTxt);
        final RecyclerView vehiclesRecyclerView = (RecyclerView) dialogView.findViewById(R.id.vehiclesRecyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(vehiclesRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL_LIST);
        vehiclesRecyclerView.addItemDecoration(dividerItemDecoration);

        builder.setView(dialogView);
        vehTitleTxt.setText(generalFunc.retrieveLangLBl("Select Your Vehicles", "LBL_SELECT_CAR_TXT"));
        mangeVehiclesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"));
        addVehiclesTxt.setText(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"));


        ManageVehicleListAdapter adapter = new ManageVehicleListAdapter(getActContext(), list, generalFunc, selectedcar);
        vehiclesRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickList(this);

        mangeVehiclesTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_car.dismiss();
                Bundle bn = new Bundle();
                new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);

            }
        });

        addVehiclesTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_car.dismiss();


            }
        });

        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Bundle bn = new Bundle();
                new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);


            }
        });
        builder.setPositiveButton(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
                (new StartActProcess(getActContext())).startAct(AddVehicleActivity.class);


            }
        });


//        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int item) {
//                // Do something with the selection
//
//                if (list_car != null) {
//                    list_car.dismiss();
//                }
//
//                String selected_carId = items_car_id.get(item);
//
//                configCarList(true, selected_carId, item);
//            }
//        });


        list_car = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_car);
        }
        list_car.show();
        final Button positiveButton = list_car.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.appThemeColor_1));
        final Button negativeButton = list_car.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
        list_car.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Utils.hideKeyboard(getActContext());
            }
        });
    }

    @Override
    public void onLocationUpdate(Location location) {

//        Location templocation=new Location("");
//        templocation.setLatitude(40.72752);
//        templocation.setLongitude(-73.9569323);
//        location=templocation;
        this.userLocation = location;

        CameraPosition cameraPosition = cameraForUserPosition();

        if (cameraPosition != null)
            getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        if (isFirstLocation == true && generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") &&
                generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {

            isFirstLocation = false;

            String isGoOnline = generalFunc.retrieveValue(CommonUtilities.GO_ONLINE_KEY);

            if ((isGoOnline != null && !isGoOnline.equals("") && isGoOnline.equals("Yes"))) {
                long lastTripTime = generalFunc.parseLongValue(0, generalFunc.retrieveValue(CommonUtilities.LAST_FINISH_TRIP_TIME_KEY));
                long currentTime = Calendar.getInstance().getTimeInMillis();

                if ((currentTime - lastTripTime) < 25000) {
                    onlineOfflineSwitch.setChecked(true);
                }

                generalFunc.storedata(CommonUtilities.GO_ONLINE_KEY, "No");
                generalFunc.storedata(CommonUtilities.LAST_FINISH_TRIP_TIME_KEY, "0");

            }

            if (generalFunc.getJsonValue("vAvailability", userProfileJson).equals("Available") && isDriverOnline == false) {
                onlineOfflineSwitch.setChecked(true);
            }
        }

        updateLocationToPubNub();
    }

    public void pubNubStatus(String status) {

    }

    public CameraPosition cameraForUserPosition() {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        if (userLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    public void openMenuProfile() {
        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);
        bn.putBoolean("isDriverOnline", isDriverOnline);
        new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, final View view, int position, long l) {
        int itemId = generalFunc.parseIntegerValue(0, list_menu_items.get(position)[2]);
        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);


        drawerAdapter.notifyDataSetChanged();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    MTextView menuTitleTxt = (MTextView) view.findViewById(R.id.menuTitleTxt);
//                    menuTitleTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));
//                    ImageView menuIcon = (ImageView) view.findViewById(R.id.menuIcon);
//                    menuIcon.setColorFilter(ContextCompat.getColor(getActContext(), R.color.appThemeColor_1));
//                    LinearLayout mainarea = (LinearLayout) view.findViewById(R.id.mainarea);
//                    mainarea.setBackgroundColor(getResources().getColor(R.color.bg_select));
//                }
//                catch (Exception e)
//                {
//
//                }
//            }
//        }, 1000);
        switch (itemId) {
            case Utils.MENU_PROFILE:
                openMenuProfile();
                break;

            case Utils.MENU_PAYMENT:
                new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                break;

            case Utils.MENU_RIDE_HISTORY:
                new StartActProcess(getActContext()).startActWithData(RideHistoryActivity.class, bn);
                break;

            case Utils.MENU_BOOKINGS:
                new StartActProcess(getActContext()).startActWithData(MyBookingsActivity.class, bn);
                break;

            case Utils.MENU_FEEDBACK:
                new StartActProcess(getActContext()).startActWithData(DriverFeedbackActivity.class, bn);
                break;

            case Utils.MENU_ABOUT_US:
                new StartActProcess(getActContext()).startAct(StaticPageActivity.class);
                break;
            case Utils.MENU_POLICY:
                (new StartActProcess(getActContext())).openURL(CommonUtilities.SERVER_URL + "privacy-policy");
                break;
            case Utils.MENU_CONTACT_US:
                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                break;
            case Utils.MENU_YOUR_DOCUMENTS:
                bn.putString("PAGE_TYPE", "Driver");
                bn.putString("iDriverVehicleId", "");
                bn.putString("doc_file", "");
                bn.putString("iDriverVehicleId", "");
                new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                break;

            case Utils.MENU_TRIP_STATISTICS:
                new StartActProcess(getActContext()).startActWithData(StatisticsActivity.class, bn);
                break;
            case Utils.MENU_MANAGE_VEHICLES:
                new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
                break;

            case Utils.MENU_HELP:
                new StartActProcess(getActContext()).startAct(HelpActivity.class);
                break;

            case Utils.MENU_WALLET:
                iswallet = true;
                new StartActProcess(getActContext()).startActWithData(MyWalletActivity.class, bn);
                break;

            case Utils.MENU_WAY_BILL:
                new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn);

                break;
            case Utils.MENU_ACCOUNT_VERIFY:
                if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") ||
                        !generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {

                    Bundle bn1 = new Bundle();
                    if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES") &&
                            !generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {
                        bn1.putString("msg", "DO_EMAIL_PHONE_VERIFY");
                    } else if (!generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES")) {
                        bn1.putString("msg", "DO_EMAIL_VERIFY");
                    } else if (!generalFunc.getJsonValue("ePhoneVerified", userProfileJson).equalsIgnoreCase("YES")) {
                        bn1.putString("msg", "DO_PHONE_VERIFY");
                    }

                    bn1.putString("UserProfileJson", userProfileJson);
                    new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn1, Utils.VERIFY_INFO_REQ_CODE);

                }
                break;
            case Utils.MENU_INVITE_FRIEND:
                new StartActProcess(getActContext()).startActWithData(InviteFriendsActivity.class, bn);
                break;

            case Utils.MENU_EMERGENCY_CONTACT:
                new StartActProcess(getActContext()).startAct(EmergencyContactActivity.class);
                break;

            case Utils.MENU_SUPPORT:
                new StartActProcess(getActContext()).startAct(SupportActivity.class);
                break;
            case Utils.MENU_YOUR_TRIPS:
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);
                break;

            case Utils.MENU_SIGN_OUT:
                generalFunc.logOutUser();
                generalFunc.restartApp();
                break;
            case Utils.MENU_MY_HEATVIEW:
                new StartActProcess(getActContext()).startActWithData(MyHeatViewActivity.class, bn);
        }

        closeDrawer();
    }

    public void checkDrawerState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START) == true) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public Context getActContext() {
        return MainActivity.this;
    }

    public void checkIsDriverOnline() {
        if (isDriverOnline == true) {
            stopService(startUpdatingStatus);
//            goOnlineOffline(false, false);

            for (int i = 0; i < 1000; i++) {
            }
        }
    }

    public void registerBroadCastReceiver() {

        if (gcmBroadCastReceiver == null) {
            gcmBroadCastReceiver = new GcmBroadCastReceiver();
        }
        unRegisterReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonUtilities.passenger_message_arrived_intent_action);

        registerReceiver(gcmBroadCastReceiver, filter);

    }

    public void unRegisterReceiver() {
        if (gcmBroadCastReceiver != null) {
            try {
                unregisterReceiver(gcmBroadCastReceiver);
            } catch (Exception e) {

            }
        }
    }

    public void registerBackgroundAppReceiver() {

        unRegisterBackgroundAppReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonUtilities.BACKGROUND_APP_RECEIVER_INTENT_ACTION);

        registerReceiver(bgAppReceiver, filter);
    }

    public void unRegisterBackgroundAppReceiver() {
        if (bgAppReceiver != null) {
            try {
                unregisterReceiver(bgAppReceiver);
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (generalFunc.isLocationEnabled() == false) {
            Utils.printLog("onresumenoLocationDailog", "called");
            noLocationDailog();
        }

        userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);


        if (iswallet) {
            userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
            setUserInfo();
            iswallet = false;
        }

        if (pubNub != null) {
            pubNub.addListener();
        }

        if (getLastLocation != null) {
            getLastLocation.startLocationUpdates();
        }

        if (generalFunc.retrieveValue(CommonUtilities.DRIVER_ONLINE_KEY).equals("false") && isDriverOnline == true) {
            setOfflineState();
            isOnlineAvoid = true;
            onlineOfflineSwitch.setChecked(false);

        }

        if (generalFunc.isLocationEnabled() == true) {
            if (no_loaction != null) {
                if (no_loaction.isShowing()) {
                    no_loaction.dismiss();
                }
            }
        }


//        registerBroadCastReceiver();

        Utils.dismissBackGroundNotification(getActContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
        }
    }

    public MyApp getApp() {
        return ((MyApp) getApplication());
    }

    public void configBackground() {
        Utils.printLog("isCurrentReqHandled", "::" + isCurrentReqHandled);
        Utils.printLog("isDriverOnlineMain", "::" + isDriverOnline);
        if (isCurrentReqHandled == false) {
            generalFunc.removeValue(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY);
            return;
        }
        Utils.printLog("isCurrentReqHandled1", "::" + isCurrentReqHandled);
        Utils.printLog("isDriverOnlineMain1", "::" + isDriverOnline);

        if (getApp().isMyAppInBackGround()) {
            unRegisterReceiver();

            if (pubNub != null) {
                pubNub.unSubscribeToCabRequestChannel();
            }
        } else {
            if (getApp().isMyAppInBackGround() == false && isDriverOnline == true) {
                Utils.printLog("Online", "State");
                setOnlineState();
            }
            Utils.printLog("Online", "State2");

            if (generalFunc.containsKey(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY)) {
                Utils.dismissBackGroundNotification(getActContext());

                String msg = generalFunc.retrieveValue(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY);

                String message_str = msg.replace("\\\"", "\"");
                Utils.printLog("Servicemessage_str", "::" + message_str);
                Utils.printLog("ServiceData", "::" + msg);

                generalFunc.removeValue(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY);


                Bundle bn = new Bundle();
                bn.putString("Message", message_str);
                generalFunc.storedata(CommonUtilities.DRIVER_CURRENT_REQ_OPEN_KEY, "true");
                Utils.printLog("Dismiss", "Start");
                (new StartActProcess(getActContext())).startActWithData(CabRequestedActivity.class, bn);

            }
        }
    }


    public void removeLocationUpdates() {

        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
            getLastLocation.removeLocUpdateListener();
            getLastLocation = null;
        }

        this.userLocation = null;
    }

    public void removePubNub() {
        if (pubNub != null) {
            try {


                pubNub.releaseInstances();
            } catch (Exception e) {

            }

        }
    }

    @Override
    protected void onDestroy() {
        checkIsDriverOnline();
        unRegisterReceiver();
        removePubNub();
        removeLocationUpdates();
        unRegisterBackgroundAppReceiver();

        if (updatepubnubtask != null) {
            updatepubnubtask.stopRepeatingTask();
            updatepubnubtask = null;
        }


        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
            return;
        }

        checkIsDriverOnline();
        removePubNub();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String userProfileJson = data.getStringExtra("UserProfileJson");

            this.userProfileJson = userProfileJson;
            setUserInfo();
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String msgType = data.getStringExtra("MSG_TYPE");

            if (msgType.equalsIgnoreCase("EDIT_PROFILE")) {
                openMenuProfile();
            }
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE) {

            this.userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
            buildMenu();
        } else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String userProfileJson = data.getStringExtra("UserProfileJson");
            this.userProfileJson = userProfileJson;
        } else if (requestCode == Utils.REQUEST_CODE_GPS_ON && resultCode == RESULT_OK) {
            if (getLastLocation == null) {
                getLastLocation = new GetLocationUpdates(getActContext(), 2, true);
                getLastLocation.setLastLocationListener(this);
            }

            if (no_loaction != null) {
                if (no_loaction.isShowing()) {
                    no_loaction.dismiss();
                }
            }
//            else
//            {
//                showNoLocationDialog();
//            }

            if (getLastLocation != null) {
                userLocation = getLastLocation.getLocation();
            }
        }
    }

    @Override
    public void onItemClick(int position, int viewClickId) {

        list_car.dismiss();

        String selected_carId = items_car_id.get(position);

        configCarList(true, selected_carId, position);


    }

    @Override
    public void handleLastLocationListnerCallback(Location mLastLocation) {
        userLocation = mLastLocation;

    }

    @Override
    public void handleLastLocationListnerNOVALUECallback(int id) {

    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == menuImgView.getId()) {
                checkDrawerState();
            } else if (view.getId() == userLocBtnImgView.getId()) {
                if (userLocation == null) {
                    return;
                }
                CameraPosition cameraPosition = cameraForUserPosition();
                if (cameraPosition != null)
                    getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else if (view.getId() == userHeatmapBtnImgView.getId()) {

                if (userLocation == null) {
                    return;
                }

                configHeatMapView(isShowNearByPassengers ? false : true);

            } else if (view.getId() == changeCarTxt.getId()) {
                configCarList(false, "", 0);
            } else if (view.getId() == imgSetting.getId()) {
                closeDrawer();
                userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
                if (generalFunc.retrieveValue(CommonUtilities.FEMALE_RIDE_REQ_ENABLE).equalsIgnoreCase("yes")) {

                    if (generalFunc.getJsonValue("eGender", userProfileJson).equalsIgnoreCase("feMale")) {
                        new StartActProcess(getActContext()).startAct(PrefranceActivity.class);
                    } else {
                        if (generalFunc.getJsonValue("eGender", userProfileJson).equals("")) {
                            genderDailog();

                        } else {
                            menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                        }
                    }
                } else {
                    menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                }

            } else if (view.getId() == logoutarea.getId()) {
                generalFunc.logOutUser();
                generalFunc.restartApp();
            } else if (view.getId() == hileimagview.getId()) {

                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {

                    generalFunc.showMessage(menuImgView, generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                } else {
                    Bundle bn = new Bundle();
                    bn.putString("userLocation", userLocation + "");
                    bn.putDouble("lat", userLocation.getLatitude());
                    bn.putDouble("long", userLocation.getLongitude());
                    new StartActProcess(getActContext()).startActWithData(HailActivity.class, bn);

                }
            }


        }
    }
}
