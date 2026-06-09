package com.vitesse.hr.di

import android.content.Context
import androidx.room.Room
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
        ).build()

    @Provides
    fun provideCandidateDao(database: VitesseDatabase): CandidateDao =
        database.candidateDao()
}
