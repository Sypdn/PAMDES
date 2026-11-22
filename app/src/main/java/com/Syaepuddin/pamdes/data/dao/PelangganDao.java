package com.Syaepuddin.pamdes.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.Syaepuddin.pamdes.data.model.Pelanggan;

import java.util.List;

@Dao
public interface PelangganDao {

    @Insert
    long insert(Pelanggan pelanggan);

    @Update
    void update(Pelanggan pelanggan);

    @Delete
    void delete(Pelanggan pelanggan);

    @Query("SELECT * FROM pelanggan ORDER BY nama ASC")
    LiveData<List<Pelanggan>> getAllPelanggan();

    @Query("SELECT * FROM pelanggan WHERE nama LIKE '%' || :query || '%' OR dusun LIKE '%' || :query || '%' ORDER BY nama ASC")
    LiveData<List<Pelanggan>> searchPelanggan(String query);

    @Query("SELECT * FROM pelanggan WHERE id = :id LIMIT 1")
    Pelanggan getPelangganByIdSync(int id);

    @Query("SELECT * FROM pelanggan ORDER BY nama ASC")
    List<Pelanggan> getAllPelangganSync();

    @Query("SELECT DISTINCT dusun FROM pelanggan WHERE dusun IS NOT NULL AND dusun != '' ORDER BY dusun ASC")
    LiveData<List<String>> getAllDusun();

    @Query("SELECT COUNT(*) FROM pelanggan")
    LiveData<Integer> getJumlahPelanggan();

    @Query("UPDATE pelanggan SET meteranTerakhir = :meteranTerakhir WHERE id = :id")
    void updateMeteranTerakhir(int id, long meteranTerakhir);
}
