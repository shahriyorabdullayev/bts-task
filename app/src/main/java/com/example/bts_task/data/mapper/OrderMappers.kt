package com.example.bts_task.data.mapper

import com.example.bts_task.data.db.OrderEntity
import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.model.Order

fun OrderEntity.toDomain(): Order = Order(
    id = id,
    number = number,
    pickupAddress = pickupAddress,
    pickupPoint = GeoPoint(pickupLat, pickupLng),
    destinationAddress = destinationAddress,
    destinationPoint = GeoPoint(destinationLat, destinationLng),
    createdAt = createdAt
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    number = number,
    pickupAddress = pickupAddress,
    pickupLat = pickupPoint.latitude,
    pickupLng = pickupPoint.longitude,
    destinationAddress = destinationAddress,
    destinationLat = destinationPoint.latitude,
    destinationLng = destinationPoint.longitude,
    createdAt = createdAt
)
