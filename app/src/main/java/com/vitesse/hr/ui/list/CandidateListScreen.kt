package com.vitesse.hr.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.vitesse.hr.data.local.Candidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(
    onCandidateClick: (Long) -> Unit = {},
    onAddClick: () -> Unit = {},
    // Hilt fournit automatiquement le ViewModel
    viewModel: CandidateListViewModel = hiltViewModel()
) {
    // observe les StateFlow du ViewModel, recompose à chaque changement
    val all by viewModel.allCandidates.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteCandidates.collectAsStateWithLifecycle()
    // rememberSaveable : ces deux states survivent à une rotation d'écran
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un candidat")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // search bar custom alignée au Figma : 56dp, rounded 28dp, icône à droite
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher un candidat") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // tabs Tous / Favoris, style Material 3 primary (indicateur sous le tab actif)
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tous") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Favoris") }
                )
            }

            // on choisit la liste selon le tab actif, puis on filtre par recherche
            // tout est fait en local (en mémoire), comme demandé dans les specs
            val source = if (selectedTab == 0) all else favorites
            val visible = source?.filter {
                query.isBlank() ||
                    it.firstName.contains(query, ignoreCase = true) ||
                    it.lastName.contains(query, ignoreCase = true)
            }

            // 3 états d'affichage : chargement, vide, ou liste
            when {
                source == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                visible.isNullOrEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        text = "Aucun candidat",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // LazyColumn = équivalent moderne de RecyclerView, ne crée que les items visibles
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible, key = { it.id }) { candidate ->
                        CandidateRow(candidate, onClick = { onCandidateClick(candidate.id) })
                    }
                }
            }
        }
    }
}

// item 88dp : photo 56dp carrée + nom + notes 2 lignes max
@Composable
private fun CandidateRow(candidate: Candidate, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (candidate.photoUri != null) {
            // Coil charge l'image depuis l'Uri (photo prise via PhotoPicker)
            AsyncImage(
                model = candidate.photoUri,
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        } else {
            // fallback si pas de photo : icône Person sur fond gris
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = candidate.fullName,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = candidate.notes,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
