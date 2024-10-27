package com.otus.securehomework.protection

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

const val PREF_BIOMETRIC = "preferences_user_biometric_data "

class BiometricPreferences
@Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>
)
{
    object PreferencesKey {
        val KEY_USER_DATA = stringPreferencesKey("user_biometric_data")
    }

    suspend fun setStoreUserData(userData: CharSequence) {
        preferencesDataStore.edit { preferences ->
            preferences[PreferencesKey.KEY_USER_DATA] = userData.toString()
        }
    }

    suspend fun getStoreUserData() : CharSequence {
        return try {
            preferencesDataStore.data.first()[PreferencesKey.KEY_USER_DATA].orEmpty()
        } catch( _ : IOException) {
            ""
        }
    }

    fun isBiometricEnabled(): Flow<Boolean> {
        return preferencesDataStore.data.map { value ->
            !value[PreferencesKey.KEY_USER_DATA].isNullOrEmpty()
        }
    }

    suspend fun clearBiometric() {
        preferencesDataStore.edit {preferences ->
            preferences.remove(PreferencesKey.KEY_USER_DATA)
        }
    }
}