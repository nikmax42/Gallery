package nikmax.gallery.gallery.core.data.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object GalleryPreferencesSerializer : Serializer<GalleryPreferences> {
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
