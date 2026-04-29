package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.OrderRepository

class GetOrderByIdUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(id: Long): Order? = repo.getById(id)
}
