package com.Syaepuddin.pamdes.utils;

import java.text.NumberFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility untuk format mata uang Rupiah, meteran air, dan tanggal.
 */
public class FormatUtils {

    private static final String[] NAMA_BULAN = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    public static String formatRupiah(long amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace(",00", "");
    }

    public static String formatMeter(long meter) {
        if (meter < 0) meter = 0;
        return String.format(Locale.getDefault(), "%07d", meter);
    }

    public static String getNamaBulan(int bulan) {
        if (bulan >= 1 && bulan <= 12) {
            return NAMA_BULAN[bulan - 1];
        }
        return "";
    }

    public static String[] getDaftarBulan() {
        return NAMA_BULAN;
    }

    public static String formatTanggal(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("in", "ID"));
        return sdf.format(new Date(timestamp));
    }
}
