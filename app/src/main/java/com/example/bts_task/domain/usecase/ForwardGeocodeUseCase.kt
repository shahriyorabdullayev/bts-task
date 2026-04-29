package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.repository.Geocoder

class ForwardGeocodeUseCase(private val geocoder: Geocoder) {
    suspend operator fun invoke(query: String): GeoPoint? = geocoder.forward(query)
}
