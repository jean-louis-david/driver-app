package com.app85taxi.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.StartActProcess;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

public class SearchPickupLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GetAddressFromLocation.AddressFound {

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    MButton btn_type2;
    int btnId;
    MTextView placeTxtView;
    boolean isPlaceSelected = false;
    LatLng placeLocation;
    Marker placeMarker;
    SupportMapFragment map;
    GoogleMap gMap;
    GetAddressFromLocation getAddressFromLocation;
    private String TAG = SearchPickupLocationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pickup_location);

        generalFunc = new GeneralFunctions(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        setLabels();

        map.getMapAsync(SearchPickupLocationActivity.this);

        backImgView.setOnClickListener(new setOnClickAct());
        btnId = Utils.generateViewId();
        btn_type2.setId(btnId);

        btn_type2.setOnClickListener(new setOnClickAct());
        (findViewById(R.id.pickUpLocSearchArea)).setOnClickListener(new setOnClickAct());

        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this), generalFunc.retrieveLangLBl("", "LBL_LONG_TOUCH_CHANGE_LOC_TXT"));

    }

    public void setLabels() {
        if (getIntent().getStringExtra("isPickUpLoc") != null && getIntent().getStringExtra("isPickUpLoc").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_BIG_TXT"));
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_HEADER_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION_HEADER_TXT"));
        }

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));

    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude) {
        placeTxtView.setText(address);
        isPlaceSelected = true;
        this.placeLocation = new LatLng(latitude, longitude);

        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

        if (gMap != null) {
            gMap.clear();
            placeMarker = gMap.addMarker(new MarkerOptions().position(this.placeLocation).title(address));
            gMap.moveCamera(cu);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;

        if (getIntent().getStringExtra("isPickUpLoc") != null && getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

            LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14);

            gMap.moveCamera(cu);

        }

        this.gMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                getAddressFromLocation.setLocation(latLng.latitude, latLng.longitude);
                getAddressFromLocation.setLoaderEnable(true);
                getAddressFromLocation.execute();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Utils.printLog(TAG, "Place:" + place.toString());

                placeTxtView.setText(place.getAddress());
                isPlaceSelected = true;
                LatLng placeLocation = place.getLatLng();

                this.placeLocation = placeLocation;

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14.0f);

                if (gMap != null) {
                    gMap.clear();
                    placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
                    gMap.moveCamera(cu);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Utils.printLog(TAG, status.getStatusMessage());

                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    public Context getActContext() {
        return SearchPickupLocationActivity.this;
    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                SearchPickupLocationActivity.super.onBackPressed();

            } else if (i == R.id.pickUpLocSearchArea) {

                try {
                    LatLngBounds bounds = null;

                    if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                        LatLng pickupPlaceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                                generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                        bounds = new LatLngBounds(pickupPlaceLocation, pickupPlaceLocation);
                    }

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setBoundsBias(bounds)
                            .build(SearchPickupLocationActivity.this);
                    startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                            generalFunc.retrieveLangLBl("", "LBL_SERVICE_NOT_AVAIL_TXT"));
                }
            } else if (i == btnId) {

                if (isPlaceSelected == false) {
                    generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                            generalFunc.retrieveLangLBl("Please set location.", "LBL_SET_LOCATION"));
                    return;
                }

                Bundle bn = new Bundle();
                bn.putString("Address", placeTxtView.getText().toString());
                bn.putString("Latitude", "" + placeLocation.latitude);
                bn.putString("Longitude", "" + placeLocation.longitude);

                new StartActProcess(getActContext()).setOkResult(bn);
                backImgView.performClick();
            }
        }
    }


}
