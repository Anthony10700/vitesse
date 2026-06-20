package com.vitesse.hr.ui.edit

import androidx.lifecycle.SavedStateHandle
import com.vitesse.hr.data.local.Candidate
import com.vitesse.hr.data.local.CandidateDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class AddEditCandidateViewModelTest {

    // fake DAO minimal pour les tests : ne fait rien d'utile mais permet de construire le VM
    private class FakeCandidateDao : CandidateDao {
        override fun getAll(): Flow<List<Candidate>> = flowOf(emptyList())
        override fun getFavorites(): Flow<List<Candidate>> = flowOf(emptyList())
        override fun getById(id: Long): Flow<Candidate?> = flowOf(null)
        override fun search(query: String): Flow<List<Candidate>> = flowOf(emptyList())
        override suspend fun insert(candidate: Candidate): Long = 1L
        override suspend fun update(candidate: Candidate) {}
        override suspend fun delete(candidate: Candidate) {}
    }

    // construit un VM en mode "Ajout" (pas de candidateId dans le SavedStateHandle)
    private fun newViewModel() = AddEditCandidateViewModel(
        dao = FakeCandidateDao(),
        savedStateHandle = SavedStateHandle()
    )

    @Test
    fun save_with_empty_firstName_returns_error() = runBlocking {
        val vm = newViewModel()
        // on ne remplit aucun champ et on tente de sauver
        vm.save()
        // une erreur doit être présente sur firstName
        assertNotNull(vm.uiState.value.errors.firstName)
    }

    @Test
    fun save_with_invalid_email_returns_error() = runBlocking {
        val vm = newViewModel()
        // tous les champs valides sauf l'email
        vm.onFirstNameChange("John")
        vm.onLastNameChange("Doe")
        vm.onPhoneNumberChange("0612345678")
        vm.onEmailAddressChange("pas-un-email")
        vm.onDateOfBirthChange(LocalDate.of(1990, 1, 1))
        vm.onExpectedSalaryChange("50000")
        vm.save()
        // erreur attendue sur emailAddress
        assertNotNull(vm.uiState.value.errors.emailAddress)
    }

    @Test
    fun save_with_future_date_returns_error() = runBlocking {
        val vm = newViewModel()
        // tous les champs valides sauf la date (dans le futur)
        vm.onFirstNameChange("John")
        vm.onLastNameChange("Doe")
        vm.onPhoneNumberChange("0612345678")
        vm.onEmailAddressChange("john@example.com")
        vm.onDateOfBirthChange(LocalDate.now().plusDays(1))
        vm.onExpectedSalaryChange("50000")
        vm.save()
        // erreur attendue sur dateOfBirth
        assertNotNull(vm.uiState.value.errors.dateOfBirth)
    }

    @Test
    fun save_with_negative_salary_returns_error() = runBlocking {
        val vm = newViewModel()
        // tous les champs valides sauf le salaire (négatif)
        vm.onFirstNameChange("John")
        vm.onLastNameChange("Doe")
        vm.onPhoneNumberChange("0612345678")
        vm.onEmailAddressChange("john@example.com")
        vm.onDateOfBirthChange(LocalDate.of(1990, 1, 1))
        vm.onExpectedSalaryChange("-100")
        vm.save()
        // erreur attendue sur expectedSalary
        assertNotNull(vm.uiState.value.errors.expectedSalary)
    }
}
