package com.undefault.bitride.di

import android.app.Application
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.data.repository.UserProfileRepository
import com.undefault.bitride.data.repository.FirestoreUserProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        app: Application
    ): UserPreferencesRepository = UserPreferencesRepository(app)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        firestore: FirebaseFirestore,
        userPreferencesRepository: UserPreferencesRepository
    ): UserProfileRepository =
        FirestoreUserProfileRepository(firestore, userPreferencesRepository)
}
