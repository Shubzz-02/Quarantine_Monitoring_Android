package com.shubzz.hqm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class SessionHandler {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMPTY = "";
    private static final String KEY_SEC_KEY = "key";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_VILL = "vill_name";
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPreferences;
    private String url = "http://192.168.43.98/backend/src/";
    //private String url = "https://shubzz.me/";

    public SessionHandler(Context mContext) {
        this.mContext = mContext;
        mPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void loginUser(String username, String fullName, String key, String block) {
        this.mEditor = mPreferences.edit();
        mEditor.putString(KEY_USERNAME, username);
        mEditor.putString(KEY_FULL_NAME, fullName);
        mEditor.putString(KEY_SEC_KEY, key);
        mEditor.putString(KEY_BLOCK, block);
        mEditor.putString(KEY_VILL, "");
        //Log.d("Secret_key", KEY_USERNAME + " " + KEY_FULL_NAME + " " + KEY_SEC_KEY);
        Date date = new Date();
        long millis = date.getTime() + (7 * 24 * 60 * 60 * 1000);
        mEditor.putLong(KEY_EXPIRES, millis);
        mEditor.apply();
    }

    public boolean isLoggedIn() {
        Date currentDate = new Date();
        long millis = mPreferences.getLong(KEY_EXPIRES, 0);
        if (millis == 0) {
            return false;
        }
        Date expiryDate = new Date(millis);
        return currentDate.before(expiryDate);
    }

    public String getVill_name() {
        String vill = null;
        vill = mPreferences.getString(KEY_VILL, KEY_EMPTY);
        return vill;
    }

    public void setVill_name(String vill_name) {
        this.mEditor = mPreferences.edit();
        mEditor.putString(KEY_VILL, vill_name);
        mEditor.apply();
    }

    public String getName() {
        String name = null;
        name = mPreferences.getString(KEY_FULL_NAME, KEY_EMPTY);
        return name;
    }

    public String getKeyUsername() {
        String name = null;
        name = mPreferences.getString(KEY_USERNAME, KEY_EMPTY);
        return name;
    }

    public String getBlock() {
        String block = null;
        block = mPreferences.getString(KEY_BLOCK, KEY_EMPTY);
        return block;
    }

    public String getSecKey() {
        String KEY = null;
        KEY = mPreferences.getString(KEY_SEC_KEY, KEY_EMPTY);
        return KEY;
    }

    public boolean spExists() {
        return getName() != null;
    }

    public String getUrl() {
        return url;
    }

    public void logoutUser() {
        this.mEditor = mPreferences.edit();
        try {
            String file = getBlock() + ".json";
            mContext.deleteFile(file);
            file = getVill_name() + ".json";
            mContext.deleteFile(file);
        } catch (Exception e) {
            //Log.d("err","file error");
        }
        mEditor.clear();
        mEditor.apply();
    }
}
