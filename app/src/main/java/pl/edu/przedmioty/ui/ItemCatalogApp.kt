package pl.edu.przedmioty.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.przedmioty.ui.screens.CatalogScreen
import pl.edu.przedmioty.ui.screens.LoginScreen
import pl.edu.przedmioty.ui.screens.RegisterScreen

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CATALOG = "catalog"
}

@Composable
fun ItemCatalogApp(viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.isSignedIn()) Routes.CATALOG else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(Routes.CATALOG) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = viewModel,
                onRegistered = {
                    navController.navigate(Routes.CATALOG) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CATALOG) {
            CatalogScreen(
                viewModel = viewModel,
                onOpenItem = { },
                onAddItem = { },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.CATALOG) { inclusive = true }
                    }
                },
            )
        }
    }
}