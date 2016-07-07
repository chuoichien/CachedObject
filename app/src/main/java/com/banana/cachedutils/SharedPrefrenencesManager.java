/**
 * SharedPrefrenencesManager.java
 * <p/>
 * Purpose              :
 * <p/>
 * Optional info        :
 *
 * @author : Huy Duong Tu
 * @date : Oct 12, 2012
 * @lastChangedRevision :
 * @lastChangedDate :
 */
package com.banana.cachedutils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Thanhlnh
 */
public class SharedPrefrenencesManager {
    public static final String SP_NAME = "CachedUtils";
    public static final String CACHED_HOME_DATA = "cached_data_";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit();
    }


    public static void setStringValue(Context context, String SPKey, String value) {
        getSharedPreferencesEditor(context).putString(SPKey, value).apply();
    }

    public static String getStringValue(Context context, String SPkey, String defaultValue) {
        return getSharedPreferences(context).getString(SPkey, defaultValue);
    }
}
