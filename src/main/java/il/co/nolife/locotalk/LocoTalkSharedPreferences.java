package il.co.nolife.locotalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;

import il.co.nolife.locotalk.Activities.LocoTalkMain;

/**
 * Created by Victor Belski on 9/6/2015.
 * Designed to handle all operations that require the shared preferences in AreaChat
 */
public class LocoTalkSharedPreferences {

    public final static String REG_ID = "REG_ID";
    public final static String APP_VERSION = "APP_VERSION";
    public final static String LOGIN = "LOGIN";
    public final static String PASSWORD = "PASSWORD";
    public final static String LOGIN_INFO = "LOGIN_INFO";
    public final static String USER_EMAIL = "USER_EMAIL";
    public final static String USER_NAME = "USER_NAME";
    public final static String USER_PASS = "USER_PASS";
    public final static String USER_REG_ID = "USER_REG_ID";

    SharedPreferences prefs;
    Context context;

    public LocoTalkSharedPreferences(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(LocoTalkMain.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String GetRegistrationId() {

        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(getClass().toString(), "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = GetAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(getClass().toString(), "App version changed.");
            return "";
        }
        return registrationId;

    }

    public void StoreRegistrationId(String id) {

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, id);
        editor.putInt(APP_VERSION, GetAppVersion(context));
        editor.apply();

    }

    public User GetUser() {

        User retVal = new User();

        retVal.setMail(prefs.getString(USER_EMAIL, ""));
        retVal.setFullName(prefs.getString(USER_NAME, ""));
        retVal.setPassword(prefs.getString(USER_PASS, ""));
        retVal.setRegistrationId(prefs.getString(USER_REG_ID, ""));

        return retVal;

    }

    public void StoreUser(User user) {

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_EMAIL, user.getMail());
        editor.putString(USER_NAME, user.getFullName());
        editor.putString(USER_PASS, user.getPassword());
        editor.putString(USER_REG_ID, user.getRegistrationId());
        editor.apply();

    }

    public void StorePassword(String pass) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_PASS, pass);
        editor.apply();
    }

    public String GetPassword() {
        return prefs.getString(USER_PASS, "");
    }

    int GetAppVersion(Context context) {

        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }

    }

}
