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

// ViewModel dédié au tab "Favoris" : ne gère QUE les candidats favoris
@HiltViewModel
class FavoriteCandidatesViewModel @Inject constructor(
    dao: CandidateDao
) : ViewModel() {

    val candidates: StateFlow<List<Candidate>?> = dao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
