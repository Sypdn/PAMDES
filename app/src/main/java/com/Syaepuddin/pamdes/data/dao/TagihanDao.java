package com.Syaepuddin.pamdes.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.Syaepuddin.pamdes.data.model.Tagihan;
import com.Syaepuddin.pamdes.data.model.TagihanDetail;

import java.util.List;

@Dao
public interface TagihanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Tagihan tagihan);

    @Update
    void update(Tagihan tagihan);

    @Delete
    void delete(Tagihan tagihan);

    @Query("UPDATE tagihan SET statusBayar = :statusBayar WHERE id = :id")
    void updateStatusBayar(int id, boolean statusBayar);

    @Query("SELECT * FROM tagihan WHERE pelangganId = :pelangganId AND bulan = :bulan AND tahun = :tahun LIMIT 1")
    Tagihan getTagihanByPelangganBulanTahunSync(int pelangganId, int bulan, int tahun);

    @Transaction
    @Query("SELECT * FROM tagihan WHERE bulan = :bulan AND tahun = :tahun ORDER BY id DESC")
    LiveData<List<TagihanDetail>> getTagihanDetailByBulanTahun(int bulan, int tahun);

    @Transaction
    @Query("SELECT tagihan.* FROM tagihan INNER JOIN pelanggan ON tagihan.pelangganId = pelanggan.id WHERE tagihan.bulan = :bulan AND tagihan.tahun = :tahun AND (:dusun IS NULL OR :dusun = '' OR pelanggan.dusun = :dusun) AND (:query IS NULL OR :query = '' OR pelanggan.nama LIKE '%' || :query || '%') AND (:statusFilter = -1 OR tagihan.statusBayar = :statusFilter) ORDER BY pelanggan.nama ASC")
    LiveData<List<TagihanDetail>> getTagihanDetailFiltered(int bulan, int tahun, String dusun, String query, int statusFilter);

    @Transaction
    @Query("SELECT tagihan.* FROM tagihan INNER JOIN pelanggan ON tagihan.pelangganId = pelanggan.id WHERE tagihan.bulan = :bulan AND tagihan.tahun = :tahun AND (:dusun IS NULL OR :dusun = '' OR pelanggan.dusun = :dusun) AND (:query IS NULL OR :query = '' OR pelanggan.nama LIKE '%' || :query || '%') AND (:statusFilter = -1 OR tagihan.statusBayar = :statusFilter) ORDER BY pelanggan.nama ASC")
    List<TagihanDetail> getTagihanDetailFilteredSync(int bulan, int tahun, String dusun, String query, int statusFilter);

    @Query("SELECT COUNT(*) FROM tagihan WHERE bulan = :bulan AND tahun = :tahun")
    LiveData<Integer> getJumlahTagihanBulan(int bulan, int tahun);
}
