package com.example.bts_task.di

import com.example.bts_task.presentation.new_order.NewOrderViewModel
import com.example.bts_task.presentation.order_detail.OrderDetailViewModel
import com.example.bts_task.presentation.orders.OrdersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { OrdersViewModel(get(), get(), get(), get()) }
    viewModel { (orderId: Long) -> OrderDetailViewModel(orderId, get(), get()) }
    viewModel { NewOrderViewModel(get(), get(), get(), get()) }
}
