package nikmax.gallery.core.preferences

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object GalleryPreferencesUtils {
    private val Context.datastore by dataStore(
        fileName = "gallery-preferences.json",
        serializer = GalleryPreferencesSerializer
    )
    
    fun getPreferencesFlow(context: Context): Flow<GalleryPreferences> {
        return context.datastore.data
    }
    
    suspend fun savePreferences(preferences: GalleryPreferences, context: Context) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}


private object GalleryPreferencesSerializer : Serializer<GalleryPreferences> {
    override val defaultValue: GalleryPreferences
        get() = GalleryPreferences()
    
    override suspend fun readFrom(input: InputStream): GalleryPreferences {
        return try {
            defaultValue
            Json.decodeFromString(
                deserializer = GalleryPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        }
        catch (e: SerializationException) {
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: GalleryPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
