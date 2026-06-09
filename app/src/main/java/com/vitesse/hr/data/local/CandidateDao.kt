package com.vitesse.hr.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDao {

    // listes réactives : tout changement en base émet une nouvelle valeur
    @Query("SELECT * FROM candidates ORDER BY firstName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<Candidate>>

    @Query("SELECT * FROM candidates WHERE isFavorite = 1 ORDER BY firstName COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<Candidate>>

    // récupération à la demande pour l'écran détail/édition
    @Query("SELECT * FROM candidates WHERE id = :id")
    fun getById(id: Long): Flow<Candidate?>

    // recherche insensible à la casse sur le prénom ou le nom
    @Query(
        "SELECT * FROM candidates " +
        "WHERE firstName LIKE '%' || :query || '%' COLLATE NOCASE " +
        "   OR lastName  LIKE '%' || :query || '%' COLLATE NOCASE " +
        "ORDER BY firstName COLLATE NOCASE ASC"
    )
    fun search(query: String): Flow<List<Candidate>>

    @Insert
    suspend fun insert(candidate: Candidate): Long

    @Update
    suspend fun update(candidate: Candidate)

    @Delete
    suspend fun delete(candidate: Candidate)

    @Query("UPDATE candidates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
