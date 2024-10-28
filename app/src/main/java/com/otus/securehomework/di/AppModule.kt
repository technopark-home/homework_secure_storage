package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import com.otus.securehomework.protection.AesKey
import com.otus.securehomework.protection.AesKeysSharedPreferences
import com.otus.securehomework.protection.BiometricAesKey
import com.otus.securehomework.protection.BiometricModule
import com.otus.securehomework.protection.BiometricPreferences
import com.otus.securehomework.protection.CIPHER_AES_BIOMETRIC
import com.otus.securehomework.protection.CipherManager
import com.otus.securehomework.protection.IAesKey
import com.otus.securehomework.protection.IGeneratorParameterSpecRSA
import com.otus.securehomework.protection.PREF_BIOMETRIC
import com.otus.securehomework.protection.RSAGenEncryptSpecImpl
import com.otus.securehomework.protection.RSAGeneratorSpecImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideUserDatastore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile(PREF_BIOMETRIC)
            }
        )
    }


    @Singleton
    @Provides
    fun provideBiometricPreferences(
        preferencesDataStore: DataStore<Preferences>
    ): BiometricPreferences {
        return BiometricPreferences(preferencesDataStore)
    }

    @Singleton
    @Provides
    fun provideBiometricModule(
        biometricPreferences: BiometricPreferences,
        @Named(CIPHER_AES_BIOMETRIC) cryptoManager: CipherManager
    ) : BiometricModule {
        return BiometricModule(biometricPreferences,cryptoManager)
    }

    @Singleton
    @Provides
    fun provideAesKey(
        @ApplicationContext context: Context,
        generatorParameterSpecRSA : IGeneratorParameterSpecRSA
    ) : IAesKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AesKey()
        } else {
            AesKeysSharedPreferences(context,generatorParameterSpecRSA )
        }
    }

    @Singleton
    @Provides
    fun provideGeneratorParameterSpecRSA(
        @ApplicationContext context: Context
    ) : IGeneratorParameterSpecRSA {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RSAGenEncryptSpecImpl()
        } else {
            RSAGeneratorSpecImpl(context)
        }
    }

    @Singleton
    @Provides
    fun provideCipherManager(
        aesKey: IAesKey
    ) : CipherManager {
        return CipherManager(aesKey)
    }

    @Singleton
    @Provides
    fun provideBiometricAesKey(
    ) : BiometricAesKey {
        return BiometricAesKey()
    }

    @Singleton
    @Provides
    @Named(CIPHER_AES_BIOMETRIC)
    fun provideBiometricsCipherManager(
        biometricAesKey: BiometricAesKey
    ) : CipherManager {
        return CipherManager(biometricAesKey)
    }

    @Singleton
    @Provides
    fun provideRemoteDataSource(
        userPreferences: UserPreferences
    ): RemoteDataSource {
        return RemoteDataSource(userPreferences)
    }

    @Provides
    fun provideAuthApi(
        remoteDataSource: RemoteDataSource,
    ): AuthApi {
        return remoteDataSource.buildApi(AuthApi::class.java)
    }

    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
    ): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserPreferences(
        @ApplicationContext context: Context,
        cipherManager: CipherManager
    ): UserPreferences {
        return UserPreferences(context, cipherManager)
    }

    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepository(authApi, userPreferences)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi,
    ): UserRepository {
        return UserRepository(userApi)
    }
}