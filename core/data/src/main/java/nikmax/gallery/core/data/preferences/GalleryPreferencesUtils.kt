package nikmax.gallery.core.data.preferences

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object GalleryPreferencesUtils {
    private val Context.datastore by dataStore(
        fileName = "gallery-preferences.json",
        serializer = GalleryPreferencesSerializer
    )

    fun getPreferencesFlow(context: Context): Flow<GalleryPreferences> {
        return context.datastore.data
    }

    suspend fun savePreferences(context: Context, preferences: GalleryPreferences) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}
