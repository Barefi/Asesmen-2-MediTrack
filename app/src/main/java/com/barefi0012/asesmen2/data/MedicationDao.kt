package com.barefi0012.asesmen2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query(
        """
        SELECT * FROM medications
        WHERE isDeleted = 0
        AND (
            ownerEmail = :ownerEmail
            OR (:ownerEmail = '__guest__' AND ownerEmail = '')
        )
        ORDER BY time ASC
        """
    )
    fun getAllActiveMedications(ownerEmail: String): Flow<List<Medication>>

    @Query(
        """
        SELECT * FROM medications
        WHERE isDeleted = 1
        AND (
            ownerEmail = :ownerEmail
            OR (:ownerEmail = '__guest__' AND ownerEmail = '')
        )
        """
    )
    fun getDeletedMedications(ownerEmail: String): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query(
        """
        SELECT * FROM medications
        WHERE id = :id
        AND (
            ownerEmail = :ownerEmail
            OR (:ownerEmail = '__guest__' AND ownerEmail = '')
        )
        LIMIT 1
        """
    )
    suspend fun getMedicationById(id: Int, ownerEmail: String): Medication?

    @Delete
    suspend fun permanentDelete(medication: Medication)
}
