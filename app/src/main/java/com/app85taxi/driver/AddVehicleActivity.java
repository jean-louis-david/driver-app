package com.app85taxi.driver;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.SetOnTouchList;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddVehicleActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    MButton submitVehicleBtn;
    MaterialEditText makeBox, modelBox, yearBox, licencePlateBox, colorPlateBox;

    ArrayList<String> dataList = new ArrayList<>();
    android.support.v7.app.AlertDialog list_make;
    android.support.v7.app.AlertDialog list_model;
    android.support.v7.app.AlertDialog list_year;

    LinearLayout serviceSelectArea;

    String iSelectedMakeId = "";
    String iSelectedModelId = "";


    int iSelectedMakePosition = 0;

    JSONArray year_arr;
    JSONArray vehicletypelist;

    ArrayList<Boolean> carTypesStatusArr;

    String iDriverVehicleId = "";
    CheckBox checkboxHandicap;
    boolean ishandicapavilabel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = new GeneralFunctions(getActContext());

        backImgView = (ImageView) findViewById(R.id.backImgView);
        checkboxHandicap = (CheckBox) findViewById(R.id.checkboxHandicap);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        serviceSelectArea = (LinearLayout) findViewById(R.id.serviceSelectArea);

        submitVehicleBtn = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        makeBox = (MaterialEditText) findViewById(R.id.makeBox);
        modelBox = (MaterialEditText) findViewById(R.id.modelBox);
        yearBox = (MaterialEditText) findViewById(R.id.yearBox);
        licencePlateBox = (MaterialEditText) findViewById(R.id.licencePlateBox);
        colorPlateBox = (MaterialEditText) findViewById(R.id.colorPlateBox);
        try {

            String isHadicap = generalFunc.retrieveValue(CommonUtilities.HANDICAP_ACCESSIBILITY_OPTION);
            if (isHadicap != null && !isHadicap.equals("")) {
                if (isHadicap.equalsIgnoreCase("Yes")) {
                    checkboxHandicap.setVisibility(View.VISIBLE);
                } else {
                    checkboxHandicap.setVisibility(View.GONE);
                }
            } else {
                checkboxHandicap.setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        licencePlateBox.setFilters(new InputFilter[]{filter});

        backImgView.setOnClickListener(new setOnClickList());

        if (getIntent().getStringExtra("iDriverVehicleId") != null) {
            iDriverVehicleId = getIntent().getStringExtra("iDriverVehicleId");
            iSelectedMakeId = getIntent().getStringExtra("iMakeId");
            iSelectedModelId = getIntent().getStringExtra("iModelId");
            String vLicencePlate = getIntent().getStringExtra("vLicencePlate");
            String vColour = getIntent().getStringExtra("vColour");
            String iYear = getIntent().getStringExtra("iYear");
            String hadicap = getIntent().getStringExtra("eHandiCapAccessibility");

            if (hadicap.equalsIgnoreCase("yes")) {
                checkboxHandicap.setChecked(true);
            }

            licencePlateBox.setText(vLicencePlate);
            colorPlateBox.setText(vColour);
            yearBox.setText(iYear);
        }

        setLabels();
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_VEHICLE"));

        submitVehicleBtn.setId(Utils.generateViewId());
        submitVehicleBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));

        makeBox.setBothText(generalFunc.retrieveLangLBl("Make", "LBL_MAKE"));
        modelBox.setBothText(generalFunc.retrieveLangLBl("Model", "LBL_MODEL"));
        yearBox.setBothText(generalFunc.retrieveLangLBl("Year", "LBL_YEAR"));
        licencePlateBox.setBothText(generalFunc.retrieveLangLBl("Licence", "LBL_LICENCE_PLATE_TXT"));
        colorPlateBox.setBothText(generalFunc.retrieveLangLBl("Color", "LBL_COLOR_TXT"));

        checkboxHandicap.setText(generalFunc.retrieveLangLBl("Handicap accessibility available?", "LBL_HANDICAP_QUESTION"));

        backImgView.setOnClickListener(new setOnClickList());
        submitVehicleBtn.setOnClickListener(new setOnClickList());


        removeInput();
        buildMakeList();
    }

    private void removeInput() {
        Utils.removeInput(makeBox);
        Utils.removeInput(modelBox);
        Utils.removeInput(yearBox);

        makeBox.setOnTouchListener(new SetOnTouchList());
        modelBox.setOnTouchListener(new SetOnTouchList());
        yearBox.setOnTouchListener(new SetOnTouchList());

        makeBox.setOnClickListener(new setOnClickList());
        modelBox.setOnClickListener(new setOnClickList());
        yearBox.setOnClickListener(new setOnClickList());
    }

    public void buildMakeList() {
        dataList.clear();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getUserVehicleDetails");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {
                    dataList.clear();
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        JSONObject message_obj = generalFunc.getJsonObject("message", responseString);
                        year_arr = generalFunc.getJsonArray("year", message_obj.toString());
                        vehicletypelist = generalFunc.getJsonArray("vehicletypelist", message_obj.toString());

                        JSONArray carList_arr;
                        if (message_obj.length() > 0 && message_obj != null) {
                            carList_arr = generalFunc.getJsonArray("carlist", message_obj.toString());

                            if (carList_arr != null) {
                                for (int i = 0; i < carList_arr.length(); i++) {

                                    JSONObject obj = generalFunc.getJsonObject(carList_arr, i);
                                    dataList.add(obj.toString());
                                }
                            }
                        }

                        buildMake();

                        buildServices();

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

    public void buildMake() {
        ArrayList<String> items = new ArrayList<String>();

        for (int i = 0; i < dataList.size(); i++) {
            items.add(generalFunc.getJsonValue("vMake", dataList.get(i)));

            String iMakeId = generalFunc.getJsonValue("iMakeId", dataList.get(i));
            if (!iSelectedMakeId.equals("") && iSelectedMakeId.equals(iMakeId)) {
                iSelectedMakePosition = i;
                makeBox.setText(generalFunc.getJsonValue("vMake", dataList.get(i)));

                buildModelList(false);
            }
        }

        CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Make", "LBL_SELECT_MAKE"));

        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                if (list_make != null) {
                    list_make.dismiss();
                }
                makeBox.setText(generalFunc.getJsonValue("vMake", dataList.get(item)));
                iSelectedMakeId = generalFunc.getJsonValue("iMakeId", dataList.get(item));
                iSelectedMakePosition = item;

            }
        });

        list_make = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_make);
        }
    }
//    private void showMakeList() {
//
//        buildMake();
//
//        list_make.show();
//    }

    private void buildYear() {
        if (year_arr == null || year_arr.length() == 0) {
            return;
        }

        ArrayList<String> items = new ArrayList<String>();

        for (int i = 0; i < year_arr.length(); i++) {
            items.add((String) generalFunc.getValueFromJsonArr(year_arr, i));
        }

        CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Year", "LBL_SELECT_YEAR"));

        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                if (list_year != null) {
                    list_year.dismiss();
                }
                yearBox.setText((String) generalFunc.getValueFromJsonArr(year_arr, item));

            }
        });

        list_year = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_make);
        }

        list_year.show();
    }

    private void buildModelList(boolean isShow) {

        ArrayList<String> items = new ArrayList<String>();

        JSONArray vModellistArr = generalFunc.getJsonArray("vModellist", dataList.get(iSelectedMakePosition));
        if (vModellistArr != null) {
            for (int i = 0; i < vModellistArr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(vModellistArr, i);

                items.add(generalFunc.getJsonValue("vTitle", obj_temp.toString()));

                String iModelId = generalFunc.getJsonValue("iModelId", obj_temp.toString());
                if (!iSelectedModelId.equals("") && iSelectedModelId.equals(iModelId)) {
                    modelBox.setText(generalFunc.getJsonValue("vTitle", obj_temp.toString()));
                }
            }

            CharSequence[] cs_currency_txt = items.toArray(new CharSequence[items.size()]);

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
            builder.setTitle(generalFunc.retrieveLangLBl("Select Models", "LBL_SELECT_MODEL"));

            builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    // Do something with the selection

                    if (list_make != null) {
                        list_make.dismiss();
                    }
                    JSONArray vModellistArr = generalFunc.getJsonArray("vModellist", dataList.get(iSelectedMakePosition));
                    JSONObject obj_temp = generalFunc.getJsonObject(vModellistArr, item);

                    modelBox.setText(generalFunc.getJsonValue("vTitle", obj_temp.toString()));
                    iSelectedModelId = generalFunc.getJsonValue("iModelId", obj_temp.toString());
                }
            });

            list_model = builder.create();

            if (generalFunc.isRTLmode() == true) {
                generalFunc.forceRTLIfSupported(list_model);
            }

            if (isShow) {
                list_model.show();
            }
        }

    }

    public void buildServices() {

        if (serviceSelectArea.getChildCount() > 0) {
            serviceSelectArea.removeAllViewsInLayout();
        }

        carTypesStatusArr = new ArrayList<>();

        String[] vCarTypes = null;

        if (!iDriverVehicleId.equals("")) {
            vCarTypes = getIntent().getStringExtra("vCarType").split(",");
        }

        for (int i = 0; i < vehicletypelist.length(); i++) {
            JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

            LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_select_service_design, null);

            MTextView serviceNameTxtView = (MTextView) view.findViewById(R.id.serviceNameTxtView);
            serviceNameTxtView.setText(generalFunc.getJsonValue("vVehicleType", obj.toString()));


            AppCompatCheckBox chkBox = (AppCompatCheckBox) view.findViewById(R.id.chkBox);


            if (vCarTypes != null) {
                boolean status = getCarTypeStatus(vCarTypes, generalFunc.getJsonValue("iVehicleTypeId", obj.toString()));

                if (status == true) {
                    chkBox.setChecked(true);
                    carTypesStatusArr.add(true);
                } else {
                    carTypesStatusArr.add(false);
                }
            } else {

                carTypesStatusArr.add(false);
            }

            final int finalI = i;
            chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    carTypesStatusArr.set(finalI, b);
                }
            });

            serviceSelectArea.addView(view);

        }

    }

    public boolean getCarTypeStatus(String[] vCarTypes, String iVehicleTypeId) {

        for (int i = 0; i < vCarTypes.length; i++) {
            if (iVehicleTypeId.equals(vCarTypes[i])) {
                return true;
            }
        }
        return false;
    }

    public Context getActContext() {
        return AddVehicleActivity.this;
    }

    public void checkData() {

        if (iSelectedMakeId.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_MAKE"));
            return;
        }
        if (iSelectedModelId.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_VEHICLE_MODEL"));
            return;
        }

        if (Utils.getText(yearBox).equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_CHOOSE_YEAR"));
            return;
        }
        if (Utils.getText(licencePlateBox).equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Please add your car's licence plate no.", "LBL_ADD_LICENCE_PLATE"));
            return;
        }

        boolean isCarTypeSelected = false;

        String carTypes = "";

        for (int i = 0; i < carTypesStatusArr.size(); i++) {
            if (carTypesStatusArr.get(i) == true) {
                isCarTypeSelected = true;

                JSONObject obj = generalFunc.getJsonObject(vehicletypelist, i);

                String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", obj.toString());

                carTypes = carTypes.equals("") ? iVehicleTypeId : (carTypes + "," + iVehicleTypeId);
            }
        }

//        if (isCarTypeSelected == false) {
//            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl(".", "LBL_SELECT_CAR_TYPE"));
//            return;
//        }

        if (checkboxHandicap.isChecked()) {
            ishandicapavilabel = true;
        } else {
            ishandicapavilabel = false;
        }

        addVehicle(iSelectedMakeId, iSelectedModelId, carTypes);
    }

    public void addVehicle(String iMakeId, String iModelId, String vCarType) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverVehicle");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("iMakeId", iMakeId);
        parameters.put("iModelId", iModelId);
        parameters.put("iYear", Utils.getText(yearBox));
        parameters.put("vLicencePlate", Utils.getText(licencePlateBox));
        parameters.put("vCarType", vCarType);
        parameters.put("iDriverVehicleId", iDriverVehicleId);
        parameters.put("vColor", Utils.getText(colorPlateBox));
        String HandiCap = "";
        if (ishandicapavilabel) {
            HandiCap = "Yes";

        } else {
            HandiCap = "No";
        }
        parameters.put("HandiCap", HandiCap);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {
                    dataList.clear();
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                            @Override
                            public void handleBtnClick(int btn_id) {
                                generateAlert.closeAlertBox();

                                setResult(RESULT_OK);
                                backImgView.performClick();
                            }
                        });
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                        generateAlert.showAlertBox();

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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                AddVehicleActivity.super.onBackPressed();

            } else if (i == R.id.makeBox) {
                if (list_make == null) {
                    buildMake();
                    list_make.show();
                } else {
                    list_make.show();
                }
            } else if (i == R.id.modelBox) {
                buildModelList(true);
            } else if (i == R.id.yearBox) {
                if (list_year == null) {
                    buildYear();

                } else {
                    list_year.show();
                }
            } else if (i == submitVehicleBtn.getId()) {
                checkData();
            }
        }
    }
}
