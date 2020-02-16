package com.app85taxi.driver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

public class VerifyInfoActivity extends AppCompatActivity {

    CardView emailView, smsView;
    ProgressBar loading;
    MaterialEditText codeBox;
    MaterialEditText emailBox;

    ImageView backImgView;
    GeneralFunctions generalFunc;
    String required_str = "";
    String error_verification_code = "";

    String userProfileJson = "";
    MTextView titleTxt;

    MButton okBtn, emailOkBtn;
    MButton resendBtn, emailResendBtn;
    MButton editBtn, emailEditBtn;
    Bundle bundle;
    String reqType = "";
    String vEmail = "", vPhone = "";

    String phoneVerificationCode = "";
    String emailVerificationCode = "";
    int resendSecAfter = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_info);

        generalFunc = new GeneralFunctions(getActContext());
        bundle = new Bundle();
        bundle = getIntent().getExtras();
        String msg = bundle.getString("msg");

        if (!getIntent().hasExtra("MOBILE")) {
            userProfileJson = bundle.getString("UserProfileJson");

            vEmail = generalFunc.getJsonValue("vEmail", userProfileJson);
            vPhone = generalFunc.getJsonValue("vPhone", userProfileJson);
        } else {
            vPhone = getIntent().getStringExtra("MOBILE");
        }

        emailView = (CardView) findViewById(R.id.emailView);
        smsView = (CardView) findViewById(R.id.smsView);

        if (msg.equalsIgnoreCase("DO_EMAIL_PHONE_VERIFY")) {
            emailView.setVisibility(View.VISIBLE);
            smsView.setVisibility(View.VISIBLE);
            reqType = "DO_EMAIL_PHONE_VERIFY";
        } else if (msg.equalsIgnoreCase("DO_EMAIL_VERIFY")) {
            emailView.setVisibility(View.VISIBLE);
            smsView.setVisibility(View.GONE);
            reqType = "DO_EMAIL_VERIFY";
        } else if (msg.equalsIgnoreCase("DO_PHONE_VERIFY")) {
            smsView.setVisibility(View.VISIBLE);
            emailView.setVisibility(View.GONE);
            reqType = "DO_PHONE_VERIFY";
        }

        okBtn = ((MaterialRippleLayout) findViewById(R.id.okBtn)).getChildView();
        resendBtn = ((MaterialRippleLayout) findViewById(R.id.resendBtn)).getChildView();
        editBtn = ((MaterialRippleLayout) findViewById(R.id.editBtn)).getChildView();
        codeBox = (MaterialEditText) findViewById(R.id.codeBox);
        emailBox = (MaterialEditText) findViewById(R.id.emailCodeBox);
        emailOkBtn = ((MaterialRippleLayout) findViewById(R.id.emailOkBtn)).getChildView();
        emailResendBtn = ((MaterialRippleLayout) findViewById(R.id.emailResendBtn)).getChildView();
        emailEditBtn = ((MaterialRippleLayout) findViewById(R.id.emailEditBtn)).getChildView();

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        loading = (ProgressBar) findViewById(R.id.loading);

        okBtn.setId(Utils.generateViewId());
        okBtn.setOnClickListener(new setOnClickList());

        resendBtn.setId(Utils.generateViewId());
        resendBtn.setOnClickListener(new setOnClickList());

        editBtn.setId(Utils.generateViewId());
        editBtn.setOnClickListener(new setOnClickList());

        emailOkBtn.setId(Utils.generateViewId());
        emailOkBtn.setOnClickListener(new setOnClickList());

        emailResendBtn.setId(Utils.generateViewId());
        emailResendBtn.setOnClickListener(new setOnClickList());

        emailEditBtn.setId(Utils.generateViewId());
        emailEditBtn.setOnClickListener(new setOnClickList());
        setLabels();
        sendVerificationSMS();

        if (generalFunc.retrieveValue(CommonUtilities.SITE_TYPE_KEY).equalsIgnoreCase("Demo")) {
            findViewById(R.id.helpOTPTxtView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.helpOTPTxtView).setVisibility(View.GONE);
        }
    }

    private void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_TXT"));
        ((MTextView) findViewById(R.id.smsTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_VERIFy_TXT"));
        ((MTextView) findViewById(R.id.smsSubTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SMS_SENT_TO") + ": ");
        ((MTextView) findViewById(R.id.emailTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_VERIFy_TXT"));
        ((MTextView) findViewById(R.id.emailSubTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_SENT_TO") + " ");
        ((MTextView) findViewById(R.id.smsHelpTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SMS_SENT_NOTE"));
        ((MTextView) findViewById(R.id.emailHelpTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_SENT_NOTE"));

        ((MTextView) findViewById(R.id.phoneTxt)).setText(vPhone);
        ((MTextView) findViewById(R.id.emailTxt)).setText(vEmail);

        okBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        resendBtn.setText(generalFunc.retrieveLangLBl("", "LBL_RESEND_SMS"));
        editBtn.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_MOBILE"));

        emailOkBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        emailResendBtn.setText(generalFunc.retrieveLangLBl("", "LBL_RESEND_EMAIL"));
        emailEditBtn.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_EMAIL"));

        error_verification_code = generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_CODE_INVALID");
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
    }

    public void sendVerificationSMS() {
        loading.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "sendVerificationSMS");
        if (!TextUtils.isEmpty(generalFunc.getMemberId())) {
            parameters.put("iMemberId", generalFunc.getMemberId());
        } else {
            parameters.put("MobileNo", vPhone);
        }

        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("REQ_TYPE", reqType);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                loading.setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        switch (reqType) {
                            case "DO_EMAIL_PHONE_VERIFY":
                                if (!generalFunc.getJsonValue(CommonUtilities.message_str, responseString).equals("")) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                            generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                                } else {
                                    if (!generalFunc.getJsonValue(CommonUtilities.message_str + "_sms", responseString).equalsIgnoreCase("LBL_MOBILE_VERIFICATION_FAILED_TXT")) {
                                        phoneVerificationCode = generalFunc.getJsonValue(CommonUtilities.message_str + "_sms", responseString);
                                    } else {
                                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                                generalFunc.getJsonValue(CommonUtilities.message_str + "_sms", responseString)));
                                    }
                                    if (!generalFunc.getJsonValue(CommonUtilities.message_str + "_email", responseString).equalsIgnoreCase("LBL_EMAIL_VERIFICATION_FAILED_TXT")) {
                                        emailVerificationCode = generalFunc.getJsonValue(CommonUtilities.message_str + "_email", responseString);
                                    } else {
                                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                                generalFunc.getJsonValue(CommonUtilities.message_str + "_email", responseString)));
                                    }
                                }
                                break;
                            case "DO_EMAIL_VERIFY":
                                emailVerificationCode = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                                break;
                            case "DO_PHONE_VERIFY":
                                phoneVerificationCode = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                                break;
                            case "PHONE_VERIFIED":
                                verifySuccessMessage(generalFunc.retrieveLangLBl("",
                                        generalFunc.getJsonValue(CommonUtilities.message_str, responseString)), true, false);

                                break;
                            case "EMAIL_VERIFIED":
                                verifySuccessMessage(generalFunc.retrieveLangLBl("",
                                        generalFunc.getJsonValue(CommonUtilities.message_str, responseString)), false, true);
                                break;
                        }

                        String userdetails= generalFunc.getJsonValue("userDetails",responseString);
                        if(!userdetails.equals("") && userdetails!=null) {
                            String messageData = generalFunc.getJsonValue(CommonUtilities.message_str, userdetails);
                            generalFunc.storedata(CommonUtilities.USER_PROFILE_JSON, messageData);
                        }
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void verifySuccessMessage(String message, final boolean sms, final boolean email) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                if (TextUtils.isEmpty(generalFunc.getMemberId())) {
                    if (TextUtils.isEmpty(generalFunc.getMemberId())) {
                        new StartActProcess(getActContext()).setOkResult();
                        VerifyInfoActivity.super.onBackPressed();
                    }
                } else {
                    if (sms == true) {
                        smsView.setVisibility(View.GONE);

                        if (emailView.getVisibility() == View.GONE) {
                            VerifyInfoActivity.super.onBackPressed();
                        }
                    } else if (email == true) {
                        emailView.setVisibility(View.GONE);

                        if (smsView.getVisibility() == View.GONE) {
                            VerifyInfoActivity.super.onBackPressed();
                        }
                    }
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void resendProcess(final MButton btn) {

        btn.setTextColor(Color.parseColor("#BABABA"));
        btn.setClickable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.setTextColor(getResources().getColor(R.color.appThemeColor_TXT_1));
                btn.setClickable(true);
            }
        }, resendSecAfter);
    }

    public Context getActContext() {
        return VerifyInfoActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
           Utils.hideKeyboard(VerifyInfoActivity.this);
            if (i == R.id.backImgView) {
                VerifyInfoActivity.super.onBackPressed();
            } else if (i == okBtn.getId()) {
//                boolean isCodeEntered = Utils.checkText(codeBox) ? (phoneVerificationCode.equalsIgnoreCase(Utils.getText(codeBox)) ? true : Utils.setErrorFields(codeBox, error_verification_code)) : Utils.setErrorFields(codeBox, required_str);
                boolean isCodeEntered = Utils.checkText(codeBox) ?
                        ((phoneVerificationCode.equalsIgnoreCase(Utils.getText(codeBox)) ||
                                (generalFunc.retrieveValue(CommonUtilities.SITE_TYPE_KEY).equalsIgnoreCase("Demo") && Utils.getText(codeBox).equalsIgnoreCase("12345"))) ? true
                                : Utils.setErrorFields(codeBox, error_verification_code)) : Utils.setErrorFields(codeBox, required_str);
                if (isCodeEntered) {
                    reqType = "PHONE_VERIFIED";
                    sendVerificationSMS();
                }
            } else if (i == resendBtn.getId()) {
                reqType = "DO_PHONE_VERIFY";
                resendProcess(resendBtn);
                sendVerificationSMS();
            } else if (i == editBtn.getId()) {
//                Bundle bn = new Bundle();
//                bn.putString("MSG_TYPE", "EDIT_PROFILE");
//                (new StartActProcess(getActContext())).setOkResult(bn);
//                VerifyInfoActivity.super.onBackPressed();
                Bundle bn = new Bundle();
                bn.putString("UserProfileJson", userProfileJson);
                bn.putBoolean("isEdit", true);
                bn.putBoolean("isMobile", true);
                new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
            } else if (i == emailOkBtn.getId()) {
//                boolean isEmailCodeEntered = Utils.checkText(emailBox) ? (emailVerificationCode.equalsIgnoreCase(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_verification_code)) : Utils.setErrorFields(emailBox, required_str);
                boolean isEmailCodeEntered = Utils.checkText(emailBox) ?
                        ((emailVerificationCode.equalsIgnoreCase(Utils.getText(emailBox)) ||
                                (generalFunc.retrieveValue(CommonUtilities.SITE_TYPE_KEY).equalsIgnoreCase("Demo") && Utils.getText(emailBox).equalsIgnoreCase("12345"))) ? true
                                : Utils.setErrorFields(emailBox, error_verification_code)) : Utils.setErrorFields(emailBox, required_str);
                if (isEmailCodeEntered) {
                    reqType = "EMAIL_VERIFIED";
                    sendVerificationSMS();
                }
            } else if (i == emailResendBtn.getId()) {
                reqType = "DO_EMAIL_VERIFY";
                resendProcess(emailResendBtn);
                sendVerificationSMS();
            } else if (i == emailEditBtn.getId()) {
//                Bundle bn = new Bundle();
//                bn.putString("MSG_TYPE", "EDIT_PROFILE");
//                (new StartActProcess(getActContext())).setOkResult(bn);
//                VerifyInfoActivity.super.onBackPressed();
                Bundle bn = new Bundle();
                bn.putString("UserProfileJson", userProfileJson);
                bn.putBoolean("isEdit", true);
                bn.putBoolean("isEmail", true);
                new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE) {
            userProfileJson = generalFunc.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
            vEmail = generalFunc.getJsonValue("vEmail", userProfileJson);
            vPhone = generalFunc.getJsonValue("vPhone", userProfileJson);

            setLabels();

        }
    }


}
