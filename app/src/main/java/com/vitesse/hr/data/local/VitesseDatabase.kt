package com.vitesse.hr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Candidate::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VitesseDatabase : RoomDatabase() {
    abstract fun candidateDao(): CandidateDao
}
