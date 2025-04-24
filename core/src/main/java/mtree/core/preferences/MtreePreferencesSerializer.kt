package mtree.core.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object MtreePreferencesSerializer : Serializer<MtreePreferences> {
    
    override val defaultValue = MtreePreferences.default()
    
    override suspend fun readFrom(input: InputStream): MtreePreferences {
        return try {
            defaultValue
            Json.decodeFromString(
                deserializer = MtreePreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        }
        catch (e: SerializationException) {
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: MtreePreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
