package com.voiclog.di

import android.content.Context
import com.voiclog.data.AppDatabase
import com.voiclog.data.dao.RecordingDao
import com.voiclog.data.repository.ModelDownloadRepository
import com.voiclog.data.repository.ModelDownloadRepositoryImpl
import com.voiclog.data.repository.RecordingRepository
import com.voiclog.data.repository.RecordingRepositoryImpl
import com.voiclog.data.summarization.Summarizer
import com.voiclog.data.transcription.Transcriber
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRecordingDao(db: AppDatabase): RecordingDao {
        return db.recordingDao()
    }

    @Singleton
    @Provides
    fun provideRecordingRepository(recordingDao: RecordingDao, transcriber: Transcriber, summarizer: Summarizer): RecordingRepository {
        return RecordingRepositoryImpl(recordingDao, transcriber, summarizer)
    }

    @Singleton
    @Provides
    fun provideModelDownloadRepository(@ApplicationContext context: Context): ModelDownloadRepository {
        return ModelDownloadRepositoryImpl(context)
    }
}