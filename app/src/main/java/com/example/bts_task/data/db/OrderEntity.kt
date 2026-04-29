package com.example.bts_task.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val destinationAddress: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val createdAt: Long
)
