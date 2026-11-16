package com.example.calendario.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AlertEntity.class, UserProfileEntity.class, CacheStatusEntity.class}, version = 1, exportSchema = true)
public abstract class CampusDatabase extends RoomDatabase {
    public abstract CampusDao campusDao();
}
