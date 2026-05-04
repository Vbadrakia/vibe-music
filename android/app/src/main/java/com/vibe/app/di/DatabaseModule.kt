package com.vibe.app.di

import android.content.Context
import androidx.room.Room
import com.vibe.app.data.local.VibeDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): VibeDatabase =
        Room.databaseBuilder(context, VibeDatabase::class.java, "vibe_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSongDao(db: VibeDatabase) = db.songDao()
    @Provides fun provideAlbumDao(db: VibeDatabase) = db.albumDao()
    @Provides fun provideArtistDao(db: VibeDatabase) = db.artistDao()
}
