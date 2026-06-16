package com.vitesse.hr.ui.edit

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.vitesse.hr.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCandidateScreen(
    candidateId: Long? = null,
    onBack: () -> Unit = {},
    viewModel: AddEditCandidateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // sauvegarde réussie -> retour à l'écran précédent
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (viewModel.isEditMode) R.string.form_screen_title_edit
                            else R.string.form_screen_title_add
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.form_back_description)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhotoSection(
                photoUri = uiState.photoUri,
                onPhotoSelected = viewModel::onPhotoUriChange
            )

            FormTextField(
                value = uiState.firstName,
                onChange = viewModel::onFirstNameChange,
                label = stringResource(R.string.form_field_first_name),
                error = uiState.errors.firstName,
                leadingIcon = Icons.Default.Person
            )
            FormTextField(
                value = uiState.lastName,
                onChange = viewModel::onLastNameChange,
                label = stringResource(R.string.form_field_last_name),
                error = uiState.errors.lastName,
                leadingIcon = Icons.Default.Person
            )
            FormTextField(
                value = uiState.phoneNumber,
                onChange = viewModel::onPhoneNumberChange,
                label = stringResource(R.string.form_field_phone),
                error = uiState.errors.phoneNumber,
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )
            FormTextField(
                value = uiState.emailAddress,
                onChange = viewModel::onEmailAddressChange,
                label = stringResource(R.string.form_field_email),
                error = uiState.errors.emailAddress,
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            DateOfBirthField(
                value = uiState.dateOfBirth,
                onChange = viewModel::onDateOfBirthChange,
                error = uiState.errors.dateOfBirth
            )

            FormTextField(
                value = uiState.expectedSalary,
                onChange = viewModel::onExpectedSalaryChange,
                label = stringResource(R.string.form_field_salary),
                error = uiState.errors.expectedSalary,
                leadingIcon = Icons.Default.Euro,
                keyboardType = KeyboardType.Decimal
            )

            FormTextField(
                value = uiState.notes,
                onChange = viewModel::onNotesChange,
                label = stringResource(R.string.form_field_notes),
                error = null,
                leadingIcon = Icons.AutoMirrored.Filled.Notes,
                minLines = 3
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.form_button_save))
                }
            }
        }
    }
}

// avatar circulaire cliquable : ouvre le PhotoPicker système
@Composable
private fun PhotoSection(
    photoUri: String?,
    onPhotoSelected: (String?) -> Unit
) {
    val context = LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // persiste la permission de lecture pour survivre à un reboot de l'appareil
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("AddEditCandidate", "Impossible de conserver la permission URI", e)
            }
            onPhotoSelected(uri.toString())
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = stringResource(R.string.form_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.form_photo_description),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// champ date readonly qui ouvre le DatePicker Material 3 au clic.
// la sélection est bornée au passé via SelectableDates pour empêcher une saisie future.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(
    value: LocalDate?,
    onChange: (LocalDate?) -> Unit,
    error: Int?
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val display = value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

    // pattern Material 3 : intercepter les clics sur un TextField readOnly
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showPicker = true
        }
    }

    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.form_field_date_of_birth)) },
        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth()
    )

    if (showPicker) {
        // le DatePicker M3 travaille en UTC -> conversions explicites pour éviter
        // tout décalage d'un jour selon le fuseau horaire de l'appareil
        val initialMillis = value
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis < System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onChange(date)
                    }
                    showPicker = false
                }) {
                    Text(stringResource(R.string.form_date_picker_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.form_date_picker_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// champ texte réutilisable pour tout le formulaire : factorise label, erreur, icône, clavier
@Composable
private fun FormTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    error: Int?,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        modifier = Modifier.fillMaxWidth()
    )
}
