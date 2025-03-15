package nikmax.gallery.core.data.media

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
    fun getAllImagesAndVideos(context: Context): List<MediaFileData> {
        return getAllImages(context) + getAllVideos(context)
    }

    // contains example of correct projection and selection to get all images and videos INCLUDING hidden ones
    private fun getAllImagesAndVideos_OLD(context: Context): MutableList<MediaFileData> {
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

    private fun getAllImages(context: Context): MutableList<MediaFileData> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.VOLUME_NAME,
            MediaStore.Images.Media.SIZE
        )
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
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

    private fun getAllVideos(context: Context): MutableList<MediaFileData> {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.VOLUME_NAME,
            MediaStore.Video.Media.SIZE
        )
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
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

    /**
     * Creates a [MediaFileData] object from a mediastore cursor.
     *
     * @param cursor the cursor to create the [MediaFileData] object from.
     * @return a [MediaFileData] object.
     */
    private fun createMediaItemData(cursor: Cursor): MediaFileData {
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
        val volumeName = cursor.getString(
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.VOLUME_NAME)
        )

        return MediaFileData(
            path = data,
            uri = contentUri.toString(),
            size = size,
            dateCreated = dateAdded,
            dateModified = dateModified,
            volume = if (volumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY) MediaFileData.Volume.PRIMARY
            else MediaFileData.Volume.SECONDARY
        )
    }
}
