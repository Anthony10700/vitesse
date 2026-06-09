package com.vitesse.hr.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitesse.hr.data.local.Candidate
import com.vitesse.hr.data.local.CandidateDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CandidateListViewModel @Inject constructor(
    private val dao: CandidateDao
) : ViewModel() {

    // null = en cours de chargement, liste = données reçues (vide ou non)
    val allCandidates: StateFlow<List<Candidate>?> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val favoriteCandidates: StateFlow<List<Candidate>?> = dao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
