package com.Syaepuddin.pamdes.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Model relasi gabungan antara Tagihan dan Pelanggan.
 */
public class TagihanDetail {

    @Embedded
    public Tagihan tagihan;

    @Relation(
        parentColumn = "pelangganId",
        entityColumn = "id"
    )
    public Pelanggan pelanggan;
}
