package com.hst.beautifulwall.utility;


import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
    private SharedPreferences sharedPreferences;
    private String PREFS_ACCOUNT = "CHANGE_IP_VPS";


    private String KEY_PREMIUM = "KEY_PREMIUM"; // premium
    private String KEY_TOTAL_COIN = "KEY_TOTAL_COIN"; // coin
    private String IS_OPEN_FIRST = "IS_OPEN_FIRST"; // coin

    public static Preference instance;

    public static Preference buildInstance(Context context) {
        if (instance == null) {
            instance = new Preference(context);
        }
        return instance;
    }

    private Preference(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE);
    }

    public boolean isOpenFirst() {
        boolean isFirst = sharedPreferences.getBoolean(IS_OPEN_FIRST, false);
        if (!isFirst) {
            setValueCoin(30);
        }
        sharedPreferences.edit().putBoolean(IS_OPEN_FIRST, true).apply();
        return isFirst;
    }

    public void setPremium(int value) {
        sharedPreferences.edit().putInt(KEY_PREMIUM, value).apply();
    }

    public int getPremium() {
        return sharedPreferences.getInt(KEY_PREMIUM, 0);
    }

    public void setValueCoin(int value) {
        sharedPreferences.edit().putInt(KEY_TOTAL_COIN, value).apply();
    }

    public int getValueCoin() {
        return sharedPreferences.getInt(KEY_TOTAL_COIN, 0);
    }
}

