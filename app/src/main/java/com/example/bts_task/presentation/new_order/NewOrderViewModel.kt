package com.example.bts_task.presentation.new_order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.LocationProvider
import com.example.bts_task.domain.usecase.AddOrderUseCase
import com.example.bts_task.domain.usecase.ForwardGeocodeUseCase
import com.example.bts_task.domain.usecase.ReverseGeocodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class NewOrderState(
    val pickupQuery: String = "",
    val destinationQuery: String = "",
    val pickupPoint: GeoPoint? = null,
    val destinationPoint: GeoPoint? = null,
    val pickupLoading: Boolean = false,
    val destinationLoading: Boolean = false,
    val error: String? = null,
    val needsLocationSettings: Boolean = false,
    val submitting: Boolean = false
)

sealed class NewOrderEvent {
    data object Submitted : NewOrderEvent()
    data class MarkerMoved(val isPickup: Boolean, val point: GeoPoint) : NewOrderEvent()
}

class NewOrderViewModel(
    private val locationProvider: LocationProvider,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val forwardGeocode: ForwardGeocodeUseCase,
    private val addOrder: AddOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewOrderState())
    val state: StateFlow<NewOrderState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NewOrderEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<NewOrderEvent> = _events.asSharedFlow()

    private var pickupTextJob: Job? = null
    private var destTextJob: Job? = null

    fun onLocationGranted() {
        viewModelScope.launch {
            _state.value = _state.value.copy(pickupLoading = true)
            val point = locationProvider.getCurrentLocation()
            if (point != null) {
                val address = reverseGeocode(point) ?: defaultLabel(point)
                _state.value = _state.value.copy(
                    pickupPoint = point,
                    pickupQuery = address,
                    pickupLoading = false
                )
                _events.tryEmit(NewOrderEvent.MarkerMoved(isPickup = true, point = point))
            } else {
                _state.value = _state.value.copy(pickupLoading = false)
            }
        }
    }

    fun onLocationDenied() {
        _state.value = _state.value.copy(needsLocationSettings = true)
    }

    fun consumeNeedsLocationSettings() {
        _state.value = _state.value.copy(needsLocationSettings = false)
    }

    fun onPickupTextChanged(text: String) {
        _state.value = _state.value.copy(pickupQuery = text)
        pickupTextJob?.cancel()
        pickupTextJob = viewModelScope.launch {
            delay(700)
            if (text.isBlank()) return@launch
            val point = forwardGeocode(text) ?: return@launch
            _state.value = _state.value.copy(pickupPoint = point)
            _events.tryEmit(NewOrderEvent.MarkerMoved(isPickup = true, point = point))
        }
    }

    fun onDestinationTextChanged(text: String) {
        _state.value = _state.value.copy(destinationQuery = text)
        destTextJob?.cancel()
        destTextJob = viewModelScope.launch {
            delay(700)
            if (text.isBlank()) return@launch
            val point = forwardGeocode(text) ?: return@launch
            _state.value = _state.value.copy(destinationPoint = point)
            _events.tryEmit(NewOrderEvent.MarkerMoved(isPickup = false, point = point))
        }
    }

    fun onMarkerDragged(isPickup: Boolean, point: GeoPoint) {
        viewModelScope.launch {
            if (isPickup) {
                _state.value = _state.value.copy(pickupPoint = point, pickupLoading = true)
                val addr = reverseGeocode(point) ?: defaultLabel(point)
                _state.value = _state.value.copy(pickupQuery = addr, pickupLoading = false)
            } else {
                _state.value = _state.value.copy(destinationPoint = point, destinationLoading = true)
                val addr = reverseGeocode(point) ?: defaultLabel(point)
                _state.value = _state.value.copy(destinationQuery = addr, destinationLoading = false)
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (s.pickupQuery.isBlank() || s.destinationQuery.isBlank()
            || s.pickupPoint == null || s.destinationPoint == null
        ) {
            _state.value = s.copy(error = "Ikkala maydon ham to'ldirilishi shart")
            return
        }
        _state.value = s.copy(error = null, submitting = true)
        viewModelScope.launch {
            val order = Order(
                id = 0,
                number = "F" + Random.nextInt(10_000, 99_999),
                pickupAddress = s.pickupQuery,
                pickupPoint = s.pickupPoint,
                destinationAddress = s.destinationQuery,
                destinationPoint = s.destinationPoint,
                createdAt = System.currentTimeMillis()
            )
            addOrder(order)
            _state.value = _state.value.copy(submitting = false)
            _events.tryEmit(NewOrderEvent.Submitted)
        }
    }

    fun consumeError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun defaultLabel(p: GeoPoint): String =
        "%.5f, %.5f".format(p.latitude, p.longitude)
}
