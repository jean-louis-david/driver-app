package com.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.app85taxi.driver.BuildConfig;
import com.app85taxi.driver.R;
import com.general.files.GeneralFunctions;
import com.general.files.WakeLocker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.view.editBox.MaterialEditText;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

//    public static final String pubNub_sub_key = "sub-c-18a5a7f2-83a0-11e6-974e-0619f8945a4f";
//    public static final String pubNub_pub_key = "pub-c-e00ce66a-d8e9-4110-a9dc-36ba7e0856fe";
//    public static final String pubNub_sec_key = "sec-c-NGI2ZWJkMjUtMjI2OC00MmFmLTk1YTEtMGI3YTQ5NmMwMjU5";

    public static final String pubNubStatus_Connected = "Connected";
    public static final String pubNubStatus_DisConnected = "DisConnected";
    public static final String pubNubStatus_Error_Connection = "ErrorInConnection";
    public static final String pubNubStatus_Denied = "DeniedConnection";

    public static final String pubNub_Update_Loc_Channel_Prefix = "ONLINE_DRIVER_LOC_";
    public static final String ENABLE_PUBNUB_KEY = "ENABLE_PUBNUB";

    public static final String Past = "getRideHistory";
    public static final String Upcoming = "checkBookings";

    public static final String userType = "Driver";

    public static final String Wallet_all = "All";
    public static final String Wallet_credit = "CREDIT";
    public static final String Wallet_debit = "DEBIT";

    public static String SESSION_ID_KEY = "APP_SESSION_ID";
    public static String DEVICE_SESSION_ID_KEY = "DEVICE_SESSION_ID";

    public static final int NOTIFICATION_ID = 11;
    public static final int NOTIFICATION_BACKGROUND_ID = 12;

    public static final String deviceType = "Android";
    public static final int OVERLAY_PERMISSION_REQ_CODE = 2542;
    public static final float defaultZomLevel = (float) 16.5;
    public static final int LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS = 2;
//    public static final int LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS = 0;

    public static final int minPasswordLength = 6;
    public static final int SELECT_COUNTRY_REQ_CODE = 124;
    public static final int SEARCH_PICKUP_LOC_REQ_CODE = 125;
    public static final int SEARCH_DEST_LOC_REQ_CODE = 126;
    public static final int MY_PROFILE_REQ_CODE = 127;
    public static final int VERIFY_MOBILE_REQ_CODE = 128;
    public static final int VERIFY_INFO_REQ_CODE = 129;
    public static final int CARD_PAYMENT_REQ_CODE = 130;
    public static final int ADD_VEHICLE_REQ_CODE = 131;
    public static final int UPLOAD_DOC_REQ_CODE = 132;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_CODE_GPS_ON = 2425;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public static final int MENU_PROFILE = 0;
    public static final int MENU_RIDE_HISTORY = 1;
    public static final int MENU_BOOKINGS = 2;
    public static final int MENU_FEEDBACK = 3;
    public static final int MENU_ABOUT_US = 4;
    public static final int MENU_CONTACT_US = 5;
    public static final int MENU_HELP = 6;
    public static final int MENU_SIGN_OUT = 7;
    public static final int MENU_INVITE_FRIEND = 8;
    public static final int MENU_WALLET = 9;
    public static final int MENU_PAYMENT = 10;
    public static final int MENU_MY_HEATVIEW = 11;
    public static final int MENU_POLICY = 12;
    public static final int MENU_SUPPORT = 13;
    public static final int MENU_YOUR_TRIPS = 14;
    public static final int MENU_YOUR_DOCUMENTS = 15;
    public static final int MENU_MANAGE_VEHICLES = 16;
    public static final int MENU_TRIP_STATISTICS = 17;
    public static final int MENU_EMERGENCY_CONTACT = 18;
    public static final int MENU_ACCOUNT_VERIFY = 19;
    public static final int MENU_WAY_BILL = 20;
    public static LatLng tempLatlong = null;


    public static int dpToPx(float dp, Context context) {
        return dpToPx(dp, context.getResources());
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static final int ImageUpload_DESIREDWIDTH = 1024;
    public static final int ImageUpload_DESIREDHEIGHT = 1024;
    public static final int ImageUpload_MINIMUM_WIDTH = 256;
    public static final int ImageUpload_MINIMUM_HEIGHT = 256;

    public static final String TempImageFolderPath = "TempImages";
    public static final String TempProfileImageName = "temp_pic_img.png";

    public static final String CabGeneralType_Ride = "Ride";
    public static final String CabGeneralType_Deliver = "Deliver";
    public static final String CabGeneralType_UberX = "UberX";
    public static final String CabGeneralTypeRide_Delivery_UberX = "Ride-Delivery-UberX";

    public static final String CabFaretypeRegular = "Regular";
    public static final String CabFaretypeFixed = "Fixed";
    public static final String CabFaretypeHourly = "Hourly";
    public static String storedImageFolderName = "/" + CommonUtilities.app_name + "/ProfileImage";

    public static String dateFormateInHeaderBar = "EEE, MMM d, yyyy";
    public static String dateFormateInList = "dd-MMM-yyyy";
    public static String DefaultDatefromate = "yyyy/MM/dd";
    public static String WalletApiFormate = "dd-MMM-yyyy";
    public static String OriginalDateFormate = "yyyy-MM-dd HH:mm:ss";

    //    static let dateFormateInList = "dd-MMM-yyyy"
    public static String dateFormateTimeOnly = "h:mm a";

    public static void printLog(String title, String content) {
        Log.d(title, content);
    }

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics));
    }

    public static int getSDKInt() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static int getExifRotation(String path) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        return orientation;

    }

    public static void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker, GoogleMap gMap, boolean isRotate, double angle) {
        if (toPosition == null || gMap == null) {
            return;
        }

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = gMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

//				setLatLonBound();

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });

        if (isRotate && angle != -1.000) {
            rotateMarker(marker, (float) angle);
        }
    }

    public static double CalculationByLocation(double lat1, double lon1, double lat2, double lon2, String returnType) {
        int Radius = 6371;// radius of earth in Km
        double lat1_s = lat1;
        double lat2_d = lat2;
        double lon1_s = lon1;
        double lon2_d = lon2;
        double dLat = Math.toRadians(lat2_d - lat1_s);
        double dLon = Math.toRadians(lon2_d - lon1_s);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1_s))
                * Math.cos(Math.toRadians(lat2_d)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        // Log.i("Radius Value", "" + valueResult + " KM " + kmInDec
        // + " Meter " + meterInDec);
        if (returnType.equals("METER")) {
            return meter;
        }
        return meter;

        // return Radius * c;
    }

    public static double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    public static void rotateMarker(final Marker marker, final float toRotation) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    public static int generateViewId() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    public static void runGC() {
        System.gc();
    }

    public static void generateListSelector(ListView listView, int color) {
        StateListDrawable res = new StateListDrawable();
        res.setExitFadeDuration(400);
        res.setAlpha(90);
//        res.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
        res.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(color));

        listView.setSelector(res);

    }

    public static void removeInput(EditText editBox) {
        editBox.setInputType(InputType.TYPE_NULL);
        editBox.setFocusableInTouchMode(false);
        editBox.setFocusable(false);

        editBox.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public static boolean checkText(MaterialEditText editBox) {
        if (getText(editBox).trim().equals("")) {
            return false;
        }
        return true;
    }

    public static boolean checkText(String txt) {
        if (txt.trim().equals("") || TextUtils.isEmpty(txt)) {
            return false;
        }
        return true;
    }

    public static boolean checkText(EditText editBox) {
        if (getText(editBox).trim().equals("")) {
            return false;
        }
        return true;
    }

    public static String getText(MaterialEditText editBox) {
        return editBox.getText().toString();
    }

    public static String getText(EditText editBox) {
        return editBox.getText().toString();
    }

    public static String getText(TextView txtView) {
        return txtView.getText().toString();
    }

    public static boolean setErrorFields(MaterialEditText editBox, String error) {
        editBox.setError(error);
        return false;
    }

    public static void hideKeyboard(Context context) {
//        Utils.printLog("Key board","hide");
//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void hideKeyboard(Activity act) {
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = act.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void setAppLocal(Context mContext) {
        GeneralFunctions generalFunc = new GeneralFunctions(mContext);

        String googleMapLangCode = generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);
        String languageToLoad = googleMapLangCode.trim().equals("") ? "en" : googleMapLangCode;
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        mContext.getResources().updateConfiguration(config, mContext.getResources().getDisplayMetrics());
    }

    public static String convertNumToAppLocal(String data, Context mContext) {
        String result = "";
        GeneralFunctions generalFunc = new GeneralFunctions(mContext);
        String googleMapLangCode = generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        if (data != null && !data.equals("")) {
            for (int i = 0; i < data.length(); i++) {

                char c = data.charAt(i);

                if (Character.isDigit(c)) {


                } else {
                    result = result + c;

                }

            }
        }
        return result;

    }

    public static void sendBroadCast(Context mContext, String action, String message) {
        Intent intent_broad = new Intent(action);
        intent_broad.putExtra(CommonUtilities.passenger_message_arrived_intent_key, message);
        mContext.sendBroadcast(intent_broad);
    }

    public static void sendBroadCast(Context mContext, String action) {
        Intent intent_broad = new Intent(action);
        mContext.sendBroadcast(intent_broad);
    }

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static void generateNotification(Context context, String message) {
        WakeLocker.acquire(context);

        Intent intent = null;
        if (getPreviousIntent(context) != null) {
            intent = getPreviousIntent(context);
        } else {
            intent = context
                    .getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.mipmap.ic_launcher;
        String title = context.getString(R.string.app_name);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(icon)
                .setContentTitle(title).setContentText(message).setContentIntent(contentIntent)
//                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification))
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true);

        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationmanager.notify(Utils.NOTIFICATION_ID, mBuilder.build());

        WakeLocker.release();
    }

    public static Intent getPreviousIntent(Context context) {
        Intent newIntent = null;
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTaskInfos = am.getRecentTasks(1024, 0);
        String myPkgNm = context.getPackageName();

        if (!recentTaskInfos.isEmpty()) {
            ActivityManager.RecentTaskInfo recentTaskInfo;
            for (int i = 0; i < recentTaskInfos.size(); i++) {
                recentTaskInfo = recentTaskInfos.get(i);
                if (recentTaskInfo.baseIntent.getComponent().getPackageName().equals(myPkgNm)) {
                    newIntent = recentTaskInfo.baseIntent;
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        }
        return newIntent;
    }

    public static String maskCardNumber(String cardNumber) {

        int i = 0;
        StringBuffer temp = new StringBuffer();
        while (i < (cardNumber.length())) {
            if (i > cardNumber.length() - 5) {
                temp.append(cardNumber.charAt(i));
            } else {
                temp.append("X");
            }
            i++;
        }
        System.out.println(temp);

        return temp.toString();
    }

    public static int pxToDp(Context context, float pxValue) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(pxValue / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void dismissBackGroundNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Utils.NOTIFICATION_BACKGROUND_ID);
        manager.cancelAll();
    }

    /*public static boolean isApplicationBroughtToBackground(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (!topActivity.getPackageName().equals(context.getPackageName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }*/

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static String[] generateImageParams(String key, String content) {
        String[] tempArr = new String[2];
        tempArr[0] = key;
        tempArr[1] = content;

        return tempArr;
    }


    public static String convertDateToFormat(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        return dateFormat.format(date);
    }

    public static boolean isValidTimeSelect(Date selectedDate, long timeOffset) {
        /*Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR,timeOffset);

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);*/
        if (selectedDate.getTime() >= (System.currentTimeMillis() + timeOffset)) {
            return true;
        }
        return false;
    }

    public static void setMenuTextColor(MenuItem item, int color) {
        SpannableString s = new SpannableString(item.getTitle());
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        item.setTitle(s);
    }

    /*public static Bitmap blurRenderScript(Bitmap smallBitmap, int radius, Context mContext) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(mContext);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    public static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }*/

}
