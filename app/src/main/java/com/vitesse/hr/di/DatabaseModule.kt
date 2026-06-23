package com.vitesse.hr.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vitesse.hr.data.local.CandidateDao
import com.vitesse.hr.data.local.VitesseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitesseDatabase =
        Room.databaseBuilder(
            context,
            VitesseDatabase::class.java,
            "vitesse.db"
        )
            .addCallback(SeedCallback)
            .build()

    @Provides
    fun provideCandidateDao(database: VitesseDatabase): CandidateDao =
        database.candidateDao()

    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            SEED_SQL.forEach { db.execSQL(it) }
        }
    }

    private const val P = "file:///android_asset/avatar_"

    private val SEED_SQL = listOf(
        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Marc', 'Lefèvre', '${P}01.jpg', '0612345678', 'marc.lefevre@example.com', '1995-04-12', 45000, 'Bon profil Android, motivé par Compose.', 1)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Sophie', 'Martin', '${P}06.jpg', '0698765432', 'sophie.martin@example.com', '1990-07-23', 52000, 'Forte expérience Jetpack Compose.', 1)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Thomas', 'Bernard', '${P}02.jpg', '0623456789', 'thomas.bernard@example.com', '1987-11-05', 65000, 'Potentiel tech lead, à revoir.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Emma', 'Rousseau', '${P}07.jpg', '0687654321', 'emma.rousseau@example.com', '1998-02-19', 38000, 'Junior motivée, formation Kotlin solide.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Lucas', 'Dubois', '${P}03.jpg', '0634567890', 'lucas.dubois@example.com', '1985-09-30', 72000, 'Très bon profil backend Spring, ouvert au mobile.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Camille', 'Petit', '${P}08.jpg', '0676543210', 'camille.petit@example.com', '1993-06-14', 48000, 'DevOps + AWS, dispo immédiatement.', 1)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Antoine', 'Moreau', '${P}04.jpg', '0645678901', 'antoine.moreau@example.com', '1992-12-08', 55000, 'iOS + Android, parle anglais courant.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Léa', 'Garnier', '${P}09.jpg', '0665432109', 'lea.garnier@example.com', '2001-03-25', 32000, 'Diplômée 2024, premier emploi.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Hugo', 'Faure', '${P}05.jpg', '0656789012', 'hugo.faure@example.com', '1989-08-17', 60000, 'Lead frontend React, veut bouger vers mobile.', 0)""",

        """INSERT INTO candidates (firstName, lastName, photoUri, phoneNumber, emailAddress, dateOfBirth, expectedSalary, notes, isFavorite)
           VALUES ('Manon', 'Roux', '${P}10.jpg', '0654321098', 'manon.roux@example.com', '1996-10-02', 42000, 'QA automation, expertise Selenium et Espresso.', 0)"""
    )
}
