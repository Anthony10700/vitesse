package com.vitesse.hr.ui.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitesse.hr.R
import com.vitesse.hr.data.local.Candidate
import com.vitesse.hr.data.local.CandidateDao
import com.vitesse.hr.ui.navigation.VitesseDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// erreurs de validation : on stocke des @StringRes Int? pour rester découplé du Context
// la UI résout via stringResource(errorId) -> i18n automatique
data class AddEditFieldErrors(
    @param:StringRes val firstName: Int? = null,
    @param:StringRes val lastName: Int? = null,
    @param:StringRes val phoneNumber: Int? = null,
    @param:StringRes val emailAddress: Int? = null,
    @param:StringRes val dateOfBirth: Int? = null,
    @param:StringRes val expectedSalary: Int? = null
) {
    val hasError: Boolean
        get() = firstName != null || lastName != null || phoneNumber != null ||
            emailAddress != null || dateOfBirth != null || expectedSalary != null
}

// état complet du formulaire à un instant T (immuable, on en crée des copies via .copy())
data class AddEditUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val dateOfBirth: LocalDate? = null,
    // String et non Double : permet à l'utilisateur de taper progressivement "1500,5"
    val expectedSalary: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    // préservé en mode édition, l'utilisateur ne le modifie pas depuis ce formulaire
    val isFavorite: Boolean = false,
    val errors: AddEditFieldErrors = AddEditFieldErrors(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    // passe à true quand la sauvegarde réussit -> la UI déclenche popBackStack
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditCandidateViewModel @Inject constructor(
    private val dao: CandidateDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // récupère le candidateId depuis l'argument de navigation (null en mode création)
    private val candidateId: Long? = savedStateHandle.get<Long>(VitesseDestinations.ARG_CANDIDATE_ID)
    val isEditMode: Boolean get() = candidateId != null

    private val _uiState = MutableStateFlow(AddEditUiState(isLoading = isEditMode))
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        // mode édition : précharge les valeurs depuis la base
        if (candidateId != null) {
            viewModelScope.launch {
                val candidate = dao.getById(candidateId).first()
                if (candidate != null) {
                    _uiState.value = AddEditUiState(
                        firstName = candidate.firstName,
                        lastName = candidate.lastName,
                        phoneNumber = candidate.phoneNumber,
                        emailAddress = candidate.emailAddress,
                        dateOfBirth = candidate.dateOfBirth,
                        expectedSalary = candidate.expectedSalary.toString(),
                        notes = candidate.notes,
                        photoUri = candidate.photoUri,
                        isFavorite = candidate.isFavorite,
                        isLoading = false
                    )
                }
            }
        }
    }

    // setters appelés par la UI à chaque modification de champ.
    // on efface l'erreur du champ modifié pour un feedback immédiat.
    fun onFirstNameChange(v: String) = update { it.copy(firstName = v, errors = it.errors.copy(firstName = null)) }
    fun onLastNameChange(v: String) = update { it.copy(lastName = v, errors = it.errors.copy(lastName = null)) }
    fun onPhoneNumberChange(v: String) = update { it.copy(phoneNumber = v, errors = it.errors.copy(phoneNumber = null)) }
    fun onEmailAddressChange(v: String) = update { it.copy(emailAddress = v, errors = it.errors.copy(emailAddress = null)) }
    fun onDateOfBirthChange(v: LocalDate?) = update { it.copy(dateOfBirth = v, errors = it.errors.copy(dateOfBirth = null)) }
    fun onExpectedSalaryChange(v: String) = update { it.copy(expectedSalary = v, errors = it.errors.copy(expectedSalary = null)) }
    fun onNotesChange(v: String) = update { it.copy(notes = v) }
    fun onPhotoUriChange(uri: String?) = update { it.copy(photoUri = uri) }

    // valide puis sauve en base si tout est OK
    fun save() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.hasError) {
            _uiState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            // conversion finale du salaire (la saisie autorise "1500,5" ou "1500.5")
            val salaryDouble = state.expectedSalary.replace(',', '.').toDouble()
            val candidate = Candidate(
                id = candidateId ?: 0,
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                photoUri = state.photoUri,
                phoneNumber = state.phoneNumber.trim(),
                emailAddress = state.emailAddress.trim(),
                dateOfBirth = state.dateOfBirth!!,
                expectedSalary = salaryDouble,
                notes = state.notes,
                isFavorite = state.isFavorite
            )
            if (candidateId != null) {
                dao.update(candidate)
            } else {
                dao.insert(candidate)
            }
            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    // règles de validation : retourne les erreurs trouvées sous forme de @StringRes
    private fun validate(state: AddEditUiState): AddEditFieldErrors {
        val salaryParsed = state.expectedSalary.replace(',', '.').toDoubleOrNull()
        return AddEditFieldErrors(
            firstName = if (state.firstName.isBlank()) R.string.error_required else null,
            lastName = if (state.lastName.isBlank()) R.string.error_required else null,
            phoneNumber = if (state.phoneNumber.isBlank()) R.string.error_required else null,
            emailAddress = when {
                state.emailAddress.isBlank() -> R.string.error_required
                !EMAIL_REGEX.matches(state.emailAddress) -> R.string.error_email_invalid
                else -> null
            },
            dateOfBirth = when {
                state.dateOfBirth == null -> R.string.error_date_required
                !state.dateOfBirth.isBefore(LocalDate.now()) -> R.string.error_date_future
                else -> null
            },
            expectedSalary = when {
                state.expectedSalary.isBlank() -> R.string.error_required
                salaryParsed == null -> R.string.error_salary_invalid
                salaryParsed < 0 -> R.string.error_salary_negative
                else -> null
            }
        )
    }

    // helper pour réduire la verbosité des setters
    private inline fun update(transform: (AddEditUiState) -> AddEditUiState) {
        _uiState.value = transform(_uiState.value)
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
