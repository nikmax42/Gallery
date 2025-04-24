package mtree.core.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

interface MediastoreDs {
    fun getImagesAndVideos(): List<MediaItemData.File>
}


class MediastoreDsImpl(private val context: Context) : MediastoreDs {
    
    override fun getImagesAndVideos(): List<MediaItemData.File> {
        return getAllImageAndVideoFiles(context)
    }
    
    // get all images and videos INCLUDING hidden ones
    private fun getAllImageAndVideoFiles(context: Context): MutableList<MediaItemData.File> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.VOLUME_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.MIME_TYPE
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
        val filesData = mutableListOf<MediaItemData.File>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val fileData = createMediaFileData(cursor)
                filesData.add(fileData)
            }
            cursor.close()
        }
        return filesData
    }
    
    private fun createMediaFileData(cursor: Cursor): MediaItemData.File {
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
        val duration = cursor.getLong(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION)
        )
        val mimeType = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        )
        
        val id = cursor.getLong(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        )
        val startsWith =
            if (mimeType.startsWith("video/"))
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentUri = ContentUris.withAppendedId(
            startsWith,
            id
        )
        
        return MediaItemData.File(
            path = data,
            uri = contentUri.toString(),
            size = size,
            duration = duration,
            creationDate = dateAdded,
            modificationDate = dateModified,
            thumbnail = data,
            mimeType = mimeType
        )
    }
}
