package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM operator_profile WHERE id = 1")
    fun observeProfile(): Flow<OperatorProfileEntity?>

    @Query("SELECT * FROM operator_profile WHERE id = 1")
    suspend fun getProfileOnce(): OperatorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: OperatorProfileEntity)

    @Query("SELECT * FROM survival_stash WHERE id = 1")
    fun observeStash(): Flow<SurvivalStashEntity?>

    @Query("SELECT * FROM survival_stash WHERE id = 1")
    suspend fun getStashOnce(): SurvivalStashEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStash(stash: SurvivalStashEntity)

    @Query("SELECT * FROM squad_deployment_logs ORDER BY timestampMs DESC LIMIT 12")
    fun observeDeploymentLogs(): Flow<List<SquadDeploymentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeploymentLog(log: SquadDeploymentLogEntity)
}
