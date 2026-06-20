package com.vitesse.hr.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitesse.hr.data.local.Candidate
import com.vitesse.hr.data.local.CandidateDao
import com.vitesse.hr.data.remote.ExchangeApi
import com.vitesse.hr.ui.navigation.VitesseDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CandidateDetailUiState(
    val candidate: Candidate? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val gbpRate: Double? = null,
    val rateLoading: Boolean = true
)

@HiltViewModel
class CandidateDetailViewModel @Inject constructor(
    private val dao: CandidateDao,
    private val exchangeApi: ExchangeApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val candidateId: Long = savedStateHandle.get<Long>(VitesseDestinations.ARG_CANDIDATE_ID)!!

    private val _uiState = MutableStateFlow(CandidateDetailUiState())
    val uiState: StateFlow<CandidateDetailUiState> = _uiState.asStateFlow()

    init {
        observeCandidate()
        loadExchangeRate()
    }

    private fun observeCandidate() {
        viewModelScope.launch {
            dao.getById(candidateId).collect { candidate ->
                _uiState.value = _uiState.value.copy(
                    candidate = candidate,
                    isLoading = candidate == null && !_uiState.value.isDeleted
                )
            }
        }
    }

    private fun loadExchangeRate() {
        viewModelScope.launch {
            val rate = try {
                val response = exchangeApi.getEurRates()
                response.eur["gbp"]
            } catch (e: Exception) {
                null
            }
            _uiState.value = _uiState.value.copy(gbpRate = rate, rateLoading = false)
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.candidate ?: return
        viewModelScope.launch {
            dao.update(current.copy(isFavorite = !current.isFavorite))
        }
    }

    fun deleteCandidate() {
        val current = _uiState.value.candidate ?: return
        viewModelScope.launch {
            dao.delete(current)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
