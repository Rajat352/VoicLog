package com.voiclog.di

import android.content.Context
import androidx.room3.Room
import com.voiclog.data.AppDatabase
import com.voiclog.data.audio.AudioRecorder
import com.voiclog.data.audio.AudioRecorderImpl
import com.voiclog.data.transcription.Transcriber
import com.voiclog.data.transcription.TranscriberImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAudioRecorder(@ApplicationContext context: Context) : AudioRecorder {
        return AudioRecorderImpl(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) : AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideTranscriber(@ApplicationContext context: Context) : Transcriber {
        return TranscriberImpl(context)
    }
}