package com.example.calendario.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts")
public class AlertEntity {
    @PrimaryKey @NonNull public String id = "";
    @NonNull public String title = "";
    @NonNull public String message = "";
    @NonNull public String course = "";
    @NonNull public String previousRoom = "";
    @NonNull public String newRoom = "";
    @NonNull public String building = "";
    @NonNull public String authorId = "";
    public long publishedAtMillis;
    public boolean active = true;
    public boolean pendingUpload;
}
