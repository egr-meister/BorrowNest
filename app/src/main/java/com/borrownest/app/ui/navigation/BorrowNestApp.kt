package com.borrownest.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.borrownest.app.ui.screens.AddEditItemScreen
import com.borrownest.app.ui.screens.ArchiveScreen
import com.borrownest.app.ui.screens.BoardScreen
import com.borrownest.app.ui.screens.HistoryScreen
import com.borrownest.app.ui.screens.ItemDetailScreen
import com.borrownest.app.ui.screens.OnboardingScreen
import com.borrownest.app.ui.screens.PersonDetailScreen
import com.borrownest.app.ui.screens.PersonSummaryScreen
import com.borrownest.app.ui.screens.ReturnedItemsScreen
import com.borrownest.app.ui.screens.SettingsScreen
import com.borrownest.app.ui.screens.StatisticsScreen
import com.borrownest.app.ui.viewmodel.BorrowViewModel

@Composable
fun BorrowNestApp() {
    val viewModel: BorrowViewModel = viewModel(factory = BorrowViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    if (!uiState.loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (uiState.settings.onboardingCompleted) Routes.BOARD else Routes.ONBOARDING

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Routes.BOARD, Routes.HISTORY, Routes.ARCHIVE, Routes.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any {
                            it.route == tab.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeInSpec() },
            exitTransition = { fadeOutSpec() },
            popEnterTransition = { fadeInSpec() },
            popExitTransition = { fadeOutSpec() }
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onAddFirstItem = {
                        viewModel.completeOnboarding()
                        navController.navigate(Routes.ADD)
                    },
                    onExplore = {
                        viewModel.completeOnboarding()
                        navController.navigate(Routes.BOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.BOARD) {
                BoardScreen(
                    viewModel = viewModel,
                    onAddItem = { navController.navigate(Routes.ADD) },
                    onItemClick = { navController.navigate(Routes.detail(it)) },
                    onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                    onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                    onOpenPersons = { navController.navigate(Routes.PERSONS) },
                    onOpenReturned = { navController.navigate(Routes.RETURNED) }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(viewModel = viewModel)
            }

            composable(Routes.ARCHIVE) {
                ArchiveScreen(
                    viewModel = viewModel,
                    onItemClick = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onOpenStatistics = { navController.navigate(Routes.STATISTICS) }
                )
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(Routes.RETURNED) {
                ReturnedItemsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onItemClick = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.PERSONS) {
                PersonSummaryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPersonClick = { name ->
                        navController.navigate("person/${java.net.URLEncoder.encode(name, "UTF-8")}")
                    }
                )
            }

            composable(Routes.PERSON_DETAIL) { entry ->
                val raw = entry.arguments?.getString("personName").orEmpty()
                val name = try {
                    java.net.URLDecoder.decode(raw, "UTF-8")
                } catch (e: Exception) {
                    raw
                }
                PersonDetailScreen(
                    viewModel = viewModel,
                    personName = name,
                    onBack = { navController.popBackStack() },
                    onItemClick = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.ADD) {
                AddEditItemScreen(
                    viewModel = viewModel,
                    itemId = null,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Routes.EDIT) { entry ->
                val itemId = entry.arguments?.getString("itemId")
                AddEditItemScreen(
                    viewModel = viewModel,
                    itemId = itemId,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Routes.DETAIL) { entry ->
                val itemId = entry.arguments?.getString("itemId")
                ItemDetailScreen(
                    viewModel = viewModel,
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(it)) }
                )
            }
        }
    }
}

private fun fadeInSpec() =
    androidx.compose.animation.fadeIn(animationSpec = tween(160))

private fun fadeOutSpec() =
    androidx.compose.animation.fadeOut(animationSpec = tween(160))
