package mtree.core.preferences

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object MtreePreferencesUtils {
    private const val FILE_NAME = "preferences.json"
    
    private val Context.datastore by dataStore(
        fileName = FILE_NAME,
        serializer = MtreePreferencesSerializer
    )
    
    fun getPreferencesFlow(context: Context): Flow<MtreePreferences> {
        return context.datastore.data
    }
    
    suspend fun savePreferences(preferences: MtreePreferences, context: Context) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}
