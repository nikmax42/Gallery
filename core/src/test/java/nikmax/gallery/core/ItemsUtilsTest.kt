/*
package nikmax.gallery.core

import nikmax.gallery.core.ItemsUtils.createItemsListToDisplay
import nikmax.gallery.core.data.media.MediaFileData
import nikmax.gallery.core.data.preferences.OLDGalleryPreferences
import nikmax.gallery.core.ui.MediaItemUI
import org.junit.Test

class ItemsUtilsTest {

    private val storageRootFile = MediaFileData(path = "/storage/emulated/0/root-file.png")
    private val storageMovie1 = MediaFileData(path = "/storage/emulated/0/Movies/first.mp4")
    private val storageMovie2 = MediaFileData(path = "/storage/emulated/0/Movies/second.mp4")
    private val deepMovie = MediaFileData(path = "/storage/emulated/0/Movies/EmptyAlbum/DeepAlbum/Deep.mp4")
    private val deepStorageImage =
        MediaFileData(path = "/storage/emulated/0/SomeDirectory/DeepDir/Deep_File_Without_Intermediate_MediaAlbums.png")
    private val sdCardRootImage = MediaFileData(path = "/storage/1712-4219/sdcard-image.jpg")
    private val sdCardPicture1 = MediaFileData(path = "/storage/1712-4219/Pictures/sd-picture1.png")

    private val files = listOf(
        storageRootFile,
        storageMovie1,
        storageMovie2,
        deepMovie,
        deepStorageImage,
        sdCardRootImage,
        sdCardPicture1
    )

    @Test
    fun `returns correct plain albums list when target album is null and plain albums argument provided`(): Unit {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album(path = "/storage/emulated/0"),
            MediaItemUI.Album(path = "/storage/emulated/0/Movies"),
            MediaItemUI.Album(path = "/storage/emulated/0/Movies/EmptyAlbum/DeepAlbum"),
            MediaItemUI.Album(path = "/storage/emulated/0/SomeDirectory/DeepDir"),
            MediaItemUI.Album(path = "/storage/1712-4219"),
            MediaItemUI.Album(path = "/storage/1712-4219/Pictures"),
        )
        val realPaths = realResult.map { it.path }.toHashSet()
        val desiredPaths = desiredResult.map { it.path }.toHashSet()
        assert(realPaths == desiredPaths)
    }

    @Test
    fun `returns correct album own files list when target album != null`(): Unit {
        val realResultFor0 = files.createItemsListToDisplay(
            targetAlbumPath = "/storage/emulated/0",
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResultFor0 = listOf(storageRootFile)
        assert(realResultFor0.map { it.path } == desiredResultFor0.map { it.path })

        val realResultForMovies = files.createItemsListToDisplay(
            targetAlbumPath = "/storage/emulated/0/Movies",
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResultForMovies = listOf(storageMovie1, storageMovie2)
        assert(realResultForMovies.map { it.path } == desiredResultForMovies.map { it.path })
    }

    @Test
    fun `returns correct nested albums list when target path == null`(): Unit {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.NESTED,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0"),
            MediaItemUI.Album("/storage/1712-4219")
        )
        assert(realResult.map { it.path } == desiredResult.map { it.path })
    }

    @Test
    fun `returns correct nested albums list when target path != null`(): Unit {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = "/storage/emulated/0",
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.NESTED,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0/SomeDirectory/DeepDir"),
            MediaItemUI.Album("/storage/emulated/0/Movies"),
            MediaItemUI.File("/storage/emulated/0/root-file.png")
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `sorting works correctly`(): Unit {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = true,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/1712-4219/Pictures"),
            MediaItemUI.Album("/storage/emulated/0/Movies"),
            MediaItemUI.Album("/storage/emulated/0/SomeDirectory/DeepDir"),
            MediaItemUI.Album("/storage/emulated/0/Movies/EmptyAlbum/DeepAlbum"),
            MediaItemUI.Album("/storage/1712-4219"),
            MediaItemUI.Album("/storage/emulated/0"),
        )
        assert(realResult.map { it.path } == desiredResult.map { it.path })
    }

    @Test
    fun `filtering works correctly`(): Unit {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = setOf(OLDGalleryPreferences.Filter.IMAGES),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0"),
            MediaItemUI.Album("/storage/1712-4219"),
            MediaItemUI.Album("/storage/emulated/0/SomeDirectory/DeepDir"),
            MediaItemUI.File("/storage/1712-4219/Pictures")
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `when search query != null and plain albums mode enabled, returns plain lists of albums whose files paths contains search query`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.PLAIN,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false,
            searchQuery = "movies"
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0/Movies"),
            MediaItemUI.Album("/storage/emulated/0/Movies/EmptyAlbum/DeepAlbum"),
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `when search query != null and nested albums mode enabled, returns nested lists of albums whose files paths contains search query`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            nestedAlbumsEnabled = OLDGalleryPreferences.AlbumsMode.NESTED,
            appliedFilters = OLDGalleryPreferences.Filter.entries.toSet(),
            sortingOrder = OLDGalleryPreferences.SortingOrder.NAME,
            useDescendSorting = false,
            includeHidden = true,
            includeFilesOnly = false,
            searchQuery = "movies"
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0/Movies")
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }
}
*/
