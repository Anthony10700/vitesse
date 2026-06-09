package com.vitesse.hr.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

// table Room des candidats (Room génère le SQLite sous le capot)
@Entity(tableName = "candidates")
data class Candidate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val photoUri: String?,
    val phoneNumber: String,
    val emailAddress: String,
    val dateOfBirth: LocalDate,
    val expectedSalary: Double,
    val notes: String = "",
    val isFavorite: Boolean = false
) {
    val fullName: String get() = "$firstName $lastName"
    val age: Int get() = Period.between(dateOfBirth, LocalDate.now()).years
}
