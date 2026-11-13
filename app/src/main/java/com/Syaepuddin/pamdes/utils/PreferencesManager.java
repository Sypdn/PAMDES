package com.Syaepuddin.pamdes.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Pengelola SharedPreferences untuk menyimpan pengaturan aplikasi.
 */
public class PreferencesManager {

    private static final String PREF_NAME = "pamdes_prefs";
    private static final String KEY_TARIF = "key_tarif_per_kubik";
    private static final String KEY_NAMA_DESA = "key_nama_desa";

    public static final int DEFAULT_TARIF = 500;
    public static final String DEFAULT_NAMA_DESA = "Desa Sukamaju";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getTarifPerKubik(Context context) {
        return getPrefs(context).getInt(KEY_TARIF, DEFAULT_TARIF);
    }

    public static void setTarifPerKubik(Context context, int tarif) {
        getPrefs(context).edit().putInt(KEY_TARIF, tarif).apply();
    }

    public static String getNamaDesa(Context context) {
        return getPrefs(context).getString(KEY_NAMA_DESA, DEFAULT_NAMA_DESA);
    }

    public static void setNamaDesa(Context context, String namaDesa) {
        getPrefs(context).edit().putString(KEY_NAMA_DESA, namaDesa).apply();
    }
}
