package com.general.files;

import android.content.Context;
import android.os.Handler;

import com.app85taxi.driver.MainActivity;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Admin on 05-10-2016.
 */
public class ConfigPubNub {
    Context mContext;
    PubNub pubnub;
    GeneralFunctions generalFunc;

    public ConfigPubNub(Context mContext) {
        this.mContext = mContext;

        generalFunc = new GeneralFunctions(mContext);

        Utils.printLog("SUB KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        Utils.printLog("PUB KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        Utils.printLog("SEC KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));

        PNConfiguration pnConfiguration = new PNConfiguration();
//        pnConfiguration.setSubscribeKey(Utils.pubNub_sub_key);
//        pnConfiguration.setPublishKey(Utils.pubNub_pub_key);
//        pnConfiguration.setSecretKey(Utils.pubNub_sec_key);
        Utils.printLog("sessionid",generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY));
        pnConfiguration.setUuid(generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY).equals("") ? generalFunc.getMemberId() : generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY));
        pnConfiguration.setSubscribeKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        pnConfiguration.setPublishKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        pnConfiguration.setSecretKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);

        pubnub = new PubNub(pnConfiguration);


        addListener();
        subscribeToPrivateChannel();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pubnub.reconnect();
            }
        }, 10000);
    }

    public ConfigPubNub(Context mContext, boolean isOnlyPublish) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);

        Utils.printLog("SUB KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        Utils.printLog("PUB KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        Utils.printLog("SEC KEy:", "::" + generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SUB_KEY));
        pnConfiguration.setPublishKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_PUB_KEY));
        pnConfiguration.setSecretKey(generalFunc.retrieveValue(CommonUtilities.PUBNUB_SEC_KEY));
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);

        pubnub = new PubNub(pnConfiguration);


//        addListener();
//        subscribeToPrivateChannel();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pubnub.reconnect();
            }
        }, 10000);
    }

    public void subscribeToPrivateChannel() {
        pubnub.subscribe()
                .channels(Arrays.asList("DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }

    public void unSubscribeToPrivateChannel() {
        pubnub.unsubscribe()
                .channels(Arrays.asList("DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }

    public void releaseInstances() {
        pubnub.removeListener(subscribeCallback);
    }

    public void addListener() {
        releaseInstances();
        pubnub.addListener(subscribeCallback);
        pubnub.reconnect();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pubnub.reconnect();
            }
        }, 10000);
    }


    SubscribeCallback subscribeCallback = new SubscribeCallback() {
        @Override
        public void status(final PubNub pubnub, final PNStatus status) {
            // the status object returned is always related to subscribe but could contain
            // information about subscribe, heartbeat, or errors
            // use the operationType to switch on different options
            if (status == null || status.getOperation() == null) {
                 Utils.printLog("status operation", ":::re connected::" + status.toString());
                pubnub.reconnect();
                return;
            }

            switch (status.getOperation()) {
                // let's combine unsubscribe and subscribe handling for ease of use
                case PNSubscribeOperation:
                case PNUnsubscribeOperation:
                    // note: subscribe statuses never have traditional
                    // errors, they just have categories to represent the
                    // different issues or successes that occur as part of subscribe
                    switch (status.getCategory()) {
                        case PNConnectedCategory:
                            // this is expected for a subscribe, this means there is no error or issue whatsoever
//                             Utils.printLog("PNConnectedCategory", ":::connected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Connected);
                            }
                        case PNReconnectedCategory:
                            // this usually occurs if subscribe temporarily fails but reconnects. This means
                            // there was an error but there is no longer any issue
                             Utils.printLog("PNReconnectedCategory", ":::re connected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Connected);
                            }
                        case PNDisconnectedCategory:
                            // this is the expected category for an unsubscribe. This means there
                            // was no error in unsubscribing from everything
                             Utils.printLog("PNDisconnectedCategory", ":::dis connected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_DisConnected);
                            }
                        case PNTimeoutCategory:
                        case PNUnexpectedDisconnectCategory:
                            // this is usually an issue with the internet connection, this is an error, handle appropriately
                            // retry will be called automatically

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pubnub.reconnect();
                                     Utils.printLog("PNUnexpectedDisconnect", ":::dis unexpected::" + status.toString());
                                }
                            }, 1500);

                             Utils.printLog("PNUnexpectedDisconnect", ":::dis unexpected::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_DisConnected);
                            }
                        case PNAccessDeniedCategory:
                            // this means that PAM does allow this client to subscribe to this
                            // channel and channel group configuration. This is another explicit error
                             Utils.printLog("AccessDenied", ":::denied::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Denied);
                            }
                        default:
                            // More errors can be directly specified by creating explicit cases for other
                            // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                             Utils.printLog("Default", ":::default::" + status.toString());
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).pubNubStatus(Utils.pubNubStatus_Error_Connection);
                            }
                    }

                case PNHeartbeatOperation:
                    // heartbeat operations can in fact have errors, so it is important to check first for an error.
                    // For more information on how to configure heartbeat notifications through the status
                    // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                    if (status.isError()) {
                        // There was an error with the heartbeat operation, handle here
                         Utils.printLog("PNHeartbeatOperation", ":::failed::" + status.toString());
                    } else {
                        // heartbeat operation was successful
                         Utils.printLog("PNHeartbeatOperation", ":::success::" + status.toString());
                    }
                default: {
                    // Encountered unknown status type
                     Utils.printLog("unknown status", ":::unknown::" + status.toString());
                }
            }
        }

        @Override
        public void message(PubNub pubnub, PNMessageResult message) {
            // handle incoming messages
            Utils.printLog("Intialize","MsgOnPubNub");
             Utils.printLog("ON message", ":::got::" + message.getMessage());


            String msg = message.getMessage().toString().replace("\\\"", "\"");
            String finalMsg = "";
            if (isJsonObj(message.getMessage().toString())) {
                finalMsg = message.getMessage().toString();
            } else {
                finalMsg = msg.substring(1, msg.length() - 1);
            }

            String codeKey = CommonUtilities.DRIVER_REQ_CODE_PREFIX_KEY + generalFunc.getJsonValue("MsgCode", finalMsg);

            if(generalFunc.retrieveValue(codeKey).equals("") && !generalFunc.containsKey(CommonUtilities.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + generalFunc.getJsonValue("MsgCode", finalMsg))){
                Utils.sendBroadCast(mContext, CommonUtilities.passenger_message_arrived_intent_action, finalMsg);
//                generalFunc.storedata(codeKey, "true");
            }

            Utils.sendBroadCast(mContext, CommonUtilities.passenger_message_arrived_intent_action_trip_msg, finalMsg);

        }

        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            // handle incoming presence data
             Utils.printLog("ON presence", ":::got::" + presence.toString());
        }
    };

    public boolean isJsonObj(String json) {

        try {
            JSONObject obj_check = new JSONObject(json);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void subscribeToCabRequestChannel() {
        Utils.printLog("Online","Sub");
        pubnub.subscribe()
                .channels(Arrays.asList("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }

    public void unSubscribeToCabRequestChannel() {
        Utils.printLog("Online","UnSub");

        pubnub.unsubscribe()
                .channels(Arrays.asList("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                .execute();
    }


    public void publishMsg(String channel, String message) {
//        .message(Arrays.asList("hello", "there"))
        if (message == null) {
            Utils.printLog("Msg null", "Yes");
            return;
        }
        pubnub.publish()
                .message(message)
                .channel(channel)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                        if (result != null && result.getTimetoken() != null) {
                            Utils.printLog("Publish Res", "::::" + result.getTimetoken());
                        }

                    }
                });
    }
}
