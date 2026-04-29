package com.example.bts_task.data.repository

import com.example.bts_task.data.db.OrderDao
import com.example.bts_task.data.mapper.toDomain
import com.example.bts_task.data.mapper.toEntity
import com.example.bts_task.domain.model.Order
import com.example.bts_task.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val dao: OrderDao
) : OrderRepository {

    override fun observeOrders(): Flow<List<Order>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun searchByNumber(query: String): Flow<List<Order>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Order? = dao.getById(id)?.toDomain()

    override suspend fun add(order: Order): Long = dao.insert(order.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
