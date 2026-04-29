package com.example.bts_task.data.geocoder

import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.domain.repository.Geocoder
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class YandexGeocoder : Geocoder {

    private val manager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    override suspend fun reverse(point: GeoPoint): String? =
        suspendCancellableCoroutine { cont ->
            val session = manager.submit(
                Point(point.latitude, point.longitude),
                /* zoom */ 16,
                SearchOptions(),
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val obj = response.collection.children.firstOrNull()?.obj
                        val address = obj?.metadataContainer
                            ?.getItem(ToponymObjectMetadata::class.java)
                            ?.address
                        val name = address?.let { formatAddress(it) }
                            ?: address?.formattedAddress
                            ?: obj?.name
                        if (cont.isActive) cont.resume(name)
                    }
                    override fun onSearchError(error: Error) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            )
            cont.invokeOnCancellation { session.cancel() }
        }

    private fun formatAddress(addr: Address): String? {
        val components = addr.components
        fun find(kind: Address.Component.Kind): String? =
            components.firstOrNull { kind in it.kinds }?.name
        val street = find(Address.Component.Kind.STREET)
        val house = find(Address.Component.Kind.HOUSE)
        val locality = find(Address.Component.Kind.LOCALITY)
            ?: find(Address.Component.Kind.PROVINCE)
        val district = find(Address.Component.Kind.DISTRICT)
        val title = listOfNotNull(street, house).joinToString(" ").ifBlank {
            find(Address.Component.Kind.HOUSE)
                ?: find(Address.Component.Kind.STREET)
                ?: addr.formattedAddress?.split(",")?.firstOrNull()?.trim()
        }
        val sub = listOfNotNull(locality, district).joinToString(", ")
        if (title.isNullOrBlank()) return null
        return if (sub.isBlank()) title else "$title, $sub"
    }

    override suspend fun forward(query: String): GeoPoint? =
        suspendCancellableCoroutine { cont ->
            val geometry = com.yandex.mapkit.geometry.Geometry.fromPoint(Point(41.311081, 69.240562))
            val session = manager.submit(
                query,
                geometry,
                SearchOptions(),
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val pt = response.collection.children
                            .firstOrNull()
                            ?.obj
                            ?.geometry
                            ?.firstOrNull()
                            ?.point
                        val gp = pt?.let { GeoPoint(it.latitude, it.longitude) }
                        if (cont.isActive) cont.resume(gp)
                    }
                    override fun onSearchError(error: Error) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            )
            cont.invokeOnCancellation { session.cancel() }
        }
}
