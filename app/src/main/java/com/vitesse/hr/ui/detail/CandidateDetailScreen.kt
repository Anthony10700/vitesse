package com.vitesse.hr.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.vitesse.hr.R
import com.vitesse.hr.data.local.Candidate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailScreen(
    onBack: () -> Unit = {},
    onEdit: (Long) -> Unit = {},
    viewModel: CandidateDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // suppression réussie -> retour automatique à la liste
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    val candidate = uiState.candidate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(candidate?.fullName.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back_description)
                        )
                    }
                },
                actions = {
                    // actions disponibles uniquement quand le candidat est chargé
                    if (candidate != null) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (candidate.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(R.string.detail_favorite_description)
                            )
                        }
                        IconButton(onClick = { onEdit(candidate.id) }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.detail_edit_description)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.detail_delete_description)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            candidate != null -> DetailContent(
                candidate = candidate,
                gbpRate = uiState.gbpRate,
                rateLoading = uiState.rateLoading,
                onCall = { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${candidate.phoneNumber}".toUri())) },
                onSms = { context.startActivity(Intent(Intent.ACTION_SENDTO, "smsto:${candidate.phoneNumber}".toUri())) },
                onEmail = { context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:${candidate.emailAddress}".toUri())) },
                modifier = Modifier.padding(padding)
            )
            // candidate == null && !isLoading : transition vers isDeleted, rien à afficher
        }
    }

    if (showDeleteDialog && candidate != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_dialog_title)) },
            text = { Text(stringResource(R.string.detail_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCandidate()
                }) {
                    Text(stringResource(R.string.detail_delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.detail_delete_dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailContent(
    candidate: Candidate,
    gbpRate: Double?,
    rateLoading: Boolean,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val salaryFormatter = remember { NumberFormat.getCurrencyInstance(Locale.FRANCE) }
    val gbpFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
            currency = Currency.getInstance("GBP")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // photo en bandeau quasi pleine largeur (suit le wireframe Figma)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(195.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            if (candidate.photoUri != null) {
                AsyncImage(
                    model = candidate.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3 boutons d'action ronds avec label dessous (cercles + texte sous le bouton)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleActionButton(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.detail_action_call),
                onClick = onCall
            )
            CircleActionButton(
                icon = Icons.Default.Sms,
                label = stringResource(R.string.detail_action_sms),
                onClick = onSms
            )
            CircleActionButton(
                icon = Icons.Default.Email,
                label = stringResource(R.string.detail_action_email),
                onClick = onEmail
            )
        }

        // sections d'info présentées en cards Material 3
        InfoCard(label = stringResource(R.string.detail_section_birthday)) {
            Text(
                text = stringResource(
                    R.string.detail_birthday_format,
                    candidate.dateOfBirth.format(dateFormatter),
                    candidate.age
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        InfoCard(label = stringResource(R.string.detail_section_salary)) {
            Text(
                text = salaryFormatter.format(candidate.expectedSalary),
                style = MaterialTheme.typography.bodyLarge
            )
            if (rateLoading) {
                Text(
                    text = stringResource(R.string.detail_rate_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (gbpRate != null) {
                val gbpAmount = candidate.expectedSalary * gbpRate
                Text(
                    text = stringResource(R.string.detail_rate_gbp_format, gbpFormatter.format(gbpAmount)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.detail_rate_offline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (candidate.notes.isNotBlank()) {
            InfoCard(label = stringResource(R.string.detail_section_notes)) {
                Text(
                    text = candidate.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// bouton d'action rond (cercle 56dp) avec son label en dessous
@Composable
private fun CircleActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(icon, contentDescription = null)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// card Material 3 contenant un titre de section et son contenu
@Composable
private fun InfoCard(
    label: String,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}
