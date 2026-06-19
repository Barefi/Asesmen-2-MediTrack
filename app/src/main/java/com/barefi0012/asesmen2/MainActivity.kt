package com.barefi0012.asesmen2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barefi0012.asesmen2.data.UserPreferences
import com.barefi0012.asesmen2.model.UserProfile
import com.barefi0012.asesmen2.ui.theme.Asesmen2Theme

class MainActivity : ComponentActivity() {


    private lateinit var viewModel: MedicationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MedicationViewModel::class.java]

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            val userPreferences = remember { UserPreferences(applicationContext) }
            val userProfile by userPreferences.userProfile.collectAsState(UserProfile())
            val activeOwnerEmail = userProfile.email.ifBlank { MedicationViewModel.GUEST_OWNER }
            var showProfileDialog by remember { mutableStateOf(false) }

            Asesmen2Theme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                userProfile = userProfile,
                                onNavigateToAdd = { navController.navigate("form/-1") },
                                onNavigateToEdit = { id -> navController.navigate("form/$id") },
                                onNavigateToBin = { navController.navigate("bin") },
                                onShowProfile = { showProfileDialog = true }
                            )
                        }
                        composable(
                            route = "form/{medicationId}",
                            arguments = listOf(navArgument("medicationId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("medicationId") ?: -1
                            FormScreen(
                                viewModel = viewModel,
                                medicationId = if (id == -1) null else id,
                                ownerEmail = activeOwnerEmail,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("bin") {
                            RecycleBinScreen(
                                viewModel = viewModel,
                                ownerEmail = activeOwnerEmail,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                    if (showProfileDialog) {
                        ProfileDialog(
                            userProfile = userProfile,
                            userPreferences = userPreferences,
                            onDismissRequest = { showProfileDialog = false }
                        )
                    }
                }
            }
        }
    }
}
