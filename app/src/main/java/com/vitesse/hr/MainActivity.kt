package com.vitesse.hr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vitesse.hr.ui.list.CandidateListScreen
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
                    CandidateListScreen()
                }
            }
        }
    }
}
