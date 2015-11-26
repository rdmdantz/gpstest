package meshsol.locationsharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Wasiq Billah on 11/9/2015.
 */
public class SharePreferences {
    static final String PREF_INIT_LAT= "initLat";
    static final String PREF_INIT_LON="initLon";
    static final String PREF_PREV_LAT= "prevLat";
    static final String PREF_PREV_LON="prevLon";
    static final String PREF_TIME= "prevTime";

    static final String PREF_SESSION= "sessoin";
    static final String PREF_UPDATED_NORMAL= "updatedNormal";
    static final String PREF_STOPED_TO_SEND_MESSAGES_MESSAGE_SENT= "stopedSendingMessages";
    static final String PREF_REQUESTED_TO_CHANGE_UPDATE_FREQUENCY="requestedChangeUpdateFrequency";
    static final String PREF_SERVER_UPDATED="serverUpdated";

    static final String PREF_USER_SERVER_ID="userServerId";
    static final String PREF_USER_GCM_ID="userGcmId";
    static final String PREF_USER_NUMBER="userNumber";

    static final String PREF_GUEST_SERVER_ID="guestGcmId";
    static final String PREF_HOST_SERVER_ID="hostGcmId";

    static final String PREF_NORMAL_ALARM_ID="normalAlarm";
    static final String PREF_HIGHFREQUENCY_ALARM_ID="highFreqAlarm";

    static final String PREF_IS_SAFEWAY="isSafeway";

    static final String PREF_MODE="myMode";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setPrefNormalAlarmIdId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_NORMAL_ALARM_ID, id);
        editor.commit();
    }

    public static void setPrefMode(Context ctx, String mode)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_MODE, mode);
        editor.commit();
    }


    public static void setPrefIsSafeway(Context ctx, boolean flag)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_IS_SAFEWAY, flag);
        editor.commit();
    }


    public static void setPrefHighfrequencyAlarmId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_HIGHFREQUENCY_ALARM_ID, id);
        editor.commit();
    }

    public static void setPrefGuestServerId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_GUEST_SERVER_ID, id);
        editor.commit();
    }

    public static void setPrefHostServerId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_HOST_SERVER_ID, id);
        editor.commit();
    }



    public static void setPrefSession(Context ctx, String session)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_SESSION, session);
        editor.commit();
    }

    public static void setPrefUserServerId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_SERVER_ID, id);
        editor.commit();
    }

    public static void setPrefServerUpdated(Context ctx, String val)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_SERVER_UPDATED, val);
        editor.commit();
    }


    public static void setPrefUserNumber(Context ctx, String number)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NUMBER, number);
        editor.commit();
    }


    public static void setPrefUserGcmId(Context ctx, String id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_GCM_ID, id);
        editor.commit();
    }


    public static void setPrefStopedSendingMessage(Context ctx, String val)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_STOPED_TO_SEND_MESSAGES_MESSAGE_SENT, val);
        editor.commit();
    }

    public static void setPrefRequestedToChangeUpdateFrequency(Context ctx, String val)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_REQUESTED_TO_CHANGE_UPDATE_FREQUENCY, val);
        editor.commit();
    }

    public static void setInitLat(Context ctx, String initLat)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_INIT_LAT, initLat);
        editor.commit();
    }

    public static void setInitLon(Context ctx, String initLon)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_INIT_LON, initLon);
        editor.commit();
    }
    public static void setPrevLat(Context ctx, String prevLat)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_PREV_LAT, prevLat);
        editor.commit();
    }

    public static void setPrevLon(Context ctx, String prevLon)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_PREV_LON, prevLon);
        editor.commit();
    }
    public static void setPrevTime(Context ctx, String time)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_TIME,time);
        editor.commit();
    }
    public static void setPrevEta(Context ctx,String tag, String val)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(tag,val);
        editor.commit();
    }

    public static void setPrevTimeEta(Context ctx,String tag, String time)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(tag,time);
        editor.commit();
    }
    public static void setPrefUpdatedNormal(Context ctx,String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_UPDATED_NORMAL,value);
        editor.commit();
    }

    public static String getPrefSession(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SESSION, "");
    }


    public static String getPrefInitLat(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_INIT_LAT, "");
    }

    public static String getPrefInitLon(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_INIT_LON, "");
    }

    public static String getPrefPrevLat(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_PREV_LAT, "");
    }

    public static String getPrefPrevLon(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_PREV_LON, "");
    }

    public static String getPrefPrevTime(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_TIME, "");
    }

    public static String getPrefPrevEta(Context ctx,String tag) {
        return getSharedPreferences(ctx).getString(tag, "");
    }

    public static String getPrefPrevTimeEta(Context ctx,String tag) {
        return getSharedPreferences(ctx).getString(tag, "");
    }

    public static String getPrefUpdatedNormal(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_UPDATED_NORMAL, "");
    }
    public static String getPrefStopedToSendMessagesMessageSent(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_STOPED_TO_SEND_MESSAGES_MESSAGE_SENT, "");
    }

    public static String getPrefRequestedToChangeUpdateFrequency(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_REQUESTED_TO_CHANGE_UPDATE_FREQUENCY, "");
    }
    public static String getPrefUserServerId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_SERVER_ID, "");
    }

    public static String getPrefUserGcmId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_GCM_ID, "");
    }

    public static String getPrefUserNumber(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_NUMBER, "");
    }

    public static String getPrefServerUpdated(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_SERVER_UPDATED, "");
    }


    public static String getPrefGuestServerId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_GUEST_SERVER_ID, "");
    }

    public static String getPrefHostServerId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_HOST_SERVER_ID, "");
    }

    public static String getPrefNormalAlarmId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_NORMAL_ALARM_ID, "");
    }

    public static String getPrefHighfrequencyAlarmId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_HIGHFREQUENCY_ALARM_ID, "");
    }

    public static String getPrefMode(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_MODE, "");
    }

    public static boolean getPrefIsSafeway(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_IS_SAFEWAY, false);
    }
}
