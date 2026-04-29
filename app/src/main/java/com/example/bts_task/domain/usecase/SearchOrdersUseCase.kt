package com.example.bts_task.domain.usecase

import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class SearchOrdersUseCase(private val repo: OrderRepository) {
    operator fun invoke(query: String): Flow<List<Order>> = repo.searchByNumber(query)
}
