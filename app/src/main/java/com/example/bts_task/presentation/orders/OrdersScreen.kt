package com.example.bts_task.presentation.orders

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bts_task.R
import com.example.bts_task.ui.theme.BrandGreen
import com.example.bts_task.ui.theme.DividerGrey
import com.example.bts_task.ui.theme.IconGrey
import com.example.bts_task.ui.theme.OutlineGrey
import com.example.bts_task.ui.theme.SurfaceGrey
import com.example.bts_task.ui.theme.TextDark
import com.example.bts_task.ui.theme.TextGrey
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: OrdersViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchOpen by remember { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }

    LaunchedEffect(searchOpen) {
        if (searchOpen) searchFocus.requestFocus()
    }

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

    Scaffold(
        containerColor = SurfaceGrey,
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        TextField(
                            value = state.query,
                            onValueChange = viewModel::setQuery,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocus),
                            placeholder = {
                                Text(
                                    "Buyurtma raqami",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGrey
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = BrandGreen
                            )
                        )
                    } else {
                        Text(
                            "Buyurtmalar",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchOpen = !searchOpen
                        if (!searchOpen) viewModel.setQuery("")
                    }) {
                        if (searchOpen) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Yopish",
                                tint = IconGrey
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_bts_search),
                                contentDescription = "Qidirish",
                            )
                        }
                    }
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
            val items = state.items
            when {
                items == null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp + contentPadding.calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(5) { ShimmerCard() }
                    }
                }
                items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inbox,
                                contentDescription = null,
                                tint = IconGrey,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Buyurtmalar yo'q",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextGrey
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp + contentPadding.calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items, key = { it.id }) { ui ->
                            OrderCard(
                                ui = ui,
                                onClick = { onOrderClick(ui.id) },
                                onDelete = { viewModel.deleteOrder(ui.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing)
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE0E0E0),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        ),
        start = Offset(translate - 300f, 0f),
        end = Offset(translate, 0f)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun OrderCard(ui: OrderUi, onClick: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ui.distanceKm?.let { "%.0f KM".format(it) } ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "No. ${ui.number}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = IconGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        containerColor = Color.White
                    ) {
                        DropdownMenuItem(
                            text = { Text("O'chirish", color = TextDark) },
                            onClick = {
                                menuOpen = false
                                confirmOpen = true
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGrey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            AddressRow(
                street = ui.pickupTitle,
                city = ui.pickupSubtitle,
                isPickup = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddressRow(
                street = ui.destinationTitle,
                city = ui.destinationSubtitle,
                isPickup = false
            )
        }
    }

    if (confirmOpen) {
        AlertDialog(
            onDismissRequest = { confirmOpen = false },
            title = { Text("Buyurtmani o'chirish", color = TextDark) },
            text = { Text("Ushbu buyurtmani o'chirishni tasdiqlaysizmi?", color = TextGrey) },
            confirmButton = {
                TextButton(onClick = {
                    confirmOpen = false
                    onDelete()
                }) { Text("O'chirish", color = BrandGreen) }
            },
            dismissButton = {
                TextButton(onClick = { confirmOpen = false }) {
                    Text("Bekor qilish", color = TextGrey)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun AddressRow(street: String, city: String, isPickup: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPickup) Image(
                painter = painterResource(R.drawable.ic_maps_a),
                contentDescription = null,
            ) else Image(
                painter = painterResource(R.drawable.ic_maps_b),
                contentDescription = null,
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
