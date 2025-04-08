package nikmax.mtree.gallery.core.data.preferences

import android.content.Context
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface GalleryPreferencesRepo {
    fun getPreferencesFlow(): Flow<GalleryPreferences>
    suspend fun savePreferences(preferences: GalleryPreferences)
}


class GalleryPreferencesRepoImpl(
    @ApplicationContext private val context: Context
) : GalleryPreferencesRepo {
    private val Context.datastore by dataStore(
        fileName = "gallery-preferences.json",
        serializer = GalleryPreferencesSerializer
    )
    
    override fun getPreferencesFlow(): Flow<GalleryPreferences> {
        return context.datastore.data
    }
    
    override suspend fun savePreferences(preferences: GalleryPreferences) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}
