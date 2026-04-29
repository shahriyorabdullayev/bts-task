package com.example.bts_task.presentation.order_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.LocationProvider
import com.example.bts_task.domain.usecase.GetOrderByIdUseCase
import com.example.bts_task.domain.util.Distance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrderDetailState(
    val order: Order? = null,
    val currentLocation: GeoPoint? = null,
    val distanceKm: Double? = null,
    val locationGranted: Boolean = false
)

class OrderDetailViewModel(
    private val orderId: Long,
    private val getOrderById: GetOrderByIdUseCase,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val order = getOrderById(orderId)
            _state.value = _state.value.copy(
                order = order,
                distanceKm = order?.let {
                    Distance.haversineKm(it.pickupPoint, it.destinationPoint)
                }
            )
        }
    }

    fun onLocationGranted() {
        _state.value = _state.value.copy(locationGranted = true)
        viewModelScope.launch {
            locationProvider.locationUpdates().collectLatest { point ->
                _state.value = _state.value.copy(currentLocation = point)
            }
        }
    }

    fun onLocationDenied() {
        _state.value = _state.value.copy(locationGranted = false)
    }
}
