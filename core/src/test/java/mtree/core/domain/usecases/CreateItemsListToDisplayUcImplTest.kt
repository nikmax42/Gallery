package mtree.core.domain.usecases

import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.Sort
import org.junit.Before
import org.junit.Test

class CreateItemsListToDisplayUcImplTest {
    
    private lateinit var uc: CreateItemsListToDisplayUcImpl
    
    @Before
    fun setup() {
        uc = CreateItemsListToDisplayUcImpl()
    }
    
    
    @Test
    fun `in plain mode, when both target path and search query is null - returns list of non-empty end-albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.albumsWithFiles.toSet()
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in plain mode, when target path is not null, but search query is null - returns list of album files`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.images.ownFiles.toSet()
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in plain mode, when target path is null, but search query is not null - returns list of albums contains search query`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = FakeData.images.name,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.images)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in plain mode, when both target path and search query is not null - returns list of files contains query`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = FakeData.image1.name,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.image1)
        assert(realResult == desiredResult)
    }
    
    
    
    @Test
    fun `in tree mode, when both target path and search query is null - returns gallery root albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.TREE,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.emulated)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in tree mode, when target path is not null, but search query is null - returns items placed inside target path`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.dcim.path,
            searchQuery = null,
            galleryMode = GalleryMode.TREE,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.albumsWithFiles.toSet()
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in tree mode, when target path is null, but search query is not null - returns search result for whole gallery`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = FakeData.image1.name,
            galleryMode = GalleryMode.TREE,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.emulated)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `in tree mode, when both target path and search query is not null - returns search result for target directory`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.dcim.path,
            searchQuery = FakeData.image1.name,
            galleryMode = GalleryMode.TREE,
            filters = Filters.includeAll(),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.images)
        assert(realResult == desiredResult)
    }
    
    
    
    @Test
    fun `if only images included - returns only image files and images-containing albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = true,
                includeVideos = false,
                includeGifs = false,
                includeUnhidden = true,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.images, FakeData.hiddenImages)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if only videos included - returns only video files and videos-containing albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = false,
                includeVideos = true,
                includeGifs = false,
                includeUnhidden = true,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.videos)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if only gifs included - returns only gif files and gifs-containing albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = false,
                includeVideos = false,
                includeGifs = true,
                includeUnhidden = true,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.gifs)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if all included - returns gif, image, video files and gif, image, video containing albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = true,
                includeVideos = true,
                includeGifs = true,
                includeUnhidden = true,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.albumsWithFiles.toSet()
        assert(realResult == desiredResult)
    }
    
    
    
    @Test
    fun `if only unhidden included - returns only unhidden files and albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = true,
                includeVideos = true,
                includeGifs = true,
                includeUnhidden = true,
                includeHidden = false
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.albumsWithFiles.toSet() - FakeData.hiddenImages
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if only hidden included - returns only hidden files and albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = true,
                includeVideos = true,
                includeGifs = true,
                includeUnhidden = false,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = setOf(FakeData.hiddenImages)
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if both hidden and unhidden included - returns both hidden and unhidden items`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters(
                includeAlbums = true,
                includeFiles = true,
                includeImages = true,
                includeVideos = true,
                includeGifs = true,
                includeUnhidden = true,
                includeHidden = true
            ),
            sort = Sort.byNameAscendWithoutPlacing()
        ).toSet()
        
        val desiredResult = FakeData.albumsWithFiles.toSet()
        assert(realResult == desiredResult)
    }
    
    
    
    @Test
    fun `if sorting by name - returns items sorted by name`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.NAME,
                descend = false,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.albumsWithFiles.sortedBy { it.name }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if descend sorting by name - returns items descend sorted by name`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = null,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.NAME,
                descend = true,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.albumsWithFiles.sortedByDescending { it.name }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if sorting by extension - returns files sorted by extension and unsorted albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.EXTENSION,
                descend = false,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedBy { it.extension }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if descend sorting by extension - returns files descend sorted by extension and unsorted albums`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.EXTENSION,
                descend = true,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedByDescending { it.extension }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if sorting by creation date - returns items sorted by creation date`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.CREATION_DATE,
                descend = false,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedBy { it.creationDate }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if descend sorting by creation date - returns items descend sorted by creation date`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.CREATION_DATE,
                descend = true,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedByDescending { it.creationDate }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if sorting by modification date - returns items sorted by modification date`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.MODIFICATION_DATE,
                descend = false,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedBy { it.modificationDate }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if descend sorting by modification date - returns items descend sorted by modification date`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.MODIFICATION_DATE,
                descend = true,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedByDescending { it.modificationDate }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if sorting by size - returns items sorted by size`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.SIZE,
                descend = false,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedBy { it.nestedMediaSize }
        assert(realResult == desiredResult)
    }
    
    @Test
    fun `if descend sorting by size - returns items descend sorted by size`() {
        val realResult = uc.execute(
            galleryAlbums = FakeData.fakeAlbums,
            basePath = FakeData.images.path,
            searchQuery = null,
            galleryMode = GalleryMode.PLAIN,
            filters = Filters.includeAll(),
            sort = Sort(
                order = Sort.Order.SIZE,
                descend = true,
                placeFirst = Sort.PlaceFirst.NONE
            )
        )
        
        val desiredResult = FakeData.images.ownFiles.sortedByDescending { it.nestedMediaSize }
        assert(realResult == desiredResult)
    }
}
