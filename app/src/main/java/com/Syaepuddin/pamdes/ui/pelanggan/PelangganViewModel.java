package com.Syaepuddin.pamdes.ui.pelanggan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.repository.PamdesRepository;

import java.util.List;

public class PelangganViewModel extends AndroidViewModel {

    private final PamdesRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<Pelanggan>> pelangganList;

    public PelangganViewModel(@NonNull Application application) {
        super(application);
        repository = new PamdesRepository(application);

        // Simple LiveData observer for query search
        pelangganList = repository.searchPelanggan("");
    }

    public LiveData<List<Pelanggan>> getPelangganList() {
        return pelangganList;
    }

    public LiveData<List<Pelanggan>> searchPelanggan(String query) {
        return repository.searchPelanggan(query);
    }

    public void insertPelanggan(String nama, String alamat, String dusun, long meteranAwal, PamdesRepository.Callback<Long> callback) {
        Pelanggan pelanggan = new Pelanggan(nama, alamat, dusun, meteranAwal, meteranAwal, System.currentTimeMillis());
        repository.insertPelanggan(pelanggan, callback);
    }

    public void updatePelanggan(Pelanggan pelanggan) {
        repository.updatePelanggan(pelanggan);
    }

    public void deletePelanggan(Pelanggan pelanggan) {
        repository.deletePelanggan(pelanggan);
    }
}
