package com.example.bts_task.di

import com.example.bts_task.domain.usecase.AddOrderUseCase
import com.example.bts_task.domain.usecase.ForwardGeocodeUseCase
import com.example.bts_task.domain.usecase.GetOrderByIdUseCase
import com.example.bts_task.domain.usecase.GetOrdersUseCase
import com.example.bts_task.domain.usecase.ReverseGeocodeUseCase
import com.example.bts_task.domain.usecase.SearchOrdersUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetOrdersUseCase(get()) }
    factory { SearchOrdersUseCase(get()) }
    factory { GetOrderByIdUseCase(get()) }
    factory { AddOrderUseCase(get()) }
    factory { ReverseGeocodeUseCase(get()) }
    factory { ForwardGeocodeUseCase(get()) }
}
