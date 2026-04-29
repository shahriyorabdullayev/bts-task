package com.example.bts_task.presentation.new_order

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bts_task.domain.model.GeoPoint
import com.example.bts_task.presentation.common.BitmapHelper
import com.example.bts_task.presentation.common.YandexMapView
import com.example.bts_task.ui.theme.BrandGreen
import com.example.bts_task.ui.theme.TextDark
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import org.koin.androidx.compose.koinViewModel

private class Holder<T> { var value: T? = null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    onSubmitted: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: NewOrderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            if (ev is NewOrderEvent.Submitted) onSubmitted()
        }
    }

    val mapHolder = remember { Holder<MapView>() }
    val pickupMarker = remember { Holder<PlacemarkMapObject>() }
    val destMarker = remember { Holder<PlacemarkMapObject>() }
    val routeLine = remember { Holder<PolylineMapObject>() }
    val drivingSession = remember { Holder<DrivingSession>() }

    val selectingPickup = remember { mutableStateOf<Boolean?>(null) }

    val tapListener = remember {
        object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                val sel = selectingPickup.value ?: return
                viewModel.onMarkerDragged(sel, GeoPoint(point.latitude, point.longitude))
                selectingPickup.value = null
            }
            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yangi buyurtma yaratish",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                YandexMapView(
                    modifier = Modifier.fillMaxSize(),
                    onReady = { mv ->
                        mapHolder.value = mv
                        mv.mapWindow.map.move(
                            CameraPosition(Point(41.311081, 69.240562), 11f, 0f, 0f)
                        )
                        mv.mapWindow.map.addInputListener(tapListener)
                    }
                )

                LaunchedEffect(state.pickupPoint, mapHolder.value) {
                    val mv = mapHolder.value ?: return@LaunchedEffect
                    val p = state.pickupPoint ?: return@LaunchedEffect
                    val pt = Point(p.latitude, p.longitude)
                    val existing = pickupMarker.value
                    if (existing == null) {
                        val placemark = mv.mapWindow.map.mapObjects.addPlacemark(
                            pt,
                            ImageProvider.fromBitmap(BitmapHelper.dot(ctx, 0xFF4CAF50.toInt()))
                        )
                        placemark.isDraggable = true
                        placemark.setDragListener(object : com.yandex.mapkit.map.MapObjectDragListener {
                            override fun onMapObjectDragStart(p0: com.yandex.mapkit.map.MapObject) {}
                            override fun onMapObjectDrag(p0: com.yandex.mapkit.map.MapObject, point: Point) {}
                            override fun onMapObjectDragEnd(p0: com.yandex.mapkit.map.MapObject) {
                                val newGeom = (p0 as? PlacemarkMapObject)?.geometry ?: return
                                viewModel.onMarkerDragged(true, GeoPoint(newGeom.latitude, newGeom.longitude))
                            }
                        })
                        pickupMarker.value = placemark
                    } else {
                        existing.geometry = pt
                    }
                }

                LaunchedEffect(state.destinationPoint, mapHolder.value) {
                    val mv = mapHolder.value ?: return@LaunchedEffect
                    val p = state.destinationPoint ?: return@LaunchedEffect
                    val pt = Point(p.latitude, p.longitude)
                    val existing = destMarker.value
                    if (existing == null) {
                        val placemark = mv.mapWindow.map.mapObjects.addPlacemark(
                            pt,
                            ImageProvider.fromBitmap(BitmapHelper.dot(ctx, 0xFFC62828.toInt()))
                        )
                        placemark.isDraggable = true
                        placemark.setDragListener(object : com.yandex.mapkit.map.MapObjectDragListener {
                            override fun onMapObjectDragStart(p0: com.yandex.mapkit.map.MapObject) {}
                            override fun onMapObjectDrag(p0: com.yandex.mapkit.map.MapObject, point: Point) {}
                            override fun onMapObjectDragEnd(p0: com.yandex.mapkit.map.MapObject) {
                                val newGeom = (p0 as? PlacemarkMapObject)?.geometry ?: return
                                viewModel.onMarkerDragged(false, GeoPoint(newGeom.latitude, newGeom.longitude))
                            }
                        })
                        destMarker.value = placemark
                    } else {
                        existing.geometry = pt
                    }
                }

                LaunchedEffect(state.pickupPoint, state.destinationPoint, mapHolder.value) {
                    val mv = mapHolder.value ?: return@LaunchedEffect
                    val pickup = state.pickupPoint ?: return@LaunchedEffect
                    val dest = state.destinationPoint ?: return@LaunchedEffect
                    val start = Point(pickup.latitude, pickup.longitude)
                    val end = Point(dest.latitude, dest.longitude)
                    val mapObjects = mv.mapWindow.map.mapObjects

                    routeLine.value?.let { mapObjects.remove(it) }
                    routeLine.value = null
                    drivingSession.value?.cancel()

                    val drivingRouter = DirectionsFactory.getInstance()
                        .createDrivingRouter(DrivingRouterType.COMBINED)
                    val requestPoints = listOf(
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

                            val cam = mv.mapWindow.map.cameraPosition(
                                Geometry.fromPolyline(route.geometry)
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AddressField(
                    label = "Qayerdan",
                    value = state.pickupQuery,
                    loading = state.pickupLoading,
                    selected = selectingPickup.value == true,
                    onClick = { selectingPickup.value = true }
                )
                AddressField(
                    label = "Qayerga",
                    value = state.destinationQuery,
                    loading = state.destinationLoading,
                    selected = selectingPickup.value == false,
                    onClick = { selectingPickup.value = false }
                )
                val canSubmit = state.pickupPoint != null &&
                        state.destinationPoint != null &&
                        !state.submitting
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = canSubmit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBDBDBD),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        "Tasdiqlash",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(56.dp))

            }
        }
    }

    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::consumeError,
            confirmButton = {
                TextButton(onClick = viewModel::consumeError) { Text("OK") }
            },
            title = { Text("Xato") },
            text = { Text(msg) }
        )
    }

    if (state.needsLocationSettings) {
        AlertDialog(
            onDismissRequest = viewModel::consumeNeedsLocationSettings,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeNeedsLocationSettings()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", ctx.packageName, null)
                    }
                    ctx.startActivity(intent)
                }) { Text("Sozlamalar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::consumeNeedsLocationSettings) { Text("Bekor qilish") }
            },
            title = { Text("Joylashuv ruxsati") },
            text = { Text("Joylashuvdan foydalanish uchun sozlamalardan ruxsat bering.") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressField(
    label: String,
    value: String,
    loading: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black
        )
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            singleLine = true,
            placeholder = {
                Text(
                    text = if (selected) "Xaritadan tanlang" else "—",
                    color = Color.Gray,
                    style = TextStyle(
                        fontSize = 12.sp
                    )
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (selected) BrandGreen else Color(0xFFBDBDBD),
                disabledTextColor = TextDark,
                disabledPlaceholderColor = Color.Gray,
                disabledTrailingIconColor = BrandGreen,
                disabledContainerColor = Color.Transparent
            ),
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = BrandGreen
                    )
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp
            )
        )
    }
}
