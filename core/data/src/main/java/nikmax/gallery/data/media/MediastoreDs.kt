package nikmax.gallery.data.media

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore


interface MediaStoreDs {
    fun rescanFiles()
    fun getFiles(): List<MediaFileData>
}


internal class MediaStoreDsImpl(private val context: Context) : MediaStoreDs {

    private var _files = listOf<MediaFileData>()

    override fun rescanFiles() {
        _files = getAllImagesAndVideos()
    }

    override fun getFiles(): List<MediaFileData> = _files

    private fun getAllImagesAndVideos(): MutableList<MediaFileData> {
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.VOLUME_NAME,
            MediaStore.MediaColumns.SIZE
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'image/%'" +
                " OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'video/%'"

        val mediaStoreUri = MediaStore.Files.getContentUri("external")
        val cursor = context.contentResolver.query(
            mediaStoreUri,
            projection,
            selection,
            null,
            null
        )
        val filesData = mutableListOf<MediaFileData>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val fileData = createMediaItemData(cursor)
                filesData.add(fileData)
            }
            cursor.close()
        }
        return filesData
    }

    private fun createMediaItemData(cursor: Cursor): MediaFileData {
        val data = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        )
        val dateAdded = cursor.getLong(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        )
        val dateModified = cursor.getLong(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        )
        val size = cursor.getLong(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        )
        val volumeName = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.VOLUME_NAME)
        )

        return MediaFileData(
            path = data,
            size = size,
            dateCreated = dateAdded,
            dateModified = dateModified,
            volume = if (volumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY) MediaFileData.Volume.PRIMARY
            else MediaFileData.Volume.SECONDARY
        )
    }

    private fun getExternalVolumeNames(context: Context): List<String> {
        return MediaStore.getExternalVolumeNames(context).toList()
    }
}
