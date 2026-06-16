package com.vitesse.hr.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

// table Room des candidats (Room génère le SQLite sous le capot)
// @ColumnInfo explicite = protège des renommages R8/ProGuard en release
@Entity(tableName = "candidates")
data class Candidate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "firstName")
    val firstName: String,
    @ColumnInfo(name = "lastName")
    val lastName: String,
    @ColumnInfo(name = "photoUri")
    val photoUri: String?,
    @ColumnInfo(name = "phoneNumber")
    val phoneNumber: String,
    @ColumnInfo(name = "emailAddress")
    val emailAddress: String,
    @ColumnInfo(name = "dateOfBirth")
    val dateOfBirth: LocalDate,
    @ColumnInfo(name = "expectedSalary")
    val expectedSalary: Double,
    @ColumnInfo(name = "notes")
    val notes: String = "",
    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false
) {
    val fullName: String get() = "$firstName $lastName"
    val age: Int get() = Period.between(dateOfBirth, LocalDate.now()).years
}
