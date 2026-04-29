package com.example.bts_task.presentation.order_detail

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bts_task.presentation.common.BitmapHelper
import com.example.bts_task.presentation.common.YandexMapView
import com.example.bts_task.ui.theme.BrandGreen
import com.example.bts_task.ui.theme.OutlineGrey
import com.example.bts_task.ui.theme.TextDark
import com.example.bts_task.ui.theme.TextGrey
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.Animation
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private class Holder<T> { var value: T? = null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = koinViewModel(parameters = { parametersOf(orderId) })
) {
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onLocationGranted() else viewModel.onLocationDenied()
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val mapHolder = remember { Holder<MapView>() }
    val userMarker = remember { Holder<PlacemarkMapObject>() }
    val routeLine = remember { Holder<PolylineMapObject>() }
    val drivingSession = remember { Holder<DrivingSession>() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = TextDark
                        )
                    }
                },
                title = {
                    Text(
                        text = state.order?.let { "Order No. ${it.number}" } ?: "Buyurtma",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            YandexMapView(
                modifier = Modifier.fillMaxSize(),
                onReady = { mv -> mapHolder.value = mv }
            )

            LaunchedEffect(state.order, mapHolder.value) {
                val mv = mapHolder.value ?: return@LaunchedEffect
                val order = state.order ?: return@LaunchedEffect

                val start = Point(order.pickupPoint.latitude, order.pickupPoint.longitude)
                val end = Point(order.destinationPoint.latitude, order.destinationPoint.longitude)

                mv.mapWindow.map.move(CameraPosition(start, 12f, 0f, 0f))

                val mapObjects = mv.mapWindow.map.mapObjects
                mapObjects.addPlacemark(
                    start,
                    ImageProvider.fromBitmap(BitmapHelper.dot(ctx, 0xFF66BB6A.toInt()))
                )
                mapObjects.addPlacemark(
                    end,
                    ImageProvider.fromBitmap(BitmapHelper.dot(ctx, 0xFFC62828.toInt()))
                )

                val drivingRouter = DirectionsFactory.getInstance()
                    .createDrivingRouter(DrivingRouterType.COMBINED)
                val requestPoints: List<RequestPoint> = listOf(
                    RequestPoint(start, RequestPointType.WAYPOINT, null, null),
                    RequestPoint(end, RequestPointType.WAYPOINT, null, null)
                )
                val listener = object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                        val route = routes.firstOrNull() ?: return
                        routeLine.value?.let { mapObjects.remove(it) }
                        val poly = mapObjects.addPolyline(route.geometry)
                        poly.setStrokeColor(0xFF66BB6A.toInt())
                        poly.strokeWidth = 5f
                        routeLine.value = poly

                        val w = mv.mapWindow.width().toFloat()
                        val h = mv.mapWindow.height().toFloat()
                        val sheetPx = 260f * ctx.resources.displayMetrics.density
                        val focusRect = ScreenRect(
                            ScreenPoint(0f, 0f),
                            ScreenPoint(w, (h - sheetPx).coerceAtLeast(h * 0.4f))
                        )
                        val cam = mv.mapWindow.map.cameraPosition(
                            Geometry.fromPolyline(route.geometry),
                            0f,
                            0f,
                            focusRect
                        )
                        mv.mapWindow.map.move(
                            CameraPosition(cam.target, cam.zoom - 0.5f, cam.azimuth, cam.tilt),
                            Animation(Animation.Type.SMOOTH, 0.5f),
                            null
                        )
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        val poly = mapObjects.addPolyline(Polyline(listOf(start, end)))
                        poly.setStrokeColor(0xFF66BB6A.toInt())
                        poly.strokeWidth = 5f
                        routeLine.value = poly
                    }
                }
                drivingSession.value = drivingRouter.requestRoutes(
                    requestPoints,
                    DrivingOptions().apply { routesCount = 1 },
                    VehicleOptions(),
                    listener
                )
            }

            LaunchedEffect(state.currentLocation, mapHolder.value) {
                val mv = mapHolder.value ?: return@LaunchedEffect
                val loc = state.currentLocation ?: return@LaunchedEffect
                val pt = Point(loc.latitude, loc.longitude)
                val existing = userMarker.value
                if (existing == null) {
                    userMarker.value = mv.mapWindow.map.mapObjects.addPlacemark(
                        pt,
                        ImageProvider.fromBitmap(BitmapHelper.dot(ctx, 0xFF1976D2.toInt()))
                    )
                } else {
                    existing.geometry = pt
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 4.dp)
                                .background(OutlineGrey, RoundedCornerShape(2.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    state.order?.let { order ->
                        val (pTitle, pSub) = com.example.bts_task.domain.util.AddressFormat.split(order.pickupAddress)
                        val (dTitle, dSub) = com.example.bts_task.domain.util.AddressFormat.split(order.destinationAddress)
                        Text(
                            text = state.distanceKm?.let { "%.0f KM".format(it) }
                                ?: "Masofa noma'lum",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        AddrLine(
                            street = pTitle,
                            city = pSub,
                            isPickup = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AddrLine(
                            street = dTitle,
                            city = dSub,
                            isPickup = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddrLine(street: String, city: String, isPickup: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPickup) PickupDot() else Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = street,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark
            )
            Text(
                text = city,
                style = MaterialTheme.typography.bodySmall,
                color = TextGrey
            )
        }
    }
}

@Composable
private fun PickupDot() {
    Surface(
        shape = CircleShape,
        color = Color.White,
        modifier = Modifier
            .size(16.dp)
            .border(1.dp, OutlineGrey, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(BrandGreen, CircleShape)
            )
        }
    }
}
