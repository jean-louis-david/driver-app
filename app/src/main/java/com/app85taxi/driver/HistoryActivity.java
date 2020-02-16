package com.app85taxi.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.BookingFragment;
import com.fragments.RideHistoryFragment;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MaterialTabs;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    String userProfileJson;
    CharSequence[] titles;
    String app_type = "Ride";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        ArrayList<Fragment> fragmentList = new ArrayList<>();


        titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
        material_tabs.setVisibility(View.VISIBLE);
        fragmentList.add(generateBookingFragHistory(Utils.Past));
        fragmentList.add(generateBookingFrag(Utils.Upcoming));


        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

    }

    public void finishScreens() {
        ActivityCompat.finishAffinity(HistoryActivity.this);
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS"));
    }


    @Override
    protected void onResume() {

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        super.onResume();

    }

    public Context getActContext() {
        return HistoryActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    HistoryActivity.super.onBackPressed();
                    break;

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK)
        {
            ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
            MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

            userProfileJson=data.getStringExtra("UserProfileJson");
            app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
            ArrayList<Fragment> fragmentList = new ArrayList<>();

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
            appLogin_view_pager.setAdapter(adapter);
            material_tabs.setViewPager(appLogin_view_pager);


        }
    }

    public BookingFragment generateBookingFrag(String bookingType) {
        BookingFragment frag = new BookingFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);
        frag.setArguments(bn);
        return frag;
    }



    public RideHistoryFragment generateBookingFragHistory(String bookingType) {
        RideHistoryFragment frag = new RideHistoryFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);
        frag.setArguments(bn);
        return frag;
    }




}
