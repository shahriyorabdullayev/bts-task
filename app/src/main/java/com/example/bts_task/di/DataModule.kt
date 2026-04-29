package com.example.bts_task.di

import androidx.room.Room
import com.example.bts_task.data.db.AppDatabase
import com.example.bts_task.data.geocoder.YandexGeocoder
import com.example.bts_task.data.location.AndroidLocationProvider
import com.example.bts_task.data.repository.OrderRepositoryImpl
import com.example.bts_task.domain.repository.Geocoder
import com.example.bts_task.domain.repository.LocationProvider
import com.example.bts_task.domain.repository.OrderRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single<AppDatabase> {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "bts.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().orderDao() }

    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single<Geocoder> { YandexGeocoder() }
}
