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

object AppPreferencesUtils {
    private val Context.datastore by dataStore(
        fileName = "application-preferences.json",
        serializer = AppPreferencesSerializer
    )

    fun getPreferencesFlow(context: Context): Flow<AppPreferences> {
        return context.datastore.data
    }

    suspend fun savePreferences(preferences: AppPreferences, context: Context) {
        withContext(Dispatchers.IO) {
            context.datastore.updateData { preferences }
        }
    }
}

private object AppPreferencesSerializer : Serializer<AppPreferences> {
    override val defaultValue: AppPreferences
        get() = AppPreferences()

    override suspend fun readFrom(input: InputStream): AppPreferences {
        return try {
            defaultValue
            Json.decodeFromString(
                deserializer = AppPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AppPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
