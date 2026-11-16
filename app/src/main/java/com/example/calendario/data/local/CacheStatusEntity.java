package com.example.calendario.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cache_status")
public class CacheStatusEntity {
    @PrimaryKey public int id = 1;
    public boolean hasServerSnapshot;
}
