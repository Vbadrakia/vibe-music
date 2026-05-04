package com.vibe.app.di

import android.content.Context
import com.vibe.app.media.PlayerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun providePlayerController(@ApplicationContext context: Context): PlayerController =
        PlayerController(context)
}
