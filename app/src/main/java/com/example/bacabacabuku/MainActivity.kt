package com.example.bacabacabuku

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import java.security.MessageDigest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bacabacabuku.ui.screens.BookDetailScreen
import com.example.bacabacabuku.ui.screens.LibraryScreen
import com.example.bacabacabuku.ui.screens.SearchScreen
import com.example.bacabacabuku.ui.screens.LoginScreen
import com.example.bacabacabuku.ui.theme.BacaBacaBukuTheme
import com.example.bacabacabuku.ui.viewmodel.BookViewModel
import com.example.bacabacabuku.ui.viewmodel.BookViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log SHA-1 fingerprint for API key configuration
        try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA1")
                val digest = md.digest(signature.toByteArray())
                val hexString = digest.joinToString(":") { String.format("%02X", it) }
                Log.d("SIGNING_INFO", "SHA-1: $hexString")
            }
        } catch (e: Exception) {
            Log.e("SIGNING_INFO", "Error getting SHA-1", e)
        }

        enableEdgeToEdge()
        
        val bookApp = application as? BookApplication
        val repository = bookApp?.repository
        
        setContent {
            BacaBacaBukuTheme {
                if (repository == null) {
                    androidx.compose.material3.Text("Error: Could not initialize repository")
                } else {
                    val navController = rememberNavController()
                    val viewModel: BookViewModel = viewModel(factory = BookViewModelFactory(repository))

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("library") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("library") {
                            LibraryScreen(
                                viewModel = viewModel,
                                onBookClick = { bookId -> navController.navigate("detail/$bookId") },
                                onAddClick = { navController.navigate("search") },
                                onLogoutClick = {
                                    navController.navigate("login") {
                                        popUpTo("library") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("search") {
                            SearchScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "detail/{bookId}",
                            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
                            BookDetailScreen(
                                bookId = bookId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
