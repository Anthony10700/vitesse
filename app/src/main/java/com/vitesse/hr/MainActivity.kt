package com.vitesse.hr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitesse.hr.ui.detail.CandidateDetailScreen
import com.vitesse.hr.ui.edit.AddEditCandidateScreen
import com.vitesse.hr.ui.list.CandidateListScreen
import com.vitesse.hr.ui.navigation.VitesseDestinations
import com.vitesse.hr.ui.theme.VitesseTheme
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint = permet l'injection Hilt dans cette Activity et ses ViewModels
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // contenu affiché sous la statusbar et la navbar
        enableEdgeToEdge()
        setContent {
            VitesseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VitesseNavHost()
                }
            }
        }
    }
}

// NavHost = conteneur qui affiche le bon écran selon l'état du NavController.
// Chaque composable() = une destination. navigate() empile, popBackStack() dépile.
@androidx.compose.runtime.Composable
private fun VitesseNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = VitesseDestinations.LIST
    ) {
        composable(VitesseDestinations.LIST) {
            CandidateListScreen(
                onCandidateClick = { id ->
                    navController.navigate(VitesseDestinations.detail(id))
                },
                onAddClick = {
                    navController.navigate(VitesseDestinations.ADD)
                }
            )
        }

        composable(VitesseDestinations.ADD) {
            AddEditCandidateScreen(
                candidateId = null,
                onBack = { navController.popBackStack() }
            )
        }

        // route paramétrée : récupère l'id de candidat depuis les arguments
        composable(
            route = VitesseDestinations.EDIT,
            arguments = listOf(navArgument(VitesseDestinations.ARG_CANDIDATE_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(VitesseDestinations.ARG_CANDIDATE_ID)
            AddEditCandidateScreen(
                candidateId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = VitesseDestinations.DETAIL,
            arguments = listOf(navArgument(VitesseDestinations.ARG_CANDIDATE_ID) { type = NavType.LongType })
        ) {
            // le candidateId est récupéré par le ViewModel via SavedStateHandle
            CandidateDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(VitesseDestinations.edit(id))
                }
            )
        }
    }
}
