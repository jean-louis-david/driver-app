package com.app85taxi.driver;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.ChatMessage;
import com.adapter.files.ChatMessagesRecycleAdapter;
import com.firebase.ui.database.FirebaseListAdapter;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.TripMessageReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    Context mContext;
    // HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;
    String isFrom = "";
    private FirebaseListAdapter<ChatMessage> adapter;

    EditText input;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private ChatMessagesRecycleAdapter chatAdapter;
    private ArrayList<ChatMessage> chatList;
    private int count = 0;
    ProgressBar LoadingProgressBar;
    HashMap<String, String> data_trip_ada;
    TripMessageReceiver tripMsgReceiver;
    ConfigPubNub configPubNub;
    GenerateAlertBox generateAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_trip_chat_detail_dialog);
        mContext = ChatActivity.this;

        generalFunc = new GeneralFunctions(ChatActivity.this);

        //  data_trip = (HashMap<String, String>) getIntent().getSerializableExtra("tripdata");
        tripMsgReceiver = new TripMessageReceiver((Activity) ChatActivity.this, true);

        data_trip_ada = new HashMap<>();
        data_trip_ada.put("iFromMemberId", getIntent().getStringExtra("iFromMemberId"));
        data_trip_ada.put("FromMemberImageName", getIntent().getStringExtra("FromMemberImageName"));
        data_trip_ada.put("iTripId", getIntent().getStringExtra("iTripId"));
        data_trip_ada.put("FromMemberName", getIntent().getStringExtra("FromMemberName"));


        Utils.printLog("data_trip_ada_list", data_trip_ada.toString());


        chatList = new ArrayList<>();
        count = 0;
        registerTripMsgReceiver();
        show();

    }

    public boolean isPubNubEnabled() {
        String ENABLE_PUBNUB = generalFunc.retrieveValue(Utils.ENABLE_PUBNUB_KEY);

        return ENABLE_PUBNUB.equalsIgnoreCase("Yes");
    }


    public void registerTripMsgReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonUtilities.passenger_message_arrived_intent_action_trip_msg);

        registerReceiver(tripMsgReceiver, filter);

        if (isPubNubEnabled()) {
            configPubNub = new ConfigPubNub(ChatActivity.this);
        }
    }

    public void tripCancelled() {

        if (generateAlert != null) {
            generateAlert.closeAlertBox();
        }
        generateAlert = new GenerateAlertBox(ChatActivity.this);

        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                generalFunc.saveGoOnlineInfo();
                generalFunc.restartApp();
                // generalFunc.restartwithGetDataApp();
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));


        generateAlert.showAlertBox();
    }

    public void unRegisterReceiver() {
        if (tripMsgReceiver != null) {
            try {
                unregisterReceiver(tripMsgReceiver);
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onDestroy() {
        stopPubNub();

        super.onDestroy();
    }


    public void stopPubNub() {
        if (configPubNub != null) {
            configPubNub.unSubscribeToPrivateChannel();
            configPubNub = null;
            Utils.runGC();
        }
        unRegisterReceiver();
    }

    public void show() {


        ImageView msgbtn = (ImageView) findViewById(R.id.msgbtn);
        input = (EditText) findViewById(R.id.input);

        input.setHint(generalFunc.retrieveLangLBl("Enter a message", "LBL_ENTER_MSG_TXT"));


        (findViewById(R.id.backImgView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(ChatActivity.this);
                onBackPressed();
            }
        });

        msgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (Utils.checkText(input)) {
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    FirebaseDatabase.getInstance()
                            .getReference("cubetaxiplus-app")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString().trim(),
                                    data_trip_ada.get("FromMemberName"), generalFunc.getMemberId() + "_" + data_trip_ada.get("iTripId") + "_" + CommonUtilities.app_type)
                            );


                    sendTripMessageNotification(input.getText().toString().trim());

                    // Clear the input
                    input.setText("");


                } else {

                }

            }
        });

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("cubetaxiplus-app");

        final RecyclerView chatCategoryRecyclerView = (RecyclerView) findViewById(R.id.chatCategoryRecyclerView);


        chatAdapter = new ChatMessagesRecycleAdapter(mContext, chatList, generalFunc, data_trip_ada);
        chatCategoryRecyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

        ((MTextView) findViewById(R.id.titleTxt)).setText(data_trip_ada.get("FromMemberName"));

        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference("cubetaxiplus-app");
        com.google.firebase.database.ChildEventListener childEventListener = new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                Log.d("Api", "onChildAdded:" + dataSnapshot.getKey());
                count++;
                // A new comment has been added, add it to the displayed list
                ChatMessage comment = dataSnapshot.getValue(ChatMessage.class);
                Log.d("Api", "onChildAdded:" + comment.getMessageId());

                try {
                    if (comment.getMessageId().equals(generalFunc.getMemberId() + "_" + data_trip_ada.get("iTripId") + "_" + CommonUtilities.app_type) || comment.getMessageId().equals(data_trip_ada.get("iFromMemberId") + "_" + data_trip_ada.get("iTripId") + "_Passenger")) {
                        chatList.add(comment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (count >= dataSnapshot.getChildrenCount()) {
                    //stop progress bar here
                    ((ProgressBar) findViewById(R.id.ProgressBar)).setVisibility(View.GONE);

                }

                chatAdapter.notifyDataSetChanged();
                ((ProgressBar) findViewById(R.id.ProgressBar)).setVisibility(View.GONE);
                chatCategoryRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                Log.d("Api", "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                ChatMessage newComment = dataSnapshot.getValue(ChatMessage.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d("Api", "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//                Log.d("Api", "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                ChatMessage movedComment = dataSnapshot.getValue(ChatMessage.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.w("Api", "postComments:onCancelled", databaseError.toException());
//                Toast.makeText(mContext, "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        };
        mainRef.addChildEventListener(childEventListener);


    }


    public void sendTripMessageNotification(String message) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "SendTripMessageNotification");
        parameters.put("UserType", Utils.userType);
        parameters.put("iFromMemberId", generalFunc.getMemberId());
        parameters.put("iTripId", data_trip_ada.get("iTripId"));
        parameters.put("iToMemberId", data_trip_ada.get("iFromMemberId"));
        parameters.put("tMessage", message);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, false, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

            }
        });
        exeWebServer.execute();
    }

    public String lastChars(String a, String b) {
        String str1 = "";
        if (a.length() >= 1) {
            str1 = a.substring(b.length() - 1);
        }
        return str1;
    }
}
