package com.example.bts_task.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.LocationProvider
import com.example.bts_task.domain.repository.OrderRepository
import com.example.bts_task.domain.usecase.GetOrdersUseCase
import com.example.bts_task.domain.usecase.SearchOrdersUseCase
import com.example.bts_task.domain.util.AddressFormat
import com.example.bts_task.domain.util.Distance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderUi(
    val id: Long,
    val number: String,
    val pickupTitle: String,
    val pickupSubtitle: String,
    val destinationTitle: String,
    val destinationSubtitle: String,
    val distanceKm: Double?
)

data class OrdersState(
    val items: List<OrderUi> = emptyList(),
    val query: String = "",
    val hasLocationPermission: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModel(
    private val getOrders: GetOrdersUseCase,
    private val searchOrders: SearchOrdersUseCase,
    private val locationProvider: LocationProvider,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val location = MutableStateFlow<GeoPoint?>(null)
    private val permission = MutableStateFlow(false)

    private val ordersFlow = query
        .flatMapLatest { q ->
            if (q.isBlank()) getOrders() else searchOrders(q)
        }

    val state: StateFlow<OrdersState> =
        combine(ordersFlow, location, query, permission) { orders, loc, q, perm ->
            OrdersState(
                items = orders.toUi(loc),
                query = q,
                hasLocationPermission = perm
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OrdersState()
        )

    fun setQuery(q: String) { query.value = q }

    fun onLocationGranted() {
        permission.value = true
        viewModelScope.launch {
            location.value = locationProvider.getCurrentLocation()
        }
    }

    fun onLocationDenied() {
        permission.value = false
        location.value = null
    }

    fun deleteOrder(id: Long) {
        viewModelScope.launch {
            orderRepository.deleteById(id)
        }
    }

    private fun List<Order>.toUi(currentLocation: GeoPoint?): List<OrderUi> {
        val mapped = map { o ->
            val (pTitle, pSub) = AddressFormat.split(o.pickupAddress)
            val (dTitle, dSub) = AddressFormat.split(o.destinationAddress)
            OrderUi(
                id = o.id,
                number = o.number,
                pickupTitle = pTitle,
                pickupSubtitle = pSub,
                destinationTitle = dTitle,
                destinationSubtitle = dSub,
                distanceKm = Distance.haversineKm(o.pickupPoint, o.destinationPoint)
            )
        }
        return if (currentLocation != null) {
            mapped.sortedBy { o ->
                val order = this.find { it.id == o.id } ?: return@sortedBy Double.MAX_VALUE
                Distance.haversineKm(currentLocation, order.pickupPoint)
            }
        } else mapped
    }
}
