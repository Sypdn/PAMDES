package com.Syaepuddin.pamdes.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entitas database untuk menyimpan data Tagihan Bulanan Rekening Air.
 */
@Entity(
    tableName = "tagihan",
    foreignKeys = @ForeignKey(
        entity = Pelanggan.class,
        parentColumns = "id",
        childColumns = "pelangganId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = {"pelangganId", "bulan", "tahun"}, unique = true)}
)
public class Tagihan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int pelangganId;
    private int bulan;
    private int tahun;
    private String kategoriPelanggan; // "Rumah Tangga" / "Pengusaha"
    private long meteranLalu;
    private long meteranIni;
    private long pemakaian;
    private int tarifPerKubik; // 500 (Rumah Tangga) atau 1000 (Pengusaha)
    private long jumlahAir;    // pemakaian * tarifPerKubik
    private int biayaBeban;    // default 2000
    private int biayaPma;      // default 500
    private long totalBayar;   // jumlahAir + biayaBeban + biayaPma
    private long tanggalInput;
    private boolean statusBayar;

    public Tagihan(int pelangganId, int bulan, int tahun, String kategoriPelanggan, long meteranLalu, long meteranIni, long pemakaian, int tarifPerKubik, long jumlahAir, int biayaBeban, int biayaPma, long totalBayar, long tanggalInput, boolean statusBayar) {
        this.pelangganId = pelangganId;
        this.bulan = bulan;
        this.tahun = tahun;
        this.kategoriPelanggan = (kategoriPelanggan != null && !kategoriPelanggan.isEmpty()) ? kategoriPelanggan : "Rumah Tangga";
        this.meteranLalu = meteranLalu;
        this.meteranIni = meteranIni;
        this.pemakaian = pemakaian;
        this.tarifPerKubik = tarifPerKubik;
        this.jumlahAir = jumlahAir;
        this.biayaBeban = biayaBeban;
        this.biayaPma = biayaPma;
        this.totalBayar = totalBayar;
        this.tanggalInput = tanggalInput;
        this.statusBayar = statusBayar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPelangganId() {
        return pelangganId;
    }

    public void setPelangganId(int pelangganId) {
        this.pelangganId = pelangganId;
    }

    public int getBulan() {
        return bulan;
    }

    public void setBulan(int bulan) {
        this.bulan = bulan;
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public String getKategoriPelanggan() {
        return (kategoriPelanggan != null && !kategoriPelanggan.isEmpty()) ? kategoriPelanggan : "Rumah Tangga";
    }

    public void setKategoriPelanggan(String kategoriPelanggan) {
        this.kategoriPelanggan = kategoriPelanggan;
    }

    public long getMeteranLalu() {
        return meteranLalu;
    }

    public void setMeteranLalu(long meteranLalu) {
        this.meteranLalu = meteranLalu;
    }

    public long getMeteranIni() {
        return meteranIni;
    }

    public void setMeteranIni(long meteranIni) {
        this.meteranIni = meteranIni;
    }

    public long getPemakaian() {
        return pemakaian;
    }

    public void setPemakaian(long pemakaian) {
        this.pemakaian = pemakaian;
    }

    public int getTarifPerKubik() {
        return tarifPerKubik;
    }

    public void setTarifPerKubik(int tarifPerKubik) {
        this.tarifPerKubik = tarifPerKubik;
    }

    public long getJumlahAir() {
        return jumlahAir;
    }

    public void setJumlahAir(long jumlahAir) {
        this.jumlahAir = jumlahAir;
    }

    public int getBiayaBeban() {
        return biayaBeban;
    }

    public void setBiayaBeban(int biayaBeban) {
        this.biayaBeban = biayaBeban;
    }

    public int getBiayaPma() {
        return biayaPma;
    }

    public void setBiayaPma(int biayaPma) {
        this.biayaPma = biayaPma;
    }

    public long getTotalBayar() {
        return totalBayar;
    }

    public void setTotalBayar(long totalBayar) {
        this.totalBayar = totalBayar;
    }

    public long getTanggalInput() {
        return tanggalInput;
    }

    public void setTanggalInput(long tanggalInput) {
        this.tanggalInput = tanggalInput;
    }

    public boolean isStatusBayar() {
        return statusBayar;
    }

    public void setStatusBayar(boolean statusBayar) {
        this.statusBayar = statusBayar;
    }
}
