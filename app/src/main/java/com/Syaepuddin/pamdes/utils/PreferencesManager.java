package com.Syaepuddin.pamdes.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Pengelola SharedPreferences untuk menyimpan pengaturan aplikasi PAMDES Wanasaba Daya.
 */
public class PreferencesManager {

    private static final String PREF_NAME = "pamdes_prefs";
    private static final String KEY_TARIF_RT = "key_tarif_rt";
    private static final String KEY_TARIF_PENGUSAHA = "key_tarif_pengusaha";
    private static final String KEY_BEBAN = "key_biaya_beban";
    private static final String KEY_PMA = "key_biaya_pma";
    private static final String KEY_NAMA_DESA = "key_nama_desa";
    private static final String KEY_JUDUL_HEADER = "key_judul_header";
    private static final String KEY_ALAMAT_HEADER = "key_alamat_header";

    public static final int DEFAULT_TARIF_RT = 500;
    public static final int DEFAULT_TARIF_PENGUSAHA = 1000;
    public static final int DEFAULT_BIAYA_BEBAN = 2000;
    public static final int DEFAULT_BIAYA_PMA = 500;
    public static final String DEFAULT_NAMA_DESA = "Wanasaba Daya Kec. Wanasaba - Lotim - NTB";
    public static final String DEFAULT_JUDUL_HEADER = "\"UTUH BERSATU\"";
    public static final String DEFAULT_ALAMAT_HEADER = "Jln. Wanasaba Karang Baru KM. 3 (085936576951)";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getTarifRumahTangga(Context context) {
        return getPrefs(context).getInt(KEY_TARIF_RT, DEFAULT_TARIF_RT);
    }

    public static void setTarifRumahTangga(Context context, int tarif) {
        getPrefs(context).edit().putInt(KEY_TARIF_RT, tarif).apply();
    }

    public static int getTarifPengusaha(Context context) {
        return getPrefs(context).getInt(KEY_TARIF_PENGUSAHA, DEFAULT_TARIF_PENGUSAHA);
    }

    public static void setTarifPengusaha(Context context, int tarif) {
        getPrefs(context).edit().putInt(KEY_TARIF_PENGUSAHA, tarif).apply();
    }

    public static int getBiayaBeban(Context context) {
        return getPrefs(context).getInt(KEY_BEBAN, DEFAULT_BIAYA_BEBAN);
    }

    public static void setBiayaBeban(Context context, int beban) {
        getPrefs(context).edit().putInt(KEY_BEBAN, beban).apply();
    }

    public static int getBiayaPma(Context context) {
        return getPrefs(context).getInt(KEY_PMA, DEFAULT_BIAYA_PMA);
    }

    public static void setBiayaPma(Context context, int pma) {
        getPrefs(context).edit().putInt(KEY_PMA, pma).apply();
    }

    public static String getNamaDesa(Context context) {
        return getPrefs(context).getString(KEY_NAMA_DESA, DEFAULT_NAMA_DESA);
    }

    public static void setNamaDesa(Context context, String namaDesa) {
        getPrefs(context).edit().putString(KEY_NAMA_DESA, namaDesa).apply();
    }

    public static String getJudulHeader(Context context) {
        return getPrefs(context).getString(KEY_JUDUL_HEADER, DEFAULT_JUDUL_HEADER);
    }

    public static String getAlamatHeader(Context context) {
        return getPrefs(context).getString(KEY_ALAMAT_HEADER, DEFAULT_ALAMAT_HEADER);
    }
}
