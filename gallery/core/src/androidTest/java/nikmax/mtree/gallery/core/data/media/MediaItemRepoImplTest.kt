package nikmax.mtree.gallery.core.data.media

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nikmax.mtree.core.data.Resource
import org.junit.Before
import org.junit.Test

class MediaItemRepoImplTest {
    
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var repo: MediaItemsRepo
    private lateinit var ds: FakeMediastoreDs
    
    @Before
    fun setUp() = runTest {
        ds = FakeMediastoreDs()
        repo = MediaItemsRepoImpl(
            mediastoreDs = ds,
            context = context
        )
        repo.rescan()
    }
    
    @Test
    fun gallery_root_path_is_storage() {
        assert(repo.galleryRootPath == "/storage")
    }
    
    @Test
    fun for_PLAIN_mode__returns_all_albums_list() = runTest {
        val realResultFlow = repo.getMediaItemsFlow(
            path = null,
            searchQuery = null,
            treeMode = false
        )
        assert(realResultFlow.first() is Resource.Success)
        
        val realResultData = (realResultFlow.first() as Resource.Success).data
        val realResultItemPaths = realResultData.map { it.path }.toSet()
        val desiredResultPaths = ds.fakeFiles.map { it.path.substringBeforeLast('/') }.toSet()
        assert(realResultItemPaths == desiredResultPaths)
    }
    
    @Test
    fun for_PLAIN_mode_with_PATH_provided__returns_album_FILES() = runTest {
        val realResultFlow = repo.getMediaItemsFlow(
            path = "/storage/emulated/0/Pictures",
            searchQuery = null,
            treeMode = false
        )
        assert(realResultFlow.first() is Resource.Success)
        
        val realResultData = (realResultFlow.first() as Resource.Success).data
        val realResultItemPaths = realResultData.map { it.path }.toSet()
        val desiredResultPaths = ds.fakeFiles
            .filter { it.path.startsWith("/storage/emulated/0/Pictures") }
            .map { it.path }
            .toSet()
        assert(realResultItemPaths == desiredResultPaths)
    }
    
    @Test
    fun for_PLAIN_mode_SEARCH__returns_albums_containing_search_query() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = "gif",
                treeMode = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val realResultItemPaths = realResultData.map { it.path }.toSet()
            val desiredResultPaths = setOf(
                "/storage/emulated/0/Gifs",
            )
            assert(realResultItemPaths == desiredResultPaths)
        }
    }
    
    @Test
    fun for_PLAIN_mode_SEARCH_with_base_PATH__returns_albums_containing_base_PATH_and_search_QUERY() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = "gif",
                treeMode = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val realResultItemPaths = realResultData.map { it.path }.toSet()
            val desiredResultPaths = setOf(
                "/storage/emulated/0/Gifs",
            )
            assert(realResultItemPaths == desiredResultPaths)
        }
    }
    
    
    @Test
    fun for_TREE_mode__returns_root_albums() = runTest {
        val realResultFlow = repo.getMediaItemsFlow(
            path = null,
            searchQuery = null,
            treeMode = true
        )
        assert(realResultFlow.first() is Resource.Success)
        
        val realResultData = (realResultFlow.first() as Resource.Success).data
        val realResultItemPaths = realResultData.map { it.path }.toSet()
        val desiredResultPaths = ds.fakeFiles.map { it.path.substringBefore("/0/") }.toSet()
        assert(realResultItemPaths == desiredResultPaths)
    }
    
    
    @Test
    fun for_TREE_mode_with_PATH_provided__returns_album_own_FILES_and_child_ALBUMS() = runTest {
        val realResultFlow = repo.getMediaItemsFlow(
            path = "/storage/emulated/0",
            searchQuery = null,
            treeMode = true
        )
        assert(realResultFlow.first() is Resource.Success)
        
        val realResultData = (realResultFlow.first() as Resource.Success).data
        val realResultItemPaths = realResultData.map { it.path }.toSet()
        val desiredResultPaths = setOf(
            "/storage/emulated/0/Pictures",
            "/storage/emulated/0/Movies",
            "/storage/emulated/0/Gifs",
        )
        assert(realResultItemPaths == desiredResultPaths)
    }
    
    @Test
    fun for_TREE_mode_SEARCH__returns_ITEMS_containing_search_QUERY() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = "gif",
                treeMode = true
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val realResultItemPaths = realResultData.map { it.path }.toSet()
            val desiredResultPaths = setOf(
                "/storage/emulated/0/Gifs",
            )
            assert(realResultItemPaths == desiredResultPaths)
        }
    }
    
    @Test
    fun for_TREE_mode_SEARCH_with_base_PATH__returns_ITEMS_containing_base_PATH_and_search_QUERY() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = "gif",
                treeMode = true
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val realResultItemPaths = realResultData.map { it.path }.toSet()
            val desiredResultPaths = setOf(
                "/storage/emulated/0/Gifs",
            )
            assert(realResultItemPaths == desiredResultPaths)
        }
    }
    
    
    @Test
    fun IMAGES_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeImages = true,
                includeVideos = false,
                includeGifs = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val resultFiles = realResultData.filterIsInstance<MediaItemData.File>()
            val resultAlbums = realResultData.filterIsInstance<MediaItemData.Album>()
            assert(resultFiles.all { it.mediaType == MediaItemData.File.Type.IMAGE })
            assert(resultAlbums.all { it.imagesCount > 0 && it.videosCount == 0 && it.gifsCount == 0 })
        }
    }
    
    @Test
    fun VIDEOS_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeImages = false,
                includeVideos = true,
                includeGifs = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val resultFiles = realResultData.filterIsInstance<MediaItemData.File>()
            val resultAlbums = realResultData.filterIsInstance<MediaItemData.Album>()
            assert(resultFiles.all { it.mediaType == MediaItemData.File.Type.VIDEO })
            assert(resultAlbums.all { it.imagesCount == 0 && it.videosCount > 0 && it.gifsCount == 0 })
        }
    }
    
    
    @Test
    fun GIFS_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeImages = false,
                includeVideos = false,
                includeGifs = true
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            val resultFiles = realResultData.filterIsInstance<MediaItemData.File>()
            val resultAlbums = realResultData.filterIsInstance<MediaItemData.Album>()
            assert(resultFiles.all { it.mediaType == MediaItemData.File.Type.GIF })
            assert(resultAlbums.all { it.imagesCount == 0 && it.videosCount == 0 && it.gifsCount > 0 })
        }
    }
    
    @Test
    fun HIDDEN_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeHidden = true,
                includeUnhidden = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            assert(realResultData.all { it.isHidden })
        }
    }
    
    @Test
    fun UNHIDDEN_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeAlbums = true,
                includeHidden = false,
                includeUnhidden = true
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            assert(realResultData.all { !it.isHidden })
        }
    }
    
    @Test
    fun FILES_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeFiles = true,
                includeAlbums = false
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            assert(realResultData.all { it is MediaItemData.File })
        }
    }
    
    @Test
    fun ALBUMS_FILTER_works_correctly() {
        runTest {
            val realResultFlow = repo.getMediaItemsFlow(
                path = null,
                searchQuery = null,
                treeMode = false,
                includeFiles = false,
                includeAlbums = true
            )
            assert(realResultFlow.first() is Resource.Success)
            
            val realResultData = (realResultFlow.first() as Resource.Success).data
            assert(realResultData.all { it is MediaItemData.Album })
        }
    }
}
