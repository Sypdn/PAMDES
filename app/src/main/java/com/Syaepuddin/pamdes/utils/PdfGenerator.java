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
 * Disesuaikan 100% dengan format Nota Fisik Rekening Air "UTUH BERSATU" Wanasaba Daya.
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
     * Membuat file PDF Struk Tagihan Individual (1 Struk per Halaman A5 Landscape).
     */
    public static PdfResult generateStrukPdf(Context context, Pelanggan pelanggan, Tagihan tagihan) {
        if (pelanggan == null || tagihan == null) return null;

        String namaDesa = PreferencesManager.getNamaDesa(context);
        String judulHeader = PreferencesManager.getJudulHeader(context);
        String alamatHeader = PreferencesManager.getAlamatHeader(context);

        PdfDocument document = new PdfDocument();

        // Standard Struk Rekening Air Landscape format (595 x 420 pt)
        int pageWidth = 595;
        int pageHeight = 420;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw Single Large Struk Ticket
        drawSingleStrukTicketLarge(canvas, pelanggan, tagihan, namaDesa, judulHeader, alamatHeader, pageWidth, pageHeight);

        document.finishPage(page);

        String safeNama = pelanggan.getNama() != null ? pelanggan.getNama().replaceAll("[^a-zA-Z0-9_-]", "_") : "Pelanggan";
        String fileName = "RekeningAir_" + safeNama + "_" + tagihan.getBulan() + "_" + tagihan.getTahun() + ".pdf";

        PdfResult result = savePdfToMediaStoreAndCache(context, document, fileName);
        document.close();
        return result;
    }

    /**
     * Membuat file PDF Rekap Bulanan Massal dalam Format A4 (6 Struk Siap Cetak per Lembar A4).
     * Susunan: 2 Kolom x 3 Baris = 6 Struk Nota per Lembar A4.
     */
    public static PdfResult generateRekapPdf(Context context, int bulan, int tahun, String filterDusun, List<TagihanDetail> listDetail) {
        if (listDetail == null || listDetail.isEmpty()) return null;

        String namaDesa = PreferencesManager.getNamaDesa(context);
        String judulHeader = PreferencesManager.getJudulHeader(context);
        String alamatHeader = PreferencesManager.getAlamatHeader(context);

        PdfDocument document = new PdfDocument();

        // Standard A4 Portrait (595 x 842 pt)
        int pageWidth = 595;
        int pageHeight = 842;

        int ticketsPerPage = 6; // 2 kolom x 3 baris
        int totalItems = listDetail.size();
        int totalPages = (int) Math.ceil((double) totalItems / ticketsPerPage);

        // Dimensions for 6 tickets on A4 (2 columns x 3 rows)
        float marginLeft = 16f;
        float marginTop = 16f;
        float colGap = 15f;
        float rowGap = 16f;

        float ticketWidth = (pageWidth - (marginLeft * 2) - colGap) / 2f;  // ~264 pt
        float ticketHeight = (pageHeight - (marginTop * 2) - (rowGap * 2)) / 3f; // ~259 pt

        for (int p = 0; p < totalPages; p++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int startIndex = p * ticketsPerPage;
            int endIndex = Math.min(startIndex + ticketsPerPage, totalItems);

            for (int i = startIndex; i < endIndex; i++) {
                int itemIndexInPage = i - startIndex;
                int col = itemIndexInPage % 2; // 0 or 1
                int row = itemIndexInPage / 2; // 0, 1, or 2

                float x = marginLeft + (col * (ticketWidth + colGap));
                float y = marginTop + (row * (ticketHeight + rowGap));

                TagihanDetail item = listDetail.get(i);
                Pelanggan pelanggan = item != null ? item.pelanggan : null;
                Tagihan tagihan = item != null ? item.tagihan : null;

                if (tagihan != null) {
                    drawSingleStrukTicketCompact(canvas, pelanggan, tagihan, namaDesa, judulHeader, alamatHeader, x, y, ticketWidth, ticketHeight);
                }
            }

            document.finishPage(page);
        }

        String fileName = "StrukMassal_PAMDES_" + bulan + "_" + tahun + ".pdf";
        PdfResult result = savePdfToMediaStoreAndCache(context, document, fileName);
        document.close();
        return result;
    }

    /**
     * Menggambar 1 Nota Struk Rekening Air Kompak (untuk Grid A4 Massal 6 Struk per Lembar).
     */
    private static void drawSingleStrukTicketCompact(Canvas canvas, Pelanggan pelanggan, Tagihan tagihan, String namaDesa, String judulHeader, String alamatHeader, float left, float top, float width, float height) {
        float right = left + width;
        float bottom = top + height;

        // Outer double box border for cutting out ticket
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(1.5f);
        canvas.drawRect(left, top, right, bottom, borderPaint);

        borderPaint.setStrokeWidth(0.6f);
        canvas.drawRect(left + 2f, top + 2f, right - 2f, bottom - 2f, borderPaint);

        // Header Box Line
        float headerBottom = top + 52f;
        canvas.drawLine(left + 2f, headerBottom, right - 2f, headerBottom, borderPaint);

        // Header Content
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);

        // Left Emblem text
        textPaint.setTextSize(6.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LOMBOK TIMUR", left + 6f, top + 14f, textPaint);

        // Center Header
        float cx = left + (width / 2f);
        textPaint.setTextSize(8.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        drawCenteredText(canvas, textPaint, "PENGELOLA AIR MINUM DESA (PAMDES)", cx, top + 15f);

        textPaint.setTextSize(10f);
        drawCenteredText(canvas, textPaint, judulHeader, cx, top + 27f);

        textPaint.setTextSize(7f);
        textPaint.setTypeface(Typeface.DEFAULT);
        drawCenteredText(canvas, textPaint, namaDesa, cx, top + 38f);
        drawCenteredText(canvas, textPaint, alamatHeader, cx, top + 48f);

        // Title: "REKENING AIR"
        textPaint.setTextSize(9.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setUnderlineText(true);
        drawCenteredText(canvas, textPaint, "REKENING AIR", cx, top + 66f);
        textPaint.setUnderlineText(false);

        // Info rows (Y: top + 78 to top + 120)
        textPaint.setTextSize(7.5f);
        textPaint.setTypeface(Typeface.DEFAULT);

        float infoY = top + 78f;
        float gapY = 11f;
        float col1X = left + 8f;
        float col2X = left + 75f;

        String namaStr = pelanggan != null ? pelanggan.getNama() : "-";
        String dusunStr = pelanggan != null ? pelanggan.getDusun() : "-";
        String kategoriStr = tagihan != null ? tagihan.getKategoriPelanggan() : "-";

        String bulanTahun = tagihan != null ? FormatUtils.getNamaBulan(tagihan.getBulan()) + " " + tagihan.getTahun() : "-";
        drawInfoRow(canvas, textPaint, "Rekening Bulan", bulanTahun, col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Wilayah", dusunStr, col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Jenis Tarif", kategoriStr, col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Nama", namaStr, col1X, col2X, infoY);

        // Table Grid (Y: top + 128 to top + 215)
        float tableLeft = left + 6f;
        float tableRight = right - 6f;
        float tableTop = top + 128f;
        float tableHeaderBottom = top + 162f;
        float tableBottom = top + 215f;

        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, borderPaint);
        canvas.drawLine(tableLeft, tableHeaderBottom, tableRight, tableHeaderBottom, borderPaint);

        // 8 Columns across table
        float tW = tableRight - tableLeft;
        float[] colX = {
                tableLeft,
                tableLeft + tW * 0.11f,
                tableLeft + tW * 0.22f,
                tableLeft + tW * 0.33f,
                tableLeft + tW * 0.47f,
                tableLeft + tW * 0.60f,
                tableLeft + tW * 0.73f,
                tableLeft + tW * 0.86f,
                tableRight
        };

        for (int i = 1; i < colX.length - 1; i++) {
            canvas.drawLine(colX[i], tableTop, colX[i], tableBottom, borderPaint);
        }

        // Header Column Texts
        textPaint.setTextSize(6.2f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        drawCellText2Lines(canvas, textPaint, "Meter", "Awal", colX[0], colX[1], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Meter", "Akhir", colX[1], colX[2], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Pakai", "(m3)", colX[2], colX[3], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Jumlah", "Rp.", colX[3], colX[4], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Beban", "Rp.", colX[4], colX[5], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Dana", "PMA", colX[5], colX[6], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Total", "Rp.", colX[6], colX[7], tableTop + 11f);
        drawCellText2Lines(canvas, textPaint, "Harga", "Perkubikasi", colX[7], colX[8], tableTop + 11f);

        // Data Row Values
        textPaint.setTextSize(7f);
        textPaint.setTypeface(Typeface.DEFAULT);

        drawCellText(canvas, textPaint, String.valueOf(tagihan != null ? tagihan.getMeteranLalu() : 0), colX[0], colX[1], tableHeaderBottom + 28f);
        drawCellText(canvas, textPaint, String.valueOf(tagihan != null ? tagihan.getMeteranIni() : 0), colX[1], colX[2], tableHeaderBottom + 28f);
        drawCellText(canvas, textPaint, String.valueOf(tagihan != null ? tagihan.getPemakaian() : 0), colX[2], colX[3], tableHeaderBottom + 28f);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan != null ? tagihan.getJumlahAir() : 0), colX[3], colX[4], tableHeaderBottom + 28f);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan != null ? tagihan.getBiayaBeban() : 0), colX[4], colX[5], tableHeaderBottom + 28f);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan != null ? tagihan.getBiayaPma() : 0), colX[5], colX[6], tableHeaderBottom + 28f);

        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan != null ? tagihan.getTotalBayar() : 0), colX[6], colX[7], tableHeaderBottom + 28f);

        textPaint.setTypeface(Typeface.DEFAULT);
        drawCellText(canvas, textPaint, "Rp." + (tagihan != null ? tagihan.getTarifPerKubik() : 0), colX[7], colX[8], tableHeaderBottom + 28f);

        // Footer Metadata
        textPaint.setTextSize(6f);
        textPaint.setColor(Color.GRAY);
        canvas.drawText("Dicetak: " + FormatUtils.formatTanggal(System.currentTimeMillis()), left + 6f, bottom - 8f, textPaint);
        canvas.drawText("PAMDES Wanasaba Daya", right - 75f, bottom - 8f, textPaint);
    }

    /**
     * Menggambar 1 Nota Struk Rekening Air Ukuran Besar (A5 Landscape Individual).
     */
    private static void drawSingleStrukTicketLarge(Canvas canvas, Pelanggan pelanggan, Tagihan tagihan, String namaDesa, String judulHeader, String alamatHeader, float pageWidth, float pageHeight) {
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(2.5f);
        canvas.drawRect(20, 20, 575, 400, borderPaint);

        borderPaint.setStrokeWidth(1.0f);
        canvas.drawRect(24, 24, 571, 396, borderPaint);

        // Header Box Line
        canvas.drawLine(24, 105, 571, 105, borderPaint);

        // Header Content
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);

        // Left Emblem text: "LOMBOK TIMUR"
        textPaint.setTextSize(9);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LOMBOK TIMUR", 32, 42, textPaint);

        // Center Header Info
        textPaint.setTextSize(13);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        drawCenteredText(canvas, textPaint, "PENGELOLA AIR MINUM DESA (PAMDES)", 297, 44);

        textPaint.setTextSize(15);
        drawCenteredText(canvas, textPaint, judulHeader, 297, 63);

        textPaint.setTextSize(10);
        textPaint.setTypeface(Typeface.DEFAULT);
        drawCenteredText(canvas, textPaint, namaDesa, 297, 78);
        drawCenteredText(canvas, textPaint, alamatHeader, 297, 93);

        // Judul Struk: "REKENING AIR" (Underlined, Centered, Bold)
        textPaint.setTextSize(14);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setUnderlineText(true);
        drawCenteredText(canvas, textPaint, "REKENING AIR", 297, 124);
        textPaint.setUnderlineText(false);

        // Information Fields List (Sesuai Kolom Nota Fisik)
        textPaint.setTextSize(11);
        textPaint.setTypeface(Typeface.DEFAULT);

        int infoY = 150;
        int gapY = 18;
        int col1X = 40;
        int col2X = 150;

        drawInfoRow(canvas, textPaint, "Rekening Bulan", FormatUtils.getNamaBulan(tagihan.getBulan()) + " " + tagihan.getTahun(), col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Wilayah", pelanggan.getDusun(), col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Jenis Tarif", tagihan.getKategoriPelanggan(), col1X, col2X, infoY); infoY += gapY;
        drawInfoRow(canvas, textPaint, "Nama", pelanggan.getNama(), col1X, col2X, infoY);

        // Tabel Rincian (Box Grid Sesuai Nota Fisik)
        int tableLeft = 32;
        int tableRight = 563;
        int tableTop = 240;
        int tableHeaderBottom = 280;
        int tableBottom = 330;

        // Outer Table Box & Line Dividers
        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, borderPaint);
        canvas.drawLine(tableLeft, tableHeaderBottom, tableRight, tableHeaderBottom, borderPaint);

        // Column X Boundaries: 8 Columns
        int[] colX = {32, 95, 158, 220, 295, 365, 435, 500, 563};

        for (int i = 1; i < colX.length - 1; i++) {
            canvas.drawLine(colX[i], tableTop, colX[i], tableBottom, borderPaint);
        }

        // Header Text Columns
        textPaint.setTextSize(9.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        drawCellText2Lines(canvas, textPaint, "Meter", "Awal", colX[0], colX[1], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Meter", "Akhir", colX[1], colX[2], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Pakai", "(m3)", colX[2], colX[3], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Jumlah", "Rp.", colX[3], colX[4], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Beban", "Rp.", colX[4], colX[5], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Dana", "PMA", colX[5], colX[6], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Total", "Rp.", colX[6], colX[7], tableTop + 14);
        drawCellText2Lines(canvas, textPaint, "Harga", "Perkubikasi", colX[7], colX[8], tableTop + 14);

        // Data Row Values
        textPaint.setTextSize(10.5f);
        textPaint.setTypeface(Typeface.DEFAULT);

        drawCellText(canvas, textPaint, String.valueOf(tagihan.getMeteranLalu()), colX[0], colX[1], tableHeaderBottom + 30);
        drawCellText(canvas, textPaint, String.valueOf(tagihan.getMeteranIni()), colX[1], colX[2], tableHeaderBottom + 30);
        drawCellText(canvas, textPaint, String.valueOf(tagihan.getPemakaian()), colX[2], colX[3], tableHeaderBottom + 30);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan.getJumlahAir()), colX[3], colX[4], tableHeaderBottom + 30);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan.getBiayaBeban()), colX[4], colX[5], tableHeaderBottom + 30);
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan.getBiayaPma()), colX[5], colX[6], tableHeaderBottom + 30);

        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        drawCellText(canvas, textPaint, FormatUtils.formatNominalSimpel(tagihan.getTotalBayar()), colX[6], colX[7], tableHeaderBottom + 30);

        textPaint.setTypeface(Typeface.DEFAULT);
        drawCellText(canvas, textPaint, "Rp. " + tagihan.getTarifPerKubik() + ",-", colX[7], colX[8], tableHeaderBottom + 30);

        // Footer Metadata
        textPaint.setTextSize(9f);
        textPaint.setColor(Color.GRAY);
        canvas.drawText("Dicetak: " + FormatUtils.formatTanggal(System.currentTimeMillis()), 32, 365, textPaint);
        canvas.drawText("Pengelola PAMDES Wanasaba Daya", 370, 365, textPaint);
    }

    private static void drawCenteredText(Canvas canvas, Paint paint, String text, float cx, float y) {
        if (text == null) return;
        float width = paint.measureText(text);
        canvas.drawText(text, cx - (width / 2f), y, paint);
    }

    private static void drawInfoRow(Canvas canvas, Paint paint, String label, String value, float x1, float x2, float y) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(label, x1, y, paint);
        canvas.drawText(":", x2 - 10, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(value != null ? value : "-", x2, y, paint);
    }

    private static void drawCellText(Canvas canvas, Paint paint, String text, float left, float right, float y) {
        if (text == null) return;
        float width = paint.measureText(text);
        float cx = (left + right) / 2f;
        canvas.drawText(text, cx - (width / 2f), y, paint);
    }

    private static void drawCellText2Lines(Canvas canvas, Paint paint, String line1, String line2, float left, float right, float y) {
        float cx = (left + right) / 2f;
        if (line1 != null) {
            float w1 = paint.measureText(line1);
            canvas.drawText(line1, cx - (w1 / 2f), y, paint);
        }
        if (line2 != null) {
            float w2 = paint.measureText(line2);
            canvas.drawText(line2, cx - (w2 / 2f), y + 12, paint);
        }
    }

    /**
     * Menyimpan PDF secara aman ke Cache internal (untuk FileProvider & PrintManager)
     * serta MediaStore Documents/PAMDES (untuk penyimpanan permanen).
     */
    private static PdfResult savePdfToMediaStoreAndCache(Context context, PdfDocument document, String fileName) {
        Uri mediaStoreUri = null;
        File pdfFile = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.writeTo(baos);
            byte[] pdfBytes = baos.toByteArray();
            baos.close();

            File cacheDir = new File(context.getCacheDir(), "pdf_reports");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            pdfFile = new File(cacheDir, fileName);

            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.flush();
            fos.close();

            mediaStoreUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, pdfFile);

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
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
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
