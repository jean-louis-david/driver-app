package com.general.files;

import android.content.Context;
import android.location.Location;

import com.app85taxi.driver.ActiveTripActivity;
import com.app85taxi.driver.DriverArrivedActivity;
import com.app85taxi.driver.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Admin on 02-08-2017.
 */

public class UpdateDirections implements GetLocationUpdates.LocationUpdates, UpdateFrequentTask.OnTaskRunCalled {

    public GoogleMap googleMap;
    public Location destinationLocation;
    public Context mcontext;
    public Location userLocation;

    GeneralFunctions generalFunctions;

    String serverKey;
    Polyline route_polyLine;

    GetLocationUpdates getLocUpdates;
    UpdateFrequentTask updateFreqTask;
    Marker driverMarker;

    public UpdateDirections(Context mcontext, GoogleMap googleMap, Location userLocation, Location destinationLocation) {
        this.googleMap = googleMap;
        this.destinationLocation = destinationLocation;
        this.mcontext = mcontext;
        this.userLocation = userLocation;
        generalFunctions = new GeneralFunctions(mcontext);

        getLocUpdates = new GetLocationUpdates(mcontext, Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true);
        getLocUpdates.setLocationUpdatesListener(this);

        if (mcontext instanceof DriverArrivedActivity) {

        } else if (mcontext instanceof ActiveTripActivity) {

        }

    }

    public void scheduleDirectionUpdate() {

        String DESTINATION_UPDATE_TIME_INTERVAL = generalFunctions.retrieveValue("DESTINATION_UPDATE_TIME_INTERVAL");
        Utils.printLog("UpdateDirection", "DESTINATION_UPDATE_TIME_INTERVAL:" + DESTINATION_UPDATE_TIME_INTERVAL);
        Utils.printLog("UpdateDirection", "DESTINATION_UPDATE_TIME_INTERVAL_VALUE:" + ((int) (generalFunctions.parseDoubleValue(2, DESTINATION_UPDATE_TIME_INTERVAL) * 60 * 1000)));
        updateFreqTask = new UpdateFrequentTask((int) (generalFunctions.parseDoubleValue(2, DESTINATION_UPDATE_TIME_INTERVAL) * 60 * 1000));
        updateFreqTask.setTaskRunListener(this);
        updateFreqTask.startRepeatingTask();
    }

    public void releaseTask() {
        if (updateFreqTask != null) {
            updateFreqTask.stopRepeatingTask();
            updateFreqTask = null;
        }

        if (getLocUpdates != null) {
            getLocUpdates.stopLocationUpdates();
            getLocUpdates = null;
        }

        Utils.runGC();
    }


    public void updateDirections() {
        Utils.printLog("UpdateDirection", "Called:" + userLocation.getLatitude() + ":" + userLocation.getLongitude() + ":destLoc:" + destinationLocation.getLatitude() + ":" + destinationLocation.getLongitude());
        serverKey = mcontext.getResources().getString(R.string.google_api_get_address_from_location_serverApi);
//        if (googleMap == null || destinationLocation == null  || destinationLocation.getLatitude() != 0.0 || destinationLocation.getLongitude() != 0.0) {
//            return;
//        }

//        if (mcontext instanceof ActiveTripActivity) {
//            ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
//            if (activeTripActivity.userLocation != null) {
//                this.userLocation = userLocation;
//            }
//
//        }

        String originLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destLoc = destinationLocation.getLatitude() + "," + destinationLocation.getLongitude();

        String directionURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + generalFunctions.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mcontext, directionURL, true);


        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    String status = generalFunctions.getJsonValue("status", responseString);

                    JSONArray jRoutes = null;
                    JSONArray jLegs = null;
                    if (status.equals("OK")) {
                        try {
                            JSONArray obj_routes = generalFunctions.getJsonArray("routes", responseString);
                            if (obj_routes != null && obj_routes.length() > 0) {
                                JSONObject obj_legs = generalFunctions.getJsonObject(generalFunctions.getJsonArray("legs", generalFunctions.getJsonObject(obj_routes, 0).toString()), 0);


                                String distance = "" + generalFunctions.getJsonValue("text",
                                        generalFunctions.getJsonValue("distance", obj_legs.toString()).toString());
                                String time = "" + generalFunctions.getJsonValue("text",
                                        generalFunctions.getJsonValue("duration", obj_legs.toString()).toString());

                                if (mcontext instanceof DriverArrivedActivity) {

                                    DriverArrivedActivity driverArrivedActivity = (DriverArrivedActivity) mcontext;
                                    driverArrivedActivity.setTimetext(distance, time);


                                } else if (mcontext instanceof ActiveTripActivity) {
                                    ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
                                    activeTripActivity.setTimetext(distance, time);
                                }
                            }
                        } catch (Exception e) {

                        }

                        PolylineOptions lineOptions = generalFunctions.getGoogleRouteOptions(responseString, Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.appThemeColor_2));

                        if (lineOptions != null) {
                            if (route_polyLine != null) {
                                route_polyLine.remove();
                            }
                            route_polyLine = googleMap.addPolyline(lineOptions);

                        }

                    } else {
                        // Notify getchya that Route is not drawn.
//                        generalFunctions.showGeneralMessage("", generalFunctions.retrieveLangLBl("Route drawn failed", "LBL_ROUTE_DRAW_FAILED"));
                    }

                } else {
                    // Notify getchya that Route is not drawn.

//                    generalFunctions.showGeneralMessage("", generalFunctions.retrieveLangLBl("Route drawn failed", "LBL_ROUTE_DRAW_FAILED"));
                }
            }
        });
        exeWebServer.execute();
    }


    @Override
    public void onLocationUpdate(Location location) {

        if (userLocation == null) {
            userLocation = location;
            updateDirections();
        }


        userLocation = location;

        if (userLocation == null) {
            Utils.printLog("updatedirection location", userLocation.getLatitude() + "," + userLocation.getLongitude());
        }

    }

    @Override
    public void onTaskRun() {
        Utils.printLog("updatedirection", ":: Ontask call");
        updateDirections();
    }
}
