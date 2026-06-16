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

// ViewModel dédié au tab "Tous" : ne gère QUE la liste complète des candidats
@HiltViewModel
class AllCandidatesViewModel @Inject constructor(
    dao: CandidateDao
) : ViewModel() {

    // null = en cours de chargement, liste = données reçues (vide ou non)
    val candidates: StateFlow<List<Candidate>?> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
