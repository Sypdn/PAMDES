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

    public LiveData<List<TagihanDetail>> getTagihanDetailFiltered(int bulan, int tahun, String dusun, String query, int statusFilter) {
        return repository.getTagihanDetailFiltered(bulan, tahun, dusun, query, statusFilter);
    }

    public void updateStatusBayar(int tagihanId, boolean statusBayar) {
        repository.updateStatusBayar(tagihanId, statusBayar);
    }

    public void getTagihanDetailFilteredSync(int bulan, int tahun, String dusun, String query, int statusFilter, PamdesRepository.Callback<List<TagihanDetail>> callback) {
        repository.getTagihanDetailFilteredSync(bulan, tahun, dusun, query, statusFilter, callback);
    }
}
