package nikmax.gallery.core.preferences

import android.content.Context
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface CorePreferencesRepo {
    fun getPreferencesFlow(): Flow<CorePreferences>
    suspend fun savePreferences(preferences: CorePreferences)
}

class CorePreferencesRepoImpl(
    @ApplicationContext private val context: Context
) : CorePreferencesRepo {
    
    private val Context.datastore by dataStore(
        fileName = "core-preferences.json",
        serializer = CorePreferencesSerializer
    )
    
    override fun getPreferencesFlow(): Flow<CorePreferences> {
        return context.datastore.data
    }
    
    override suspend fun savePreferences(preferences: CorePreferences) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
    
}
