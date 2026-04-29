package com.example.bts_task.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE number LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OrderEntity): Long

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun count(): Int

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
