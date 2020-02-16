package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.app85taxi.driver.ChatActivity;
import com.app85taxi.driver.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import java.util.HashMap;

public class OpenPassengerDetailDialog {

    Context mContext;
    HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;

    android.support.v7.app.AlertDialog alertDialog;

    private FirebaseAuth auth;
    final FirebaseUser user;
    ProgressBar LoadingProgressBar;
    boolean isnotification;

    public OpenPassengerDetailDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, boolean isnotification) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        this.isnotification = isnotification;

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        show();
    }

    public void show() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_passenger_detail_dialog, null);
        builder.setView(dialogView);

        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));

        ((MTextView) dialogView.findViewById(R.id.rateTxt)).setText(generalFunc.convertNumberWithRTL(data_trip.get("PRating")));
        ((MTextView) dialogView.findViewById(R.id.nameTxt)).setText(data_trip.get("PName"));

        ((MTextView) dialogView.findViewById(R.id.passengerDTxt)).setText(generalFunc.retrieveLangLBl("Passenger Detail", "LBL_PASSENGER_DETAIL"));
        ((MTextView) dialogView.findViewById(R.id.callTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.msgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((SimpleRatingBar) dialogView.findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));

        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/" + data_trip.get("PassengerId") + "/"
                + data_trip.get("PPicName");

        Picasso.with(mContext)
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.passengerImgView)));

        (dialogView.findViewById(R.id.callArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" +/* data_trip.get("PPhoneC") + "" +*/ data_trip.get("PPhone")));
                    mContext.startActivity(callIntent);

                } catch (Exception e) {
                }
            }
        });
        (dialogView.findViewById(R.id.msgArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//                    smsIntent.setType("vnd.android-dir/mms-sms");
//                    smsIntent.putExtra("address", "" + data_trip.get("PPhoneC") + "" + data_trip.get("PPhone"));
//                    mContext.startActivity(smsIntent);
//                } catch (Exception e) {
//
//                }

                /*if (user == null) {
                    signupForChate(data_trip);
                } else {
                    doLoginForChat(data_trip);
                }*/

                // new OpenChatDetailDialog(mContext, data_trip, generalFunc, "");
//                Bundle bn=new Bundle();
//                bn.putSerializable("tripdata",data_trip);
//                new StartActProcess(mContext).startActWithData(ChatActivity.class,bn);

                Bundle bnChat = new Bundle();

                bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                bnChat.putString("iTripId", data_trip.get("iTripId"));
                bnChat.putString("FromMemberName", data_trip.get("PName"));

                new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);




               /* LoadingProgressBar.setVisibility(View.VISIBLE);

                auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (user!=null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(data_trip.get("drivervName")+" "+data_trip.get("drivervLastName"))
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Api", "User profile updated.");
                                            }
                                        }
                                    });
                        }
                        LoadingProgressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {

//                            Toast.makeText(MainActivity.this,"Authentication failed.", Toast.LENGTH_LONG).show();
                            generalFunc.showError();
                        } else {
                            new OpenChatDetailDialog(mContext, data_trip, generalFunc, "");
                        }

                    }
                });*/

            }
        });

//        if (data_trip.containsKey("PPetId") && !TextUtils.isEmpty(data_trip.get("PPetId"))) {
//
//            (dialogView.findViewById(R.id.passengerDetailArea)).setVisibility(View.GONE);
//
//            ((MTextView) dialogView.findViewById(R.id.rateTxt1)).setText(data_trip.get("PRating"));
//            ((MTextView) dialogView.findViewById(R.id.nameTxt1)).setText(data_trip.get("PName"));
//
//            Picasso.with(mContext)
//                    .load(image_url)
//                    .placeholder(R.mipmap.ic_no_pic_user)
//                    .error(R.mipmap.ic_no_pic_user)
//                    .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.passengerImgView1)));
//        }
        (dialogView.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
        if (isnotification) {
            isnotification = false;
            dialogView.findViewById(R.id.msgArea).performClick();
        }
    }

    private void doLoginForChat(final HashMap<String, String> tripDataMap) {

          /*  try to login */
        LoadingProgressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(tripDataMap.get("TripId") + "Driver" + generalFunc.getMemberId() + "@gmail.com", "Driver_" + tripDataMap.get("TripId") + generalFunc.getMemberId())
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
//                                    progressBar.setVisibility(View.GONE);
                        LoadingProgressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
//                                        if (password.length() < 6) {
//                                            inputPassword.setError(getString(R.string.minimum_password));
//                                        } else {
//                            Toast.makeText(mContext,"Authentication failed.", Toast.LENGTH_LONG).show();
//                                        }

                            signupForChate(tripDataMap);
                        } else {
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                            // new OpenChatDetailDialog(mContext, tripDataMap, generalFunc, "");
//                            Bundle bn=new Bundle();
//                            bn.putSerializable("tripdata",tripDataMap);


                            Bundle bnChat = new Bundle();

                            bnChat.putString("iFromMemberId", tripDataMap.get("PassengerId"));
                            bnChat.putString("FromMemberImageName", tripDataMap.get("PPicName"));
                            bnChat.putString("iTripId", tripDataMap.get("iTripId"));
                            bnChat.putString("FromMemberName", tripDataMap.get("PName"));

                            new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);


                            // new StartActProcess(mContext).startActWithData(ChatActivity.class,bn);

                        }
                    }
                });
    }

    private void signupForChate(final HashMap<String, String> tripDataMap) {
        LoadingProgressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(tripDataMap.get("TripId") + "Driver" + generalFunc.getMemberId() + "@gmail.com", "Driver_" + tripDataMap.get("TripId") + generalFunc.getMemberId())
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(mContext, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
//                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        // Handle Errors here.
                        LoadingProgressBar.setVisibility(View.GONE);

                        if (!task.isSuccessful()) {
//                            Toast.makeText(mContext, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                            doLoginForChat(data_trip);
                        } else {
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                            // new OpenChatDetailDialog(mContext, tripDataMap, generalFunc, "");
//                            Bundle bn=new Bundle();
//                            bn.putSerializable("tripdata",tripDataMap);
//                            new StartActProcess(mContext).startActWithData(ChatActivity.class,bn);

                            Bundle bnChat = new Bundle();
                            bnChat.putString("iFromMemberId", tripDataMap.get("PassengerId"));
                            bnChat.putString("FromMemberImageName", tripDataMap.get("PPicName"));
                            bnChat.putString("iTripId", tripDataMap.get("iTripId"));
                            bnChat.putString("FromMemberName", tripDataMap.get("PName"));
                            new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);


                        }
                    }
                });

    }
}
