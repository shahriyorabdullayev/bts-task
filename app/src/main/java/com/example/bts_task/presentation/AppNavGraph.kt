package com.example.bts_task.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bts_task.R
import com.example.bts_task.presentation.new_order.NewOrderScreen
import com.example.bts_task.presentation.order_detail.OrderDetailScreen
import com.example.bts_task.presentation.orders.OrdersScreen
import com.example.bts_task.ui.theme.BrandGreen
import com.example.bts_task.ui.theme.SurfaceGrey
import com.example.bts_task.ui.theme.TextGrey
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

sealed interface Route {
    @Serializable data object Main : Route
    @Serializable data object Orders : Route
    @Serializable data object NewOrder : Route
    @Serializable data class OrderDetail(val id: Long) : Route
}

private data class TopLevelTab(
    val route: Route,
    val routeClass: KClass<*>,
    val label: String,
    val iconRes: Int
)

private val topLevelTabs = listOf(
    TopLevelTab(Route.Orders, Route.Orders::class, "Asosiy", R.drawable.ic_bts_home),
    TopLevelTab(Route.NewOrder, Route.NewOrder::class, "Yangi buyurtma", R.drawable.ic_bts_add)
)

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()
    NavHost(
        navController = rootNavController,
        startDestination = Route.Main,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable<Route.Main> {
            MainShell(
                onOrderClick = { id -> rootNavController.navigate(Route.OrderDetail(id)) }
            )
        }
        composable<Route.OrderDetail> { entry ->
            val args: Route.OrderDetail = entry.toRoute()
            OrderDetailScreen(
                orderId = args.id,
                onBack = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainShell(onOrderClick: (Long) -> Unit) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = SurfaceGrey,
        bottomBar = { BtsBottomNav(tabNavController, currentDestination) }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Route.Orders,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable<Route.Orders> {
                OrdersScreen(
                    contentPadding = padding,
                    onOrderClick = onOrderClick
                )
            }
            composable<Route.NewOrder> {
                NewOrderScreen(
                    contentPadding = padding,
                    onSubmitted = { tabNavController.navigateTopLevel(Route.Orders) }
                )
            }
        }
    }
}

@Composable
private fun BtsBottomNav(
    navController: NavController,
    currentDestination: NavDestination?
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        topLevelTabs.forEach { tab ->
            val selected = currentDestination?.hierarchyChain()
                ?.any { it.hasRoute(tab.routeClass) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateTopLevel(tab.route) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(tab.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandGreen,
                    selectedTextColor = BrandGreen,
                    unselectedIconColor = TextGrey,
                    unselectedTextColor = TextGrey,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

private fun NavController.navigateTopLevel(route: Route) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination.hierarchyChain(): Sequence<NavDestination> =
    generateSequence(this) { it.parent }
