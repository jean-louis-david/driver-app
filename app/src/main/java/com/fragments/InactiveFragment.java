package com.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adapter.files.InactiveRecycleAdapter;
import com.app85taxi.driver.ListOfDocumentActivity;
import com.app85taxi.driver.MainActivity;
import com.app85taxi.driver.ManageVehiclesActivity;
import com.app85taxi.driver.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class InactiveFragment extends Fragment implements InactiveRecycleAdapter.OnItemClickList {


    View view;
    private RecyclerView mRecyclerView;

    ArrayList<HashMap<String, String>> list;

    InactiveRecycleAdapter inactiveRecycleAdapter;

    GeneralFunctions generalFunc;

    MainActivity mainActivity;

    boolean isdocprogress = false;
    boolean isvehicleprogress = false;
    boolean isdriveractive = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inactive, container, false);

        mainActivity = (MainActivity) getActivity();
        generalFunc = mainActivity.generalFunc;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.inActiveRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    private void setData() {
        list = new ArrayList<>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("title", generalFunc.retrieveLangLBl("Registration Successful", "LBL_REGISTRATION_SUCCESS"));
        map1.put("msg", "");
        map1.put("btn", "");
        map1.put("line", "start");
        map1.put("state", "true");
        list.add(map1);

        HashMap<String, String> map2 = new HashMap<String, String>();
        if (!isdocprogress) {
            map2.put("title", generalFunc.retrieveLangLBl("Upload your documents", "LBL_UPLOAD_YOUR_DOCS"));
            map2.put("msg", generalFunc.retrieveLangLBl("We need to verify your driving documents to activate your account.", "LBL_UPLOAD_YOUR_DOCS_NOTE"));
            map2.put("btn", generalFunc.retrieveLangLBl("Upload Document", "LBL_UPLOAD_DOC"));
            map2.put("line", "two");
            map2.put("state", isdocprogress + "");
        } else {
            map2.put("title", generalFunc.retrieveLangLBl("Upload your Documents Successful", "LBL_UPLOADDOC_SUCCESS"));
            map2.put("msg", "");
            map2.put("btn", "");
            map2.put("line", "two");
            map2.put("state", isdocprogress + "");

        }
        list.add(map2);

        HashMap<String, String> map3 = new HashMap<String, String>();
        if (!isvehicleprogress) {
            map3.put("title", generalFunc.retrieveLangLBl("Add vehicles with document", "LBL_ADD_VEHICLE_AND_DOC"));
            map3.put("msg", generalFunc.retrieveLangLBl("Please add your vehicles and its document. After that we will verify its registration.", "LBL_ADD_VEHICLE_AND_DOC_NOTE"));
            map3.put("btn", generalFunc.retrieveLangLBl("Add Vehicle", "LBL_ADD_VEHICLE"));
            map3.put("line", "three");
            map3.put("state", isvehicleprogress + "");
        } else {
            map3.put("title", generalFunc.retrieveLangLBl("Your vehicle added successfully.", "LBL_VEHICLE_ADD_SUCCESS"));
            map3.put("msg", "");
            map3.put("btn", "");
            map3.put("line", "three");
            map3.put("state", isvehicleprogress + "");

        }
        list.add(map3);

        HashMap<String, String> map4 = new HashMap<String, String>();
        map4.put("title", generalFunc.retrieveLangLBl("Waiting for admin's approval", "LBL_WAIT_ADMIN_APPROVE"));
        map4.put("msg", generalFunc.retrieveLangLBl("We will check your provided information and get back to you soon.", "LBL_WAIT_ADMIN_APPROVE_NOTE"));
        map4.put("btn", "");
        map4.put("line", "end");
        map4.put("state", isdriveractive + "");
        list.add(map4);

        inactiveRecycleAdapter = new InactiveRecycleAdapter(mainActivity.getActContext(), list, mainActivity.generalFunc);
        mRecyclerView.setAdapter(inactiveRecycleAdapter);
        inactiveRecycleAdapter.setOnItemClickList(this);
    }

    @Override
    public void onItemClick(int position) {

        if (position == 1) {
            //open upload doc activity
            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Driver");
            bn.putString("iDriverVehicleId", "");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            new StartActProcess(mainActivity.getActContext()).startActWithData(ListOfDocumentActivity.class, bn);

        } else if (position == 2) {
            //open add vehicle activity
            //(new StartActProcess(mainActivity.getActContext())).startAct(AddVehicleActivity.class);
            Bundle bn = new Bundle();
            new StartActProcess(mainActivity.getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getDriverStateDetails();
    }

    public void getDriverStateDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverStates");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mainActivity.getActContext(), parameters);
        exeWebServer.setLoaderConfig(mainActivity.getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        if (generalFunc.getJsonValue("IS_DOCUMENT_PROCESS_COMPLETED", responseString).equalsIgnoreCase("yes")) {
                            isdocprogress = true;

                        } else {
                            isdocprogress = false;

                        }
                        if (generalFunc.getJsonValue("IS_VEHICLE_PROCESS_COMPLETED", responseString).equalsIgnoreCase("yes")) {
                            isvehicleprogress = true;

                        } else {
                            isvehicleprogress = false;

                        }
                        if (generalFunc.getJsonValue("IS_DRIVER_STATE_ACTIVATED", responseString).equalsIgnoreCase("yes")) {
                            isdriveractive = true;
                        } else {
                            isdriveractive = false;

                        }

                        setData();


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
}
