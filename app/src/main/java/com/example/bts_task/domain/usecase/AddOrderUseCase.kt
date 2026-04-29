package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.OrderRepository

class AddOrderUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(order: Order): Long = repo.add(order)
}
