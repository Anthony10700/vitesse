package com.vitesse.hr.ui.navigation

// routes de navigation centralisées : évite les typos et facilite les renommages
object VitesseDestinations {
    const val LIST = "list"
    const val ADD = "add"

    // route paramétrée pour l'édition (jalon 6) : edit/{candidateId}
    const val EDIT = "edit/{candidateId}"
    fun edit(id: Long) = "edit/$id"

    // route paramétrée pour le détail (jalon 6)
    const val DETAIL = "detail/{candidateId}"
    fun detail(id: Long) = "detail/$id"

    // nom de l'argument utilisé dans les routes paramétrées
    const val ARG_CANDIDATE_ID = "candidateId"
}
