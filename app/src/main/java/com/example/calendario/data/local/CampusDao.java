package com.example.calendario.data.local;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CampusDao {
    @Query("SELECT * FROM alerts WHERE active = 1 ORDER BY publishedAtMillis DESC, id ASC")
    List<AlertEntity> getActiveAlerts();

    @Query("SELECT * FROM alerts WHERE pendingUpload = 1")
    List<AlertEntity> getPendingAlerts();

    @Nullable @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    AlertEntity getAlert(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAlert(AlertEntity alert);

    @Query("DELETE FROM alerts WHERE pendingUpload = 0 AND id NOT IN (:ids)")
    void deleteSyncedExcept(List<String> ids);

    @Query("DELETE FROM alerts WHERE pendingUpload = 0")
    void deleteSyncedAlerts();

    @Nullable @Query("SELECT * FROM user_profile WHERE id = 1")
    UserProfileEntity getProfile();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertProfile(UserProfileEntity profile);

    @Nullable @Query("SELECT * FROM cache_status WHERE id = 1")
    CacheStatusEntity getCacheStatus();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertCacheStatus(CacheStatusEntity status);
}
