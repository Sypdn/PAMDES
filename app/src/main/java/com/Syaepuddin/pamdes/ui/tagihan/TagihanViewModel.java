package com.Syaepuddin.pamdes.ui.tagihan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.model.Tagihan;
import com.Syaepuddin.pamdes.data.repository.PamdesRepository;

import java.util.List;

public class TagihanViewModel extends AndroidViewModel {

    private final PamdesRepository repository;

    public TagihanViewModel(@NonNull Application application) {
        super(application);
        repository = new PamdesRepository(application);
    }

    public LiveData<List<Pelanggan>> searchPelanggan(String query) {
        return repository.searchPelanggan(query);
    }

    public LiveData<List<Pelanggan>> getAllPelanggan() {
        return repository.getAllPelanggan();
    }

    public void checkExistingTagihan(int pelangganId, int bulan, int tahun, PamdesRepository.Callback<Tagihan> callback) {
        repository.getExistingTagihan(pelangganId, bulan, tahun, callback);
    }

    public void simpanTagihan(Tagihan tagihan, PamdesRepository.Callback<Long> callback) {
        repository.simpanTagihan(tagihan, callback);
    }
}
