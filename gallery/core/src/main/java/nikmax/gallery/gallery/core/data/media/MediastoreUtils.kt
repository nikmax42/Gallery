package nikmax.gallery.gallery.core.data.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore


/**
 * Utilities for retrieving media files from the [MediaStore].
 */
internal object MediastoreUtils {
    
    /**
     * Retrieves a list of images and videos (including placed in hidden .directories) from the mediastore.
     *
     * @return a list of [MediaFileData] objects.
     */
    fun getAllImagesAndVideos(context: Context): List<MediaItemData.File> {
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
            MediaStore.MediaColumns.DURATION
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
    
    /**
     * Creates a [MediaFileData] object from a mediastore cursor.
     *
     * @param cursor the cursor to create the [MediaFileData] object from.
     * @return a [MediaFileData] object.
     */
    private fun createMediaFileData(cursor: Cursor): MediaItemData.File {
        val data = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        )
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
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
        val volumeName = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.VOLUME_NAME)
        )
        
        return MediaItemData.File(
            path = data,
            uri = contentUri.toString(),
            size = size,
            duration = duration,
            dateCreated = dateAdded,
            dateModified = dateModified,
        )
    }
    
}
