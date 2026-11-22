package com.Syaepuddin.pamdes.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entitas database untuk menyimpan data Pelanggan Air Desa Wanasaba Daya.
 */
@Entity(tableName = "pelanggan")
public class Pelanggan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nama;
    private String dusun;
    private String kategori; // "Rumah Tangga" (Rp 500/m³) atau "Pengusaha" (Rp 1.000/m³)
    private long meteranAwal;
    private long meteranTerakhir;
    private long createdAt;

    public Pelanggan(String nama, String dusun, String kategori, long meteranAwal, long meteranTerakhir, long createdAt) {
        this.nama = nama;
        this.dusun = dusun;
        this.kategori = (kategori != null && !kategori.isEmpty()) ? kategori : "Rumah Tangga";
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

    public String getDusun() {
        return dusun;
    }

    public void setDusun(String dusun) {
        this.dusun = dusun;
    }

    public String getKategori() {
        return (kategori != null && !kategori.isEmpty()) ? kategori : "Rumah Tangga";
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
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
