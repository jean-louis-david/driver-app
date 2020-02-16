package com.app85taxi.driver;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.general.files.CancelTripDialog;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.OpenPassengerDetailDialog;
import com.general.files.StartActProcess;
import com.general.files.TripMessageReceiver;
import com.general.files.UpdateDirections;
import com.general.files.UpdateDriverLocationService;
import com.general.files.UpdateFrequentTask;
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
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import java.util.HashMap;

public class DriverArrivedActivity extends AppCompatActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdates {

    GeneralFunctions generalFunc;

    MTextView titleTxt;
    MButton btn_type2;

    String tripId = "";

    HashMap<String, String> data_trip;
    SupportMapFragment map;
    GoogleMap gMap;
    GetLocationUpdates getLocationUpdates;

    boolean isFirstLocation = true;
    Location userLocation;

    ConfigPubNub configPubNub;
    TripMessageReceiver tripMsgReceiver;
    Intent startLocationUpdateService;
    MTextView addressTxt;

    Polyline route_polyLine;
    ExecuteWebServerUrl routeExeWebServer;
    boolean killRouteDrawn = false;

    String REQUEST_TYPE = "";

    public ImageView emeTapImgView;
    android.support.v7.app.AlertDialog list_navigation;

    UpdateDirections updateDirections;
    // public MTextView timeTxt;

    Marker driverMarker;
    Location currentRotatedLocation = null;
    boolean isnotification = false;

    UpdateFrequentTask updatepubnubtask;
    ConfigPubNub pubNub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_arrived);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = new GeneralFunctions(getActContext());

        generalFunc.storedata(CommonUtilities.DRIVER_ONLINE_KEY, "false");
        isnotification = getIntent().getBooleanExtra("isnotification", false);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        (findViewById(R.id.backImgView)).setVisibility(View.GONE);
        btn_type2.setId(Utils.generateViewId());

        emeTapImgView = (ImageView) findViewById(R.id.emeTapImgView);
        emeTapImgView.setOnClickListener(new setOnClickList());

        //timeTxt = (MTextView) findViewById(timeTxt);
        setData();
//        if (isnotification) {
//            new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, isnotification);
//        }
        if (generalFunc.retrieveValue("OPEN_CHAT").equals("Yes")) {
            generalFunc.storedata("OPEN_CHAT", "No");
            Bundle bnChat = new Bundle();

            bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
            bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
            bnChat.putString("iTripId", data_trip.get("iTripId"));
            bnChat.putString("FromMemberName", data_trip.get("PName"));

            new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
        }
        setLabels();

        generalFunc.storedata(CommonUtilities.DriverWaitingTime, "0");
        generalFunc.storedata(CommonUtilities.DriverWaitingSecTime, "0");

        tripMsgReceiver = new TripMessageReceiver((Activity) getActContext(), false);
        startLocationUpdateService = new Intent(getApplicationContext(), UpdateDriverLocationService.class);
        startLocationUpdateService.putExtra("PAppVersion", data_trip.get("PAppVersion"));

        map.getMapAsync(this);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        btn_type2.setOnClickListener(new setOnClickAct());
        registerTripMsgReceiver();
        startService(startLocationUpdateService);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        if (generalFunc.isRTLmode()) {
            (findViewById(R.id.navStripImgView)).setRotation(180);
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

    public boolean isPubNubEnabled() {
        String ENABLE_PUBNUB = generalFunc.retrieveValue(Utils.ENABLE_PUBNUB_KEY);
        return ENABLE_PUBNUB.equalsIgnoreCase("Yes");
    }

    public void setTimetext(String distance, String time) {
        try {
//            timeTxt.setText(time + " " + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT") + " & " + distance + "  " + generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));
        } catch (Exception e) {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
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

    public void setLabels() {
        setPageName();

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_ARRIVED_TXT"));
        ((MTextView) findViewById(R.id.navigateTxt)).setText(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));
    }

    public void setPageName() {
        if (REQUEST_TYPE.equals("Deliver")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("Pickup Delivery", "LBL_PICKUP_DELIVERY"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("Pick Up Passenger", "LBL_PICK_UP_PASSENGER"));
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
        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();
                return false;
            }
        });

        double passenger_lat = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double passenger_lon = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

        MarkerOptions marker_passenger_opt = new MarkerOptions()
                .position(new LatLng(passenger_lat, passenger_lon));
        marker_passenger_opt.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_passanger)).anchor(0.5f,
                0.5f);
        getMap().addMarker(marker_passenger_opt);


        getLocationUpdates = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true);
        getLocationUpdates.setLocationUpdatesListener(this);

//        if (userLocation != null && route_polyLine == null) {
//            drawRoute("" + passenger_lat, "" + passenger_lon);
//
//
//
//        }
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void drawRoute(final String passenger_lat, final String passenger_lon) {

        String originLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destLoc = passenger_lat + "," + passenger_lon;
        String serverKey = getResources().getString(R.string.google_api_get_address_from_location_serverApi);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        Utils.printLog("url pickUp", "url:" + url);

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

    public void setData() {

        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        Utils.printLog("Api", ":: arrived" + data.toString());
        this.data_trip = data;

        double passenger_lat = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double passenger_lon = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

        addressTxt.setText(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"));
        GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setLocation(passenger_lat, passenger_lon);
        getAddressFromLocation.setAddressList(new GetAddressFromLocation.AddressFound() {
            @Override
            public void onAddressFound(String address, double latitude, double longitude) {
                addressTxt.setText(address);
            }
        });
        getAddressFromLocation.execute();

        (findViewById(R.id.navigateArea)).setOnClickListener(new setOnClickAct("" + passenger_lat, "" + passenger_lon));

        REQUEST_TYPE = data_trip.get("REQUEST_TYPE");

        setPageName();
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;


        if (currentRotatedLocation == null) {
            currentRotatedLocation = location;

        }

        CameraPosition cameraPosition = cameraForUserPosition();
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);

        if (updateDirections == null) {
            Location destLoc = new Location("temp");
            destLoc.setLatitude(Double.parseDouble(data_trip.get("sourceLatitude")));
            destLoc.setLongitude(Double.parseDouble(data_trip.get("sourceLongitude")));
            updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
            updateDirections.scheduleDirectionUpdate();

        }

        updateDriverMarker();
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

    public void buildMsgOnArrivedBtn() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                if (btn_id == 0) {
                    generateAlert.closeAlertBox();
                } else {
                    setDriverStatusToArrived();
                }
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_ARRIVED_CONFIRM_DIALOG_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        generateAlert.showAlertBox();

        generateAlert.getAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#1C1C1C"));
        generateAlert.getAlertDialog().getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#909090"));
    }

    public void setDriverStatusToArrived() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "DriverArrived");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {
                        unRegisterReceiver();
                        stopDriverLocationUpdateService();

                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                        data_trip.put("DestLocLatitude", generalFunc.getJsonValue("DLatitude", message));
                        data_trip.put("DestLocLongitude", generalFunc.getJsonValue("DLongitude", message));
                        data_trip.put("DestLocAddress", generalFunc.getJsonValue("DAddress", message));
                        data_trip.put("vTripStatus", "Arrived");

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
                        new StartActProcess(getActContext()).startActWithData(ActiveTripActivity.class, bn);

                        //   generalFunc.restartwithGetDataApp();

//                        stopPubNub();
                        ActivityCompat.finishAffinity(DriverArrivedActivity.this);

                    } else {
                        if (generalFunc.getJsonValue(CommonUtilities.message_str, responseString).equals("DO_RESTART")) {
                            generalFunc.restartApp();
                        } else {
                            generalFunc.showGeneralMessage("",
                                    generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        }

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL"));
        menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel trip", "LBL_CANCEL_TRIP"));

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_passenger_detail:

                new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, false);
                return true;

            case R.id.menu_cancel_trip:
                new CancelTripDialog(getActContext(), data_trip, generalFunc, false);
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
        unRegisterReceiver();
        stopDriverLocationUpdateService();
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

    public Context getActContext() {
        return DriverArrivedActivity.this; // Must be context of activity not application
    }

    public class setOnClickAct implements View.OnClickListener {

        String passenger_lat = "";
        String passenger_lon = "";

        public setOnClickAct() {
        }

        public setOnClickAct(String passenger_lat, String passenger_lon) {
            this.passenger_lat = passenger_lat;
            this.passenger_lon = passenger_lon;
        }

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == btn_type2.getId()) {
                buildMsgOnArrivedBtn();
            } else if (i == R.id.navigateArea) {
                openNavigationDialog(passenger_lat, passenger_lon);
            }
        }
    }

    public void openNavigationDialog(final String passenger_lat, final String passenger_lon) {
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
                radiogmap.setChecked(true);
                radiowazemap.setChecked(false);
                googlemmapTxtView.performClick();

            }
        });
        radiowazemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiogmap.setChecked(false);
                radiowazemap.setChecked(true);
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
                    Utils.printLog("passenger_lat", passenger_lat + "");
                    Utils.printLog("passenger_lon", passenger_lon + "");
                    String url_view = "http://maps.google.com/maps?daddr=" + passenger_lat + "," + passenger_lon;
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

                    String uri = "waze://?ll=" + passenger_lat + "," + passenger_lon + "&navigate=yes";
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

}
