package pl.edu.przedmioty.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import pl.edu.przedmioty.ui.screens.CatalogScreen
import pl.edu.przedmioty.ui.screens.LoginScreen
import pl.edu.przedmioty.ui.screens.RegisterScreen

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CATALOG = "catalog"
}

@Composable
fun ItemCatalogApp() {
    val navController = rememberNavController()
    val isSignedIn = remember { FirebaseAuth.getInstance().currentUser != null }

    NavHost(navController = navController, startDestination = if (isSignedIn) Routes.CATALOG else Routes.LOGIN) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            LoginScreen(
                viewModel = loginViewModel,
                onLoggedIn = {
                    navController.navigate(Routes.CATALOG) {
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
                    navController.navigate(Routes.CATALOG) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CATALOG) {
            val catalogViewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory)
            CatalogScreen(
                viewModel = catalogViewModel,
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