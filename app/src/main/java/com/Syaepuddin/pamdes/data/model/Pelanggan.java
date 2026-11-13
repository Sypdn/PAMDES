package com.Syaepuddin.pamdes.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entitas database untuk menyimpan data Pelanggan Air Desa.
 */
@Entity(tableName = "pelanggan")
public class Pelanggan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nama;
    private String alamat;
    private String dusun;
    private long meteranAwal;
    private long meteranTerakhir;
    private long createdAt;

    public Pelanggan(String nama, String alamat, String dusun, long meteranAwal, long meteranTerakhir, long createdAt) {
        this.nama = nama;
        this.alamat = alamat;
        this.dusun = dusun;
        this.meteranAwal = meteranAwal;
        this.meteranTerakhir = meteranTerakhir;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getDusun() {
        return dusun;
    }

    public void setDusun(String dusun) {
        this.dusun = dusun;
    }

    public long getMeteranAwal() {
        return meteranAwal;
    }

    public void setMeteranAwal(long meteranAwal) {
        this.meteranAwal = meteranAwal;
    }

    public long getMeteranTerakhir() {
        return meteranTerakhir;
    }

    public void setMeteranTerakhir(long meteranTerakhir) {
        this.meteranTerakhir = meteranTerakhir;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
