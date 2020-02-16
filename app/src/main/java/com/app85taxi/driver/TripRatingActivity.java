package com.app85taxi.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import java.util.HashMap;

public class TripRatingActivity extends AppCompatActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdates {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    GoogleMap gMap;

    Location userLocation;

    MButton btn_type2;
    MaterialEditText commentBox;

    SimpleRatingBar ratingBar;
    String iTripId_str;

    HashMap<String, String> data_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_rating);

        generalFunc = new GeneralFunctions(getActContext());

        data_trip = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        iTripId_str = data_trip.get("TripId");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        map.getMapAsync(this);

        GetLocationUpdates getLocationUpdates = new GetLocationUpdates(getActContext(), 10);
        getLocationUpdates.setLocationUpdatesListener(this);

        (findViewById(R.id.backImgView)).setVisibility(View.GONE);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        commentBox.setSingleLine(false);
        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commentBox.setGravity(Gravity.TOP);
        // commentBox.setHideUnderline(true);
        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NONE);

        setLabels();
//        new CreateRoundedView(Color.parseColor("#FFFFFF"), Utils.dipToPixels(getActContext(), 4),
//                Utils.dipToPixels(getActContext(), 2), Color.parseColor("#d2d2d2"), findViewById(R.id.commentArea));

        ((MTextView) findViewById(R.id.nameTxt)).setText(data_trip.get("PName"));

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        ((MTextView) findViewById(R.id.rateTxt)).setText(generalFunc.retrieveLangLBl("Rate", "LBL_RATE"));
        commentBox.setHint(generalFunc.retrieveLangLBl("", "LBL_WRITE_COMMENT_HINT_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == btn_type2.getId()) {
                if (ratingBar.getRating() < 1) {
                    generalFunc.showMessage(generalFunc.getCurrentView(TripRatingActivity.this),
                            generalFunc.retrieveLangLBl("", "LBL_ERROR_RATING_DIALOG_TXT"));
                    return;
                }
                submitRating();
            }
        }
    }

    public void submitRating() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "submitRating");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("tripID", iTripId_str);
        parameters.put("rating", "" + ratingBar.getRating() + "");
        parameters.put("message", Utils.getText(commentBox));
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

//                        buildTripEndMessage(generalFunc.retrieveLangLBl("", "LBL_TRIP_FINISHED_TXT"),
//                                generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), true);

                        showBookingAlert(generalFunc.retrieveLangLBl("", "LBL_TRIP_FINISHED_TXT"));

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

    public void showBookingAlert(String message) {
        android.support.v7.app.AlertDialog alertDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_booking_view, null);
        builder.setView(dialogView);

        final MTextView titleTxt = (MTextView) dialogView.findViewById(R.id.titleTxt);
        final MTextView mesasgeTxt = (MTextView) dialogView.findViewById(R.id.mesasgeTxt);


        titleTxt.setText(generalFunc.retrieveLangLBl("Booking Successful", "LBL_SUCCESS_FINISHED"));
        mesasgeTxt.setText(message);


        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_OK_THANKS"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                generalFunc.saveGoOnlineInfo();

                //generalFunc.restartApp();
                generalFunc.restartwithGetDataApp();

            }
        });


        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


//        final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        negativeButton.setTextColor(getResources().getColor(R.color.black));

    }

    public void buildTripEndMessage(String message, String positiveBtn, final boolean isRestart) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                generalFunc.saveGoOnlineInfo();
                if (isRestart == true) {
                    generalFunc.restartApp();
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();
    }


    public Context getActContext() {
        return TripRatingActivity.this;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;
        CameraPosition cameraPosition = cameraForUserPosition();
        getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

    public GoogleMap getMap() {
        return this.gMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(true);
        }

        getMap().getUiSettings().setTiltGesturesEnabled(false);
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);
    }
}
