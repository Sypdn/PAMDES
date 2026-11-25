package com.Syaepuddin.pamdes.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.Syaepuddin.pamdes.data.dao.PelangganDao;
import com.Syaepuddin.pamdes.data.dao.TagihanDao;
import com.Syaepuddin.pamdes.data.database.AppDatabase;
import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.model.Tagihan;
import com.Syaepuddin.pamdes.data.model.TagihanDetail;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository yang mengelola operasi data asynchronously.
 */
public class PamdesRepository {

    private final PelangganDao pelangganDao;
    private final TagihanDao tagihanDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public PamdesRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        pelangganDao = db.pelangganDao();
        tagihanDao = db.tagihanDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // PELANGGAN OPERATIONS
    public LiveData<List<Pelanggan>> getAllPelanggan() {
        return pelangganDao.getAllPelanggan();
    }

    public LiveData<List<Pelanggan>> searchPelanggan(String query) {
        return pelangganDao.searchPelanggan(query);
    }

    public LiveData<List<String>> getAllDusun() {
        return pelangganDao.getAllDusun();
    }

    public LiveData<Integer> getJumlahPelanggan() {
        return pelangganDao.getJumlahPelanggan();
    }

    public void insertPelanggan(Pelanggan pelanggan, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = pelangganDao.insert(pelanggan);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void updatePelanggan(Pelanggan pelanggan) {
        executorService.execute(() -> pelangganDao.update(pelanggan));
    }

    public void deletePelanggan(Pelanggan pelanggan) {
        executorService.execute(() -> pelangganDao.delete(pelanggan));
    }

    // TAGIHAN OPERATIONS
    public void getExistingTagihan(int pelangganId, int bulan, int tahun, Callback<Tagihan> callback) {
        executorService.execute(() -> {
            Tagihan tagihan = tagihanDao.getTagihanByPelangganBulanTahunSync(pelangganId, bulan, tahun);
            if (callback != null) {
                callback.onResult(tagihan);
            }
        });
    }

    public void simpanTagihan(Tagihan tagihan, Callback<Long> callback) {
        executorService.execute(() -> {
            long resultId = tagihanDao.insert(tagihan);
            // Otomatis perbarui meteran terakhir pelanggan
            pelangganDao.updateMeteranTerakhir(tagihan.getPelangganId(), tagihan.getMeteranIni());
            if (callback != null) {
                callback.onResult(resultId);
            }
        });
    }

    public void updateStatusBayar(int tagihanId, boolean statusBayar) {
        executorService.execute(() -> tagihanDao.updateStatusBayar(tagihanId, statusBayar));
    }

    public LiveData<List<TagihanDetail>> getTagihanDetailFiltered(int bulan, int tahun, String dusun, String query, int statusFilter) {
        return tagihanDao.getTagihanDetailFiltered(bulan, tahun, dusun, query, statusFilter);
    }

    public void getTagihanDetailFilteredSync(int bulan, int tahun, String dusun, String query, int statusFilter, Callback<List<TagihanDetail>> callback) {
        executorService.execute(() -> {
            List<TagihanDetail> list = tagihanDao.getTagihanDetailFilteredSync(bulan, tahun, dusun, query, statusFilter);
            if (callback != null) {
                callback.onResult(list);
            }
        });
    }

    public LiveData<Integer> getJumlahTagihanBulan(int bulan, int tahun) {
        return tagihanDao.getJumlahTagihanBulan(bulan, tahun);
    }
}
