package com.Syaepuddin.pamdes.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.Syaepuddin.pamdes.data.dao.PelangganDao;
import com.Syaepuddin.pamdes.data.dao.TagihanDao;
import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.model.Tagihan;

@Database(entities = {Pelanggan.class, Tagihan.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PelangganDao pelangganDao();
    public abstract TagihanDao tagihanDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "pamdes_database.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
