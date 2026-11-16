package com.example.calendario.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @PrimaryKey public int id = 1;
    @NonNull public String name = "";
    @NonNull public String email = "";
}
