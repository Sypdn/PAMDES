package com.Syaepuddin.pamdes.ui.rekap;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.Syaepuddin.pamdes.data.model.TagihanDetail;
import com.Syaepuddin.pamdes.data.repository.PamdesRepository;

import java.util.List;

public class RekapViewModel extends AndroidViewModel {

    private final PamdesRepository repository;

    public RekapViewModel(@NonNull Application application) {
        super(application);
        repository = new PamdesRepository(application);
    }

    public LiveData<List<String>> getAllDusun() {
        return repository.getAllDusun();
    }

    public LiveData<List<TagihanDetail>> getTagihanDetailFiltered(int bulan, int tahun, String dusun, String query) {
        return repository.getTagihanDetailFiltered(bulan, tahun, dusun, query);
    }

    public void getTagihanDetailFilteredSync(int bulan, int tahun, String dusun, String query, PamdesRepository.Callback<List<TagihanDetail>> callback) {
        repository.getTagihanDetailFilteredSync(bulan, tahun, dusun, query, callback);
    }
}
