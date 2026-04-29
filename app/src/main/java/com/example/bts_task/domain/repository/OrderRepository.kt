package com.example.bts_task.domain.repository

import com.example.bts_task.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    fun searchByNumber(query: String): Flow<List<Order>>
    suspend fun getById(id: Long): Order?
    suspend fun add(order: Order): Long
    suspend fun deleteById(id: Long)
}
