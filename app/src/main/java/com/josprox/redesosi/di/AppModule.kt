package com.josprox.redesosi.di

import android.content.Context
import com.josprox.redesosi.data.database.AppDatabase
import com.josprox.redesosi.data.database.StudyDao
import com.josprox.redesosi.data.network.GroqApiService
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideStudyDao(appDatabase: AppDatabase): StudyDao {
        return appDatabase.studyDao()
    }

    @Provides
    @Singleton
    fun provideGroqApiService(): GroqApiService {
        return GroqApiService()
    }

    @Provides
    @Singleton
    fun provideStudyRepository(dao: StudyDao, groqApi: GroqApiService): StudyRepository {
        return StudyRepository(dao, groqApi)
    }
}
