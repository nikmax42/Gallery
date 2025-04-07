package nikmax.gallery.core.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object CorePreferencesSerializer : Serializer<CorePreferences> {
    override val defaultValue: CorePreferences
        get() = CorePreferences()
    
    override suspend fun readFrom(input: InputStream): CorePreferences {
        return try {
            defaultValue
            Json.decodeFromString(
                deserializer = CorePreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        }
        catch (e: SerializationException) {
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: CorePreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
