package com.undefault.bitride.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "bitride_prefs")

@Singleton
class DataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_MAP_FILE_NAME = stringPreferencesKey("active_map_file_name")
        private val KEY_POI_DB_NAME = stringPreferencesKey("active_poi_db_name")
        private val KEY_NIK_HASH = stringPreferencesKey("nik_hash")
        private val KEY_ROLES = stringPreferencesKey("user_roles")
    }

    val activeMapFileNameFlow: Flow<String?> = dataStore.data.map { it[KEY_MAP_FILE_NAME] }
    val activePoiDbNameFlow: Flow<String?> = dataStore.data.map { it[KEY_POI_DB_NAME] }
    val nikHashFlow: Flow<String?> = dataStore.data.map { it[KEY_NIK_HASH] }
    val rolesFlow: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[KEY_ROLES]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setActiveMapFileName(fileName: String) {
        dataStore.edit { it[KEY_MAP_FILE_NAME] = fileName }
    }

    suspend fun setActivePoiDbName(name: String) {
        dataStore.edit { it[KEY_POI_DB_NAME] = name }
    }

    suspend fun saveLoggedInUser(nikHash: String, role: String) {
        dataStore.edit { prefs ->
            prefs[KEY_NIK_HASH] = nikHash
            val roles = prefs[KEY_ROLES]?.split(",")?.toMutableSet() ?: mutableSetOf()
            if (roles.add(role)) {
                prefs[KEY_ROLES] = roles.joinToString(",")
            }
        }
    }

    suspend fun clearLoggedInUser() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_NIK_HASH)
            prefs.remove(KEY_ROLES)
        }
    }
}
