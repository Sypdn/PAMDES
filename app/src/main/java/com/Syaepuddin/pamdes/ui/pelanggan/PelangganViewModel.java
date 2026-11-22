package com.Syaepuddin.pamdes.ui.pelanggan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.repository.PamdesRepository;

import java.util.List;

public class PelangganViewModel extends AndroidViewModel {

    private final PamdesRepository repository;

    public PelangganViewModel(@NonNull Application application) {
        super(application);
        repository = new PamdesRepository(application);
    }

    public LiveData<List<Pelanggan>> getPelangganList() {
        return repository.getAllPelanggan();
    }

    public LiveData<List<Pelanggan>> searchPelanggan(String query) {
        return repository.searchPelanggan(query);
    }

    public void insertPelanggan(String nama, String dusun, String kategori, long meteranAwal, PamdesRepository.Callback<Long> callback) {
        Pelanggan pelanggan = new Pelanggan(nama, dusun, kategori, meteranAwal, meteranAwal, System.currentTimeMillis());
        repository.insertPelanggan(pelanggan, callback);
    }

    public void updatePelanggan(Pelanggan pelanggan) {
        repository.updatePelanggan(pelanggan);
    }

    public void deletePelanggan(Pelanggan pelanggan) {
        repository.deletePelanggan(pelanggan);
    }
}
