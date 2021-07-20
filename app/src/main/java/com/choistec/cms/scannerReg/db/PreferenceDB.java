package com.choistec.cms.scannerReg.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 설정에 저장된 DB 클래스
 */
public class PreferenceDB {
    public static Context mSuperContext;
    public PreferenceDB(Context context) {
        mSuperContext = context;
    }

    /**
     * 정해진 키 값으로 저장된 DB를 String 값으로 불러온다.
     * @param key DB의 키값
     * @return
     */
    public String getStringFromPref(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        return pref.getString(key, null);
    }
    /**
     * 정해진 키 값으로 저장된 DB를 String 값으로 불러온다.
     * @param key DB의 키값
     * @return
     */
    public Integer getIntFromPref(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        String stringValue = pref.getString(key, "0");
        return Integer.parseInt(stringValue);
    }

    /**
     * 정해진 키 값으로 저장된 DB를 Boolean 값으로 불러온다.
     * 기본 값은 false 이다.
     * @param key DB의 키값
     * @return
     */
    public boolean getBooleanFromPref(String key) {
        SharedPreferences pref;
        pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        return pref.getBoolean(key, false);
    }


}
