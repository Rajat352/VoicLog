package com.voiclog.di

import android.content.Context
import com.voiclog.data.audio.AudioRecorder
import com.voiclog.data.audio.AudioRecorderImpl
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
}