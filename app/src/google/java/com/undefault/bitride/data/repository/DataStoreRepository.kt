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
import app.organicmaps.bitride.mesh.MeshManager

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
        private val KEY_ID_CARD_PHOTO_PATH = stringPreferencesKey("id_card_photo_path")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_NIK = stringPreferencesKey("nik")
        private val KEY_BANK_NAME = stringPreferencesKey("bank_name")
        private val KEY_BANK_ACCOUNT = stringPreferencesKey("bank_account_number")
    }

    val activeMapFileNameFlow: Flow<String?> = dataStore.data.map { it[KEY_MAP_FILE_NAME] }
    val activePoiDbNameFlow: Flow<String?> = dataStore.data.map { it[KEY_POI_DB_NAME] }
    val nikHashFlow: Flow<String?> = dataStore.data.map { it[KEY_NIK_HASH] }
    val rolesFlow: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[KEY_ROLES]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
    val idCardPhotoPathFlow: Flow<String?> = dataStore.data.map { it[KEY_ID_CARD_PHOTO_PATH] }
    val fullNameFlow: Flow<String?> = dataStore.data.map { it[KEY_FULL_NAME] }
    val nikFlow: Flow<String?> = dataStore.data.map { it[KEY_NIK] }
    val bankNameFlow: Flow<String?> = dataStore.data.map { it[KEY_BANK_NAME] }
    val bankAccountNumberFlow: Flow<String?> = dataStore.data.map { it[KEY_BANK_ACCOUNT] }

    suspend fun setActiveMapFileName(fileName: String) {
        dataStore.edit { it[KEY_MAP_FILE_NAME] = fileName }
    }

    suspend fun setActivePoiDbName(name: String) {
        dataStore.edit { it[KEY_POI_DB_NAME] = name }
    }

    suspend fun saveIdCardPhotoPath(path: String) {
        dataStore.edit { it[KEY_ID_CARD_PHOTO_PATH] = path }
    }

    suspend fun savePersonalInfo(name: String, nik: String) {
        dataStore.edit {
            it[KEY_FULL_NAME] = name
            it[KEY_NIK] = nik
        }
    }

    suspend fun saveBankInfo(bankName: String, accountNumber: String) {
        dataStore.edit {
            it[KEY_BANK_NAME] = bankName
            it[KEY_BANK_ACCOUNT] = accountNumber
        }
    }

    suspend fun saveLoggedInUser(nikHash: String, roles: List<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_NIK_HASH] = nikHash
            prefs[KEY_ROLES] = roles.toSet().joinToString(",")
        }
    }

    suspend fun clearLoggedInUser() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_NIK_HASH)
            prefs.remove(KEY_ROLES)
        }
        MeshManager.stop(context)
    }
}
