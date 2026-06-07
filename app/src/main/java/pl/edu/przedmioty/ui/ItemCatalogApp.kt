package pl.edu.przedmioty.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import pl.edu.przedmioty.ui.screens.CatalogScreen
import pl.edu.przedmioty.ui.screens.ItemDetailScreen
import pl.edu.przedmioty.ui.screens.ItemFormScreen
import pl.edu.przedmioty.ui.screens.LoginScreen
import pl.edu.przedmioty.ui.screens.RegisterScreen

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CATALOG_GRAPH = "catalog_graph"
    const val CATALOG = "catalog"
    const val DETAIL = "detail/{itemId}"
    const val FORM = "form?itemId={itemId}"

    fun detail(itemId: String) = "detail/$itemId"
    fun form(itemId: String? = null) = if (itemId != null) "form?itemId=$itemId" else "form"
}

@Composable
fun ItemCatalogApp() {
    val navController = rememberNavController()
    val isSignedIn = remember { FirebaseAuth.getInstance().currentUser != null }

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) Routes.CATALOG_GRAPH else Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            LoginScreen(
                viewModel = loginViewModel,
                onLoggedIn = {
                    navController.navigate(Routes.CATALOG_GRAPH) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
            RegisterScreen(
                viewModel = registerViewModel,
                onRegistered = {
                    navController.navigate(Routes.CATALOG_GRAPH) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        navigation(startDestination = Routes.CATALOG, route = Routes.CATALOG_GRAPH) {
            composable(Routes.CATALOG) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CATALOG_GRAPH)
                }
                val catalogViewModel: CatalogViewModel = viewModel(graphEntry, factory = CatalogViewModel.Factory)
                CatalogScreen(
                    viewModel = catalogViewModel,
                    onOpenItem = { itemId -> navController.navigate(Routes.detail(itemId)) },
                    onAddItem = { navController.navigate(Routes.form()) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.CATALOG_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CATALOG_GRAPH)
                }
                val catalogViewModel: CatalogViewModel = viewModel(graphEntry, factory = CatalogViewModel.Factory)
                val detailViewModel: ItemDetailViewModel = viewModel(factory = ItemDetailViewModel.Factory)
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                ItemDetailScreen(
                    catalogViewModel = catalogViewModel,
                    detailViewModel = detailViewModel,
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.form(itemId)) },
                    onDeleted = {
                        navController.navigate(Routes.CATALOG) {
                            popUpTo(Routes.CATALOG) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Routes.FORM,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { backStackEntry ->
                val graphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CATALOG_GRAPH)
                }
                val catalogViewModel: CatalogViewModel = viewModel(graphEntry, factory = CatalogViewModel.Factory)
                val formViewModel: ItemFormViewModel = viewModel(factory = ItemFormViewModel.Factory)
                val itemId = backStackEntry.arguments?.getString("itemId")
                ItemFormScreen(
                    catalogViewModel = catalogViewModel,
                    formViewModel = formViewModel,
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}