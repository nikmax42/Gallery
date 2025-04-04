package nikmax.gallery.gallery.core.data.media

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nikmax.gallery.gallery.core.data.media.MediaItemRepoImpl.Companion.applyItemTypeFilters
import nikmax.gallery.gallery.core.data.media.MediaItemRepoImpl.Companion.applyMediaTypeFilters
import nikmax.gallery.gallery.core.data.media.MediaItemRepoImpl.Companion.applyVisibilityFilters
import nikmax.gallery.gallery.core.data.media.MediaItemRepoImpl.Companion.createGalleryAlbumsList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaItemRepoImplTest {
    
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val repo = MediaItemRepoImpl(context)
    
    private val picture1 = MediaItemData.File(
        path = "/storage/emulated/0/Pictures/1.jpg"
    )
    private val movie1 = MediaItemData.File(
        path = "/storage/emulated/0/Movies/1.mp4"
    )
    private val sdcard_picture1 = MediaItemData.File(
        path = "/storage/ABCD-1234/Pictures/1.jpg"
    )
    
    private val files = listOf(picture1, movie1, sdcard_picture1)
    
    private lateinit var galleryAlbums: List<MediaItemData.Album>
    
    @Before
    fun setup() {
        galleryAlbums = files.createGalleryAlbumsList()
    }
    
    
    @Test
    fun list_of_files_converts_to_correct_list_of_albums() {
        val realResult = files.createGalleryAlbumsList()
        val desiredResult = listOf(
            MediaItemData.Album("/", emptyList()),
            MediaItemData.Album("/storage", emptyList()),
            MediaItemData.Album("/storage/emulated", emptyList()),
            MediaItemData.Album("/storage/emulated/0", emptyList()),
            MediaItemData.Album("/storage/emulated/0/Pictures", listOf(picture1)),
            MediaItemData.Album("/storage/emulated/0/Movies", listOf(movie1)),
            MediaItemData.Album("/storage/ABCD-1234", emptyList()),
            MediaItemData.Album("/storage/ABCD-1234/Pictures", listOf(sdcard_picture1))
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
        assert(realResult.sumOf { it.files.size } == desiredResult.sumOf { it.files.size })
    }
    
    @Test
    fun in_tree_mode_when_target_path_is_not_null__returns_album_own_files_and_nested_directories() {
        val realResult = MediaItemRepoImpl.getDirectoryContent(
            directoryPath = "/storage/emulated/0",
            galleryAlbums = galleryAlbums
        )
        val desiredResult = listOf(
            MediaItemData.Album("/storage/emulated/0/Pictures", listOf(picture1)),
            MediaItemData.Album("/storage/emulated/0/Movies", listOf(movie1))
        )
        assert(realResult.map { it.path }.toHashSet() == desiredResult.map { it.path }.toHashSet())
        assert(realResult.sumOf { it.size } == desiredResult.sumOf { it.size })
    }
    
    @Test
    fun in_tree_mode_when_path_is_gallery_root__returns_gallery_root_album_content() {
        val realResult = MediaItemRepoImpl.getDirectoryContent(
            directoryPath = "/storage",
            galleryAlbums = galleryAlbums
        )
        val desiredResult = listOf(
            MediaItemData.Album("/storage/emulated", emptyList()),
            MediaItemData.Album("/storage/ABCD-1234", emptyList())
        )
        assert(realResult.map { it.path }.toHashSet() == desiredResult.map { it.path }.toHashSet())
        assert(realResult.sumOf { it.size } == desiredResult.sumOf { it.size })
    }
    
    @Test
    fun in_plain_mode_when_target_path_is_null__returns_all_non_empty_albums() {
        val realResult = MediaItemRepoImpl.getFlatListOfAllGalleryNotEmptyAlbums(galleryAlbums)
        val desiredResult = galleryAlbums.filterNot { it.files.isEmpty() }
        assert(realResult.all { it.files.isNotEmpty() })
        assert(realResult.map { it.path }.toHashSet() == desiredResult.map { it.path }.toHashSet())
        assert(realResult.sumOf { it.size } == desiredResult.sumOf { it.size })
    }
    
    @Test
    fun in_plain_mode_when_target_path_is_not_null__returns_album_own_files() {
        val realResult = MediaItemRepoImpl.getDirectoryContent(
            directoryPath = "/storage/emulated/0/Pictures",
            galleryAlbums = galleryAlbums
        )
        val desiredResult = listOf(picture1)
        assert(realResult == desiredResult)
    }
    
    /*   @Test
      fun search_result_contains_albums_and_files_whose_paths_contains_query() {
          val realResult = repo.getSearchResultFlow(query = "1.mp4")
          val desiredResult = listOf(
              MediaItemData.Album("/storage/emulated/0/Movies", listOf(movie1)),
              movie1
          )
          assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
          assert(realResult.sumOf { it.size } == desiredResult.sumOf { it.size })
      } */
    
    @Test
    fun itemTypeFiltering_correct() {
        val albumsOnlyResult = galleryAlbums.applyItemTypeFilters(includeAlbums = true, includeFiles = false)
        assert(albumsOnlyResult.all { it is MediaItemData.Album })
        
        val filesOnlyResult = galleryAlbums.applyItemTypeFilters(includeAlbums = false, includeFiles = true)
        assert(filesOnlyResult.all { it is MediaItemData.File })
    }
    
    @Test
    fun mediaTypeFiltering_correct() {
        val imagesOnlyResult = galleryAlbums.applyMediaTypeFilters(
            includeImages = true, includeVideos = false, includeGifs = false
        )
        assert(
            imagesOnlyResult.all {
                it is MediaItemData.File && it.mediaType == MediaItemData.File.Type.IMAGE ||
                        it is MediaItemData.Album && it.imagesCount > 0
            }
        )
        
        val videosOnlyResult = galleryAlbums.applyMediaTypeFilters(
            includeImages = false, includeVideos = true, includeGifs = false
        )
        assert(
            videosOnlyResult.all {
                it is MediaItemData.File && it.mediaType == MediaItemData.File.Type.VIDEO ||
                        it is MediaItemData.Album && it.videosCount > 0
            }
        )
        
        val gifsOnlyResult = galleryAlbums.applyMediaTypeFilters(
            includeImages = false, includeVideos = false, includeGifs = true
        )
        assert(
            gifsOnlyResult.all {
                it is MediaItemData.File && it.mediaType == MediaItemData.File.Type.GIF ||
                        it is MediaItemData.Album && it.gifsCount > 0
            }
        )
    }
    
    @Test
    fun visibilityFiltering_correct() {
        val unhiddenOnlyResult = galleryAlbums.applyVisibilityFilters(includeUnhidden = true, includeHidden = false)
        assert(unhiddenOnlyResult.all { !it.isHidden })
        
        val hiddenOnlyResult = galleryAlbums.applyVisibilityFilters(includeUnhidden = false, includeHidden = true)
        assert(hiddenOnlyResult.all { it.isHidden })
    }
}
