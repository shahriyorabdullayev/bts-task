package com.example.bts_task.domain.repository

import com.example.bts_task.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoPoint?
    fun locationUpdates(): Flow<GeoPoint>
}
