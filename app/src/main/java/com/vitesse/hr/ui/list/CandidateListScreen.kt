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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.vitesse.hr.R
import com.vitesse.hr.data.local.Candidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(
    onCandidateClick: (Long) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.list_fab_add_description))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // search bar custom alignée au Figma : 56dp, rounded 28dp, icône à droite
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.list_search_placeholder)) },
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
                    text = { Text(stringResource(R.string.list_tab_all)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.list_tab_favorites)) }
                )
            }

            // chaque sous-page a son propre ViewModel, Hilt fournit le bon automatiquement
            // le flow Room du tab inactif est arrêté après 5s (SharingStarted.WhileSubscribed)
            when (selectedTab) {
                0 -> AllCandidatesTab(query = query, onCandidateClick = onCandidateClick)
                else -> FavoriteCandidatesTab(query = query, onCandidateClick = onCandidateClick)
            }
        }
    }
}

@Composable
private fun AllCandidatesTab(
    query: String,
    onCandidateClick: (Long) -> Unit,
    viewModel: AllCandidatesViewModel = hiltViewModel()
) {
    val candidates by viewModel.candidates.collectAsStateWithLifecycle()
    CandidatesList(candidates = candidates, query = query, onCandidateClick = onCandidateClick)
}

@Composable
private fun FavoriteCandidatesTab(
    query: String,
    onCandidateClick: (Long) -> Unit,
    viewModel: FavoriteCandidatesViewModel = hiltViewModel()
) {
    val candidates by viewModel.candidates.collectAsStateWithLifecycle()
    CandidatesList(candidates = candidates, query = query, onCandidateClick = onCandidateClick)
}

// rendu commun aux 2 tabs : loading / empty / liste
@Composable
private fun CandidatesList(
    candidates: List<Candidate>?,
    query: String,
    onCandidateClick: (Long) -> Unit
) {
    // filtre local en mémoire selon la recherche
    val visible = candidates?.filter {
        query.isBlank() ||
            it.firstName.contains(query, ignoreCase = true) ||
            it.lastName.contains(query, ignoreCase = true)
    }

    when {
        candidates == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        visible.isNullOrEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(
                text = stringResource(R.string.list_empty),
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
