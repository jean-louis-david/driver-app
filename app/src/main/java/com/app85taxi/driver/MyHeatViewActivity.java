package com.app85taxi.driver;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
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
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 23-01-2017.
 */
public class MyHeatViewActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GetLocationUpdates.LocationUpdates {

    public GeneralFunctions generalFunc;
    public String userProfileJson = "";
    String app_type = "Ride";
    ImageView userLocBtnImgView;
    MTextView titleTxt;
    SupportMapFragment map;
    GoogleMap gMap;
    ExecuteWebServerUrl heatMapAsyncTask;
    HashMap<String, String> onlinePassengerLocList = new HashMap<String, String>();
    HashMap<String, String> historyLocList = new HashMap<String, String>();
    ArrayList<TileOverlay> mapOverlayList = new ArrayList<>();
    double Radius_Map = 0;
    boolean isFirstLocation = true;
    boolean isFirstLocationUpdate = true;
    GetLocationUpdates getLastLocation;
    private ImageView backImgView;
    private Location userLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatview);

        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = getIntent().getStringExtra("UserProfileJson");
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.heatMapV2);

        getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true);
        getLastLocation.setLocationUpdatesListener(this);

        setLable();
        map.getMapAsync(MyHeatViewActivity.this);

        backImgView.setOnClickListener(new setOnClickList());
        userLocBtnImgView.setOnClickListener(new setOnClickList());

    }

    private void setLable() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MENU_MY_HEATVIEW"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingHeatMapProgressBar)).setVisibility(View.GONE);

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(true);
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(true);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(true);

            if (userLocation != null) {
                getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 16));
            }
            if (isFirstLocation == true) {
                isFirstLocation = false;
                VisibleRegion vr = getMap().getProjection().getVisibleRegion();
                final LatLng mainCenter = vr.latLngBounds.getCenter();
                final LatLng northeast = vr.latLngBounds.northeast;
                final LatLng southwest = vr.latLngBounds.southwest;

                final double radius_map = generalFunc.CalculationByLocation(mainCenter.latitude, mainCenter.longitude, southwest.latitude, southwest.longitude);
                getNearByPassenger(String.valueOf(radius_map), mainCenter.latitude, mainCenter.longitude);

                Radius_Map = radius_map;
            }

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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (this.userLocation == null) {
            return;
        }

        VisibleRegion vr = getMap().getProjection().getVisibleRegion();
        final LatLng mainCenter = vr.latLngBounds.getCenter();
        final LatLng northeast = vr.latLngBounds.northeast;
        final LatLng southwest = vr.latLngBounds.southwest;

        final double radius_map = generalFunc.CalculationByLocation(mainCenter.latitude, mainCenter.longitude, southwest.latitude, southwest.longitude);

        boolean isWithin1m = Radius_Map > radius_map + 0.001;

        if (isWithin1m == true)
            getNearByPassenger(String.valueOf(radius_map), mainCenter.latitude, mainCenter.longitude);

        Radius_Map = radius_map;
    }

    @Override
    public void onLocationUpdate(Location location) {

        this.userLocation = location;
        if (isFirstLocationUpdate == true) {
            isFirstLocationUpdate = false;
            if (userLocation == null) {
                return;
            }
            CameraPosition cameraPosition = cameraForUserPosition();
            if (cameraPosition != null)
                getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

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

    public Context getActContext() {
        return MyHeatViewActivity.this;
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

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);

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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                MyHeatViewActivity.super.onBackPressed();
            } else if (i == userLocBtnImgView.getId()) {
                if (userLocation == null) {
                    return;
                }
                CameraPosition cameraPosition = cameraForUserPosition();
                if (cameraPosition != null)
                    getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

}
