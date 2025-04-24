package mtree.core.preferences

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface MtreePreferencesRepo {
    fun getPreferencesFlow(): Flow<MtreePreferences>
    
    suspend fun savePreferences(preferences: MtreePreferences)
}


class MtreePreferencesRepoImpl(private val context: Context) : MtreePreferencesRepo {
    companion object {
        private const val FILE_NAME = "preferences.json"
    }
    
    private val Context.datastore by dataStore(
        fileName = FILE_NAME,
        serializer = MtreePreferencesSerializer
    )
    
    override fun getPreferencesFlow(): Flow<MtreePreferences> {
        return context.datastore.data
    }
    
    override suspend fun savePreferences(preferences: MtreePreferences) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}
