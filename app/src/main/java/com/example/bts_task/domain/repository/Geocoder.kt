package com.example.bts_task.domain.repository

import com.example.bts_task.domain.model.GeoPoint

interface Geocoder {
    suspend fun reverse(point: GeoPoint): String?
    suspend fun forward(query: String): GeoPoint?
}
