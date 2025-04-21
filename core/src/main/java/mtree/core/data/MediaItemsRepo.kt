package mtree.core.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.util.fastFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface MediaItemsRepo {
    
    fun getMediaAlbumsFlow(): Flow<Resource<List<MediaItemData>>>
    
    /**
     * Use mediastore to update files flow with all available media files.
     */
    suspend fun rescan()
}


internal class MediaItemsRepoImpl(
    private val mediastoreDs: MediastoreDs,
    private val context: Context
) : MediaItemsRepo {
    
    private val _loadingFlow = MutableStateFlow(true)
    private val _albumsFlow = MutableStateFlow<List<MediaItemData.Album>>(emptyList())
    
    override fun getMediaAlbumsFlow(): Flow<Resource<List<MediaItemData>>> {
        return combine(_loadingFlow, _albumsFlow) { loading, albums ->
            when (loading) {
                true -> Resource.Loading(albums)
                false -> Resource.Success(albums)
            }
        }
    }
    
    override suspend fun rescan() {
        val startTime = System.currentTimeMillis()
        Timber.d("Rescan initiated")
        _loadingFlow.update { true }
        withContext(Dispatchers.IO) {
            val galleryData = mediastoreDs
                .getImagesAndVideos()
                .createGalleryData()
            withContext(Dispatchers.Main) {
                _albumsFlow.update { galleryData }
                _loadingFlow.update { false }
            }
        }
        val endTime = System.currentTimeMillis()
        Timber.d("Rescan finished. Took ${endTime - startTime} ms")
    }
    
    
    companion object {
        /**
         * Converts list of [MediaItemData.File] obtained from [android.provider.MediaStore]
         * to list of [MediaItemData.Album] filled with metadata.
         *
         * Creates intermediate albums without own files but with metadata to build ersatz-filesystem for tree mode.
         * */
        @VisibleForTesting
        internal fun List<MediaItemData.File>.createGalleryData(): List<MediaItemData.Album> {
            val albums = mutableMapOf<String, MediaItemData.Album>()
            
            //create empty albums from path
            this
                .groupBy { Path(it.path).parent.pathString }
                .forEach { albumGroup ->
                    albums.put(
                        albumGroup.key,
                        MediaItemData.Album.createEmpty(
                            path = albumGroup.key,
                            files = albumGroup.value
                        )
                    )
                    val parentDirectories = mutableListOf<String>()
                    var node = Path(albumGroup.key).parent.pathString
                    while (node != "null") {
                        parentDirectories.add(node)
                        node = Path(node).parent?.toString() ?: "null"
                    }
                    parentDirectories.forEach { albumPath ->
                        albums.put(
                            albumPath,
                            MediaItemData.Album.createEmpty(
                                path = albumPath,
                                files = emptyList()
                            )
                        )
                    }
                }
            
            //fill albums with metadata
            albums.values.forEach { album ->
                val albumOwnFiles = album.ownFiles
                //files placed in nested albums
                val albumDeepFiles = this.fastFilter { it.path.startsWith(album.path) } - albumOwnFiles
                //size of whole directory (including nested directories)
                val albumSize = (albumOwnFiles + albumDeepFiles).sumOf { it.size }
                //count of nested images (include placed nested albums)
                val imagesCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.IMAGE }
                val videosCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.VIDEO }
                val gifsCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.GIF }
                //count of child deep albums
                val nestedAlbumsCount = albums.values.count { it.path.startsWith(album.path) && it.path != album.path }
                val hiddenCount = (albumOwnFiles + albumDeepFiles).count { it.isHidden }
                val unhiddenCount = (albumOwnFiles + albumDeepFiles).count { !it.isHidden }
                val creationDate = (albumOwnFiles + albumDeepFiles).minOfOrNull { it.creationDate } ?: 0
                val modificationDate = (albumOwnFiles + albumDeepFiles).maxOfOrNull { it.modificationDate } ?: 0
                //hidden children excluded from thumbnail calculation for "safety reasons"
                val thumbnail = albumOwnFiles.firstOrNull()?.path
                    ?: albumDeepFiles.filterNot { it.isHidden }.firstOrNull()?.path
                
                albums.put(
                    album.path,
                    MediaItemData.Album(
                        path = album.path,
                        ownFiles = albumOwnFiles,
                        nestedMediaSize = albumSize,
                        nestedImagesCount = imagesCount,
                        nestedVideosCount = videosCount,
                        nestedGifsCount = gifsCount,
                        nestedHiddenMediaCount = hiddenCount,
                        nestedUnhiddenMediaCount = unhiddenCount,
                        creationDate = creationDate,
                        modificationDate = modificationDate,
                        nestedAlbumsCount = nestedAlbumsCount,
                        thumbnailPath = thumbnail
                    )
                )
            }
            return albums.values.toList()
        }
    }
}
