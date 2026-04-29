package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrdersUseCase(private val repo: OrderRepository) {
    operator fun invoke(): Flow<List<Order>> = repo.observeOrders()
}
