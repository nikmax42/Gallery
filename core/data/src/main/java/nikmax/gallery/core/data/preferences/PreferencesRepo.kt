package nikmax.gallery.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


interface PreferencesRepo {
    fun getPreferencesFlow(): Flow<OLDGalleryPreferences>
    suspend fun savePreferences(preferences: OLDGalleryPreferences)
}


class PreferencesRepoImpl
@Inject constructor(
    private val context: Context
) : PreferencesRepo {

    private object Keys {
        val ALBUMS_MODE = intPreferencesKey("ALBUMS_MODE")
        val SORTING_ORDER = intPreferencesKey("SORTING_ORDER")
        val DESCEND_SORTING = booleanPreferencesKey("DESCEND_SORTING")
        val GRID_COLUMNS_PORTRAIT = intPreferencesKey("GRID_COLUMNS_PORTRAIT")
        val GRID_COLUMNS_LANDSCAPE = intPreferencesKey("GRID_COLUMNS_LANDSCAPE")
        val SELECTED_FILTERS = stringSetPreferencesKey("SELECTED_FILTERS")
        val SHOW_HIDDEN = booleanPreferencesKey("SHOW_HIDDEN")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    override fun getPreferencesFlow(): Flow<OLDGalleryPreferences> {
        return context.dataStore.data.map { preferences ->
            OLDGalleryPreferences(
                albumsMode = when (preferences[Keys.ALBUMS_MODE] ?: 0) {
                    0 -> OLDGalleryPreferences.AlbumsMode.PLAIN
                    else -> OLDGalleryPreferences.AlbumsMode.NESTED
                },
                sortingOrder = when (preferences[Keys.SORTING_ORDER] ?: 0) {
                    0 -> OLDGalleryPreferences.SortingOrder.CREATION_DATE
                    1 -> OLDGalleryPreferences.SortingOrder.MODIFICATION_DATE
                    2 -> OLDGalleryPreferences.SortingOrder.NAME
                    3 -> OLDGalleryPreferences.SortingOrder.SIZE
                    else -> OLDGalleryPreferences.SortingOrder.CREATION_DATE
                },
                descendSorting = preferences[Keys.DESCEND_SORTING] ?: false,
                gridColumnsPortrait = preferences[Keys.GRID_COLUMNS_PORTRAIT] ?: 3,
                gridColumnsLandscape = preferences[Keys.GRID_COLUMNS_LANDSCAPE] ?: 4,
                enabledFilters = preferences[Keys.SELECTED_FILTERS]?.map {
                    when (it) {
                        OLDGalleryPreferences.Filter.IMAGES.name -> OLDGalleryPreferences.Filter.IMAGES
                        OLDGalleryPreferences.Filter.VIDEOS.name -> OLDGalleryPreferences.Filter.VIDEOS
                        OLDGalleryPreferences.Filter.GIFS.name -> OLDGalleryPreferences.Filter.GIFS
                        else -> OLDGalleryPreferences.Filter.IMAGES
                    }
                }?.toSet() ?: setOf(
                    OLDGalleryPreferences.Filter.IMAGES,
                    OLDGalleryPreferences.Filter.VIDEOS,
                    OLDGalleryPreferences.Filter.GIFS
                ),
                showHidden = preferences[Keys.SHOW_HIDDEN] ?: false
            )
        }
    }

    override suspend fun savePreferences(preferences: OLDGalleryPreferences) {
        context.dataStore.edit { data ->
            data[Keys.ALBUMS_MODE] = when (preferences.albumsMode) {
                OLDGalleryPreferences.AlbumsMode.PLAIN -> 0
                OLDGalleryPreferences.AlbumsMode.NESTED -> 1
            }
            data[Keys.SORTING_ORDER] = when (preferences.sortingOrder) {
                OLDGalleryPreferences.SortingOrder.CREATION_DATE -> 0
                OLDGalleryPreferences.SortingOrder.MODIFICATION_DATE -> 1
                OLDGalleryPreferences.SortingOrder.NAME -> 2
                OLDGalleryPreferences.SortingOrder.SIZE -> 3
            }
            data[Keys.DESCEND_SORTING] = preferences.descendSorting
            data[Keys.GRID_COLUMNS_PORTRAIT] = preferences.gridColumnsPortrait
            data[Keys.GRID_COLUMNS_LANDSCAPE] = preferences.gridColumnsLandscape
            data[Keys.SELECTED_FILTERS] = preferences.enabledFilters.map { it.name }.toSet()
            data[Keys.SHOW_HIDDEN] = preferences.showHidden
        }
    }
}
