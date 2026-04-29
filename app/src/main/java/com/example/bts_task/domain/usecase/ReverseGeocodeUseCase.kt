package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.repository.Geocoder

class ReverseGeocodeUseCase(private val geocoder: Geocoder) {
    suspend operator fun invoke(point: GeoPoint): String? = geocoder.reverse(point)
}
