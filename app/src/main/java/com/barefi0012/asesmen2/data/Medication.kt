package com.barefi0012.asesmen2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerEmail: String,
    val name: String,
    val dosage: String,
    val time: String,
    val photoPath: String? = null,
    val isTaken: Boolean = false,
    val isDeleted: Boolean = false
)
