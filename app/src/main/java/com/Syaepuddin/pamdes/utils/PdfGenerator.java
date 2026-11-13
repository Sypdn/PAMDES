package com.Syaepuddin.pamdes.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.model.Tagihan;
import com.Syaepuddin.pamdes.data.model.TagihanDetail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Utility untuk membuat, mencetak (PrintManager), dan membagikan dokumen PDF Struk & Rekap.
 */
public class PdfGenerator {

    private static final String FILE_PROVIDER_AUTHORITY = "com.Syaepuddin.pamdes.fileprovider";

    public static class PdfResult {
        public Uri uri;
        public File file;

        public PdfResult(Uri uri, File file) {
            this.uri = uri;
            this.file = file;
        }
    }

    /**
     * Membuat file PDF Struk Tagihan Individual.
     */
    public static PdfResult generateStrukPdf(Context context, Pelanggan pelanggan, Tagihan tagihan) {
        if (pelanggan == null || tagihan == null) return null;

        String namaDesa = PreferencesManager.getNamaDesa(context);
        PdfDocument document = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Header
        paint.setColor(Color.parseColor("#0F2027"));
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("PENGELOLA AIR MINUM DESA (PAMDES)", 40, 50, paint);

        paint.setTextSize(16);
        paint.setColor(Color.parseColor("#0083B0"));
        canvas.drawText(namaDesa.toUpperCase(), 40, 75, paint);

        // Garis Pembatas Header
        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setStrokeWidth(2);
        canvas.drawLine(40, 95, pageWidth - 40, 95, paint);

        // Judul Struk
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("STRUK TAGIHAN AIR BULANAN", 40, 130, paint);

        // Detail Pelanggan & Periode
        paint.setTextSize(14);
        paint.setTypeface(Typeface.DEFAULT);
        int y = 170;
        int lineGap = 26;

        drawLabelValue(canvas, paint, "Nama Pelanggan", pelanggan.getNama(), 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Alamat", pelanggan.getAlamat(), 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Dusun", pelanggan.getDusun(), 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Periode Tagihan", FormatUtils.getNamaBulan(tagihan.getBulan()) + " " + tagihan.getTahun(), 40, y); y += lineGap + 10;

        // Garis Pembatas Rincian
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawLine(40, y, pageWidth - 40, y, paint);
        y += 30;

        // Tabel Rincian Meteran & Tagihan
        paint.setColor(Color.parseColor("#11998E"));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(14);
        canvas.drawText("RINCIAN PEMAKAIAN", 40, y, paint);
        y += lineGap;

        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT);

        drawLabelValue(canvas, paint, "Meteran Bulan Lalu", FormatUtils.formatMeter(tagihan.getMeteranLalu()) + " m³", 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Meteran Bulan Ini", FormatUtils.formatMeter(tagihan.getMeteranIni()) + " m³", 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Pemakaian Air", tagihan.getPemakaian() + " m³", 40, y); y += lineGap;
        drawLabelValue(canvas, paint, "Tarif per m³", FormatUtils.formatRupiah(tagihan.getTarifPerKubik()), 40, y); y += lineGap + 10;

        // Garis Total
        paint.setColor(Color.parseColor("#0083B0"));
        paint.setStrokeWidth(3);
        canvas.drawLine(40, y, pageWidth - 40, y, paint);
        y += 35;

        // Total Pembayaran
        paint.setColor(Color.parseColor("#0F2027"));
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("TOTAL TAGIHAN:", 40, y, paint);
        canvas.drawText(FormatUtils.formatRupiah(tagihan.getTotalBayar()), pageWidth - 220, y, paint);
        y += 50;

        // Footer Struk
        paint.setColor(Color.GRAY);
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("Dicetak pada: " + FormatUtils.formatTanggal(System.currentTimeMillis()), 40, y, paint); y += 20;
        canvas.drawText("Terima kasih atas pembayaran Anda. Simpan struk ini sebagai bukti pembayaran sah.", 40, y, paint);

        document.finishPage(page);

        String safeNama = pelanggan.getNama() != null ? pelanggan.getNama().replaceAll("[^a-zA-Z0-9_-]", "_") : "Pelanggan";
        String fileName = "Struk_" + safeNama + "_" + tagihan.getBulan() + "_" + tagihan.getTahun() + ".pdf";

        PdfResult result = savePdfToMediaStoreAndCache(context, document, fileName);
        document.close();
        return result;
    }

    /**
     * Membuat file PDF Rekap Bulanan dengan dukungan multi-halaman (paginasi).
     */
    public static PdfResult generateRekapPdf(Context context, int bulan, int tahun, String filterDusun, List<TagihanDetail> listDetail) {
        if (listDetail == null) return null;

        String namaDesa = PreferencesManager.getNamaDesa(context);
        PdfDocument document = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;
        int itemsPerPage = 18;
        int totalItems = listDetail.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        long totalPemakaian = 0;
        long totalTagihan = 0;
        for (TagihanDetail detail : listDetail) {
            if (detail != null && detail.tagihan != null) {
                totalPemakaian += detail.tagihan.getPemakaian();
                totalTagihan += detail.tagihan.getTotalBayar();
            }
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int p = 0; p < totalPages; p++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = 45;

            // Header Halaman
            paint.setColor(Color.parseColor("#0F2027"));
            paint.setTextSize(18);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("LAPORAN REKAPITULASI TAGIHAN AIR (PAMDES)", 40, y, paint); y += 25;

            paint.setTextSize(14);
            paint.setColor(Color.parseColor("#0083B0"));
            canvas.drawText(namaDesa.toUpperCase() + (filterDusun != null && !filterDusun.isEmpty() ? " - DUSUN " + filterDusun.toUpperCase() : ""), 40, y, paint); y += 20;

            paint.setTextSize(12);
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.DEFAULT);
            canvas.drawText("Periode: " + FormatUtils.getNamaBulan(bulan) + " " + tahun, 40, y, paint); y += 20;

            // Header Tabel
            int tableTop = y;
            paint.setColor(Color.parseColor("#11998E"));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(40, tableTop, pageWidth - 40, tableTop + 25, paint);

            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(11);

            canvas.drawText("NO", 45, tableTop + 17, paint);
            canvas.drawText("NAMA PELANGGAN", 80, tableTop + 17, paint);
            canvas.drawText("DUSUN", 260, tableTop + 17, paint);
            canvas.drawText("PEMAKAIAN", 370, tableTop + 17, paint);
            canvas.drawText("TOTAL (RP)", 470, tableTop + 17, paint);

            y = tableTop + 30;
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.DEFAULT);

            int startIndex = p * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

            for (int i = startIndex; i < endIndex; i++) {
                TagihanDetail item = listDetail.get(i);

                if (i % 2 == 1) {
                    Paint bgPaint = new Paint();
                    bgPaint.setColor(Color.parseColor("#F5F7FA"));
                    canvas.drawRect(40, y - 12, pageWidth - 40, y + 10, bgPaint);
                }

                canvas.drawText(String.valueOf(i + 1), 45, y, paint);
                canvas.drawText(truncateText(item.pelanggan != null ? item.pelanggan.getNama() : "-", 24), 80, y, paint);
                canvas.drawText(truncateText(item.pelanggan != null ? item.pelanggan.getDusun() : "-", 15), 260, y, paint);
                canvas.drawText((item.tagihan != null ? item.tagihan.getPemakaian() : 0) + " m³", 370, y, paint);
                canvas.drawText(FormatUtils.formatRupiah(item.tagihan != null ? item.tagihan.getTotalBayar() : 0), 470, y, paint);

                y += 22;
            }

            // Halaman Terakhir -> Tampilkan Ringkasan & Tanda Tangan
            if (p == totalPages - 1) {
                y += 15;
                paint.setColor(Color.parseColor("#0F2027"));
                paint.setStrokeWidth(2);
                canvas.drawLine(40, y, pageWidth - 40, y, paint);
                y += 25;

                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(12);
                canvas.drawText("RINGKASAN TOTAL:", 40, y, paint); y += 20;

                paint.setTypeface(Typeface.DEFAULT);
                canvas.drawText("Total Pelanggan: " + totalItems + " orang", 40, y, paint); y += 18;
                canvas.drawText("Total Pemakaian Air: " + totalPemakaian + " m³", 40, y, paint); y += 18;
                canvas.drawText("Total Tagihan Air: " + FormatUtils.formatRupiah(totalTagihan), 40, y, paint); y += 30;

                canvas.drawText("Dicetak: " + FormatUtils.formatTanggal(System.currentTimeMillis()), pageWidth - 220, y, paint); y += 40;
                canvas.drawText("Pengelola PAMDES", pageWidth - 220, y, paint);
            }

            // Footer Nomor Halaman
            paint.setColor(Color.GRAY);
            paint.setTextSize(10);
            paint.setTypeface(Typeface.DEFAULT);
            canvas.drawText("Halaman " + (p + 1) + " dari " + totalPages, pageWidth - 100, pageHeight - 30, paint);

            document.finishPage(page);
        }

        String fileName = "Rekap_PAMDES_" + bulan + "_" + tahun + ".pdf";
        PdfResult result = savePdfToMediaStoreAndCache(context, document, fileName);
        document.close();
        return result;
    }

    private static void drawLabelValue(Canvas canvas, Paint paint, String label, String value, int x, int y) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(label + ":", x, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(value != null ? value : "-", x + 180, y, paint);
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }

    /**
     * Menyimpan PDF secara aman ke Cache internal (untuk FileProvider & PrintManager)
     * serta MediaStore Documents/PAMDES (untuk penyimpanan permanen).
     */
    private static PdfResult savePdfToMediaStoreAndCache(Context context, PdfDocument document, String fileName) {
        Uri mediaStoreUri = null;
        File pdfFile = null;

        try {
            // Write PDF to byte array first
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.writeTo(baos);
            byte[] pdfBytes = baos.toByteArray();
            baos.close();

            // 1. Simpan ke Cache internal aplikasi
            File cacheDir = new File(context.getCacheDir(), "pdf_reports");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            pdfFile = new File(cacheDir, fileName);

            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.flush();
            fos.close();

            mediaStoreUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, pdfFile);

            // 2. Simpan ke MediaStore (Folder Documents/PAMDES) di API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/PAMDES");
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);

                Uri externalUri = MediaStore.Files.getContentUri("external");
                Uri insertedUri = context.getContentResolver().insert(externalUri, values);

                if (insertedUri != null) {
                    OutputStream os = context.getContentResolver().openOutputStream(insertedUri);
                    if (os != null) {
                        os.write(pdfBytes);
                        os.flush();
                        os.close();
                    }
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    context.getContentResolver().update(insertedUri, values, null, null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return new PdfResult(mediaStoreUri, pdfFile);
    }

    /**
     * Mencetak dokumen PDF secara langsung menggunakan layanan Android PrintManager bawaan.
     */
    public static void printPdf(Context context, File pdfFile, String jobName) {
        if (context == null || pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "File PDF tidak ditemukan untuk dicetak", Toast.LENGTH_SHORT).show();
            return;
        }

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            Toast.makeText(context, "Layanan Cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show();
            return;
        }

        String printJobName = (jobName != null && !jobName.isEmpty()) ? jobName : "PAMDES_Document";

        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                PrintDocumentInfo info = new PrintDocumentInfo.Builder(printJobName + ".pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build();

                callback.onLayoutFinished(info, !newAttributes.equals(oldAttributes));
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                FileInputStream input = null;
                FileOutputStream output = null;

                try {
                    input = new FileInputStream(pdfFile);
                    output = new FileOutputStream(destination.getFileDescriptor());

                    byte[] buf = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buf)) > 0) {
                        if (cancellationSignal.isCanceled()) {
                            callback.onWriteCancelled();
                            return;
                        }
                        output.write(buf, 0, bytesRead);
                    }

                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onWriteFailed(e.getMessage());
                } finally {
                    try {
                        if (input != null) input.close();
                        if (output != null) output.close();
                    } catch (Exception ignored) {}
                }
            }
        };

        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A5)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build();

        printManager.print(printJobName, printAdapter, attributes);
    }

    /**
     * Membuka dokumen PDF menggunakan Intent.ACTION_VIEW.
     */
    public static void openPdf(Context context, Uri uri) {
        if (uri == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Buka Dokumen PDF"));
        } catch (Exception e) {
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF terpasang", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Membagikan dokumen PDF (misal via WhatsApp, Email, dll).
     */
    public static void sharePdf(Context context, Uri uri, String title) {
        if (uri == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, title));
        } catch (Exception e) {
            Toast.makeText(context, "Gagal membagikan PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
