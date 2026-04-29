package com.example.bts_task.domain.model

data class Order(
    val id: Long,
    val number: String,
    val pickupAddress: String,
    val pickupPoint: GeoPoint,
    val destinationAddress: String,
    val destinationPoint: GeoPoint,
    val createdAt: Long
)
