package nikmax.gallery.gallery.core

import nikmax.gallery.gallery.core.data.media.MediaFileData
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.createItemsListToDisplay
import org.junit.Test
import kotlin.io.path.Path
import kotlin.io.path.pathString


class ItemsUtilsTest {

    private val storageRootFile = MediaFileData(path = "/storage/emulated/0/root-file.png")
    private val storageMovie1 = MediaFileData(path = "/storage/emulated/0/Movies/first.mp4")
    private val storageMovie2 = MediaFileData(path = "/storage/emulated/0/Movies/second.mp4")
    private val storageDeepMovie = MediaFileData(path = "/storage/emulated/0/Movies/EmptyAlbum/DeepAlbum/Deep.mp4")
    private val storageDeepImage = MediaFileData(
        path = "/storage/emulated/0/SomeDirectory/DeepDir/Deep_File_Without_Intermediate_MediaAlbums.png"
    )
    private val storageGif = MediaFileData(path = "/storage/emulated/0/Gifs/gif.gif")

    private val sdCardRootImage = MediaFileData(path = "/storage/1712-4219/sdcard-image.jpg")
    private val sdCardPicture1 = MediaFileData(path = "/storage/1712-4219/Pictures/sd-picture1.png")

    private val files = listOf(
        storageRootFile,
        storageMovie1,
        storageMovie2,
        storageDeepMovie,
        storageDeepImage,
        storageGif,
        sdCardRootImage,
        sdCardPicture1
    )

    @Test
    fun `returns correct result in plain albums mode when target album == null`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            treeModeEnabled = false
        )
        val desiredResult = listOf(
            MediaItemUI.Album(path = Path(storageRootFile.path).parent.pathString),
            MediaItemUI.Album(path = Path(storageMovie1.path).parent.pathString),
            MediaItemUI.Album(path = Path(storageDeepMovie.path).parent.pathString),
            MediaItemUI.Album(path = Path(storageDeepImage.path).parent.pathString),
            MediaItemUI.Album(path = Path(storageGif.path).parent.pathString),
            MediaItemUI.Album(path = Path(sdCardRootImage.path).parent.pathString),
            MediaItemUI.Album(path = Path(sdCardPicture1.path).parent.pathString),
        )
        val realPaths = realResult.map { it.path }.toHashSet()
        val desiredPaths = desiredResult.map { it.path }.toHashSet()
        assert(realPaths == desiredPaths)
    }

    @Test
    fun `returns correct result in tree albums mode when target album == null`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            treeModeEnabled = true
        )
        val desiredResult = listOf(
            MediaItemUI.Album("/storage/emulated/0"),
            MediaItemUI.Album("/storage/1712-4219")
        )
        assert(realResult.map { it.path } == desiredResult.map { it.path })
    }

    @Test
    fun `returns correct album content in plain mode`() {
        val realResultForMovies = files.createItemsListToDisplay(
            targetAlbumPath = Path(storageMovie1.path).parent.pathString,
            treeModeEnabled = false
        )
        val desiredResultForMovies = listOf(
            MediaItemUI.File(storageMovie1.path),
            MediaItemUI.File(storageMovie2.path)
        )
        assert(realResultForMovies.map { it.path } == desiredResultForMovies.map { it.path })
    }

    @Test
    fun `returns correct album content in tree mode`() {
        val realResultForMovies = files.createItemsListToDisplay(
            targetAlbumPath = Path(storageMovie1.path).parent.pathString,
            treeModeEnabled = true
        )
        val desiredResultForMovies = listOf(
            MediaItemUI.File(storageMovie1.path),
            MediaItemUI.File(storageMovie2.path),
            MediaItemUI.Album(Path(storageDeepMovie.path).parent.pathString)
        )
        assert(realResultForMovies.map { it.path }.toSet() == desiredResultForMovies.map { it.path }.toSet())
    }

    @Test
    fun `returns correct result in tree albums mode when target path != null`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = "/storage/emulated/0",
            treeModeEnabled = true,
        )
        val desiredResult = listOf(
            MediaItemUI.Album(Path(storageMovie1.path).parent.pathString),
            MediaItemUI.Album(Path(storageDeepImage.path).parent.pathString),
            MediaItemUI.Album(Path(storageGif.path).parent.pathString),
            MediaItemUI.File(storageRootFile.path)
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `images filtering returns images files or albums containing images`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            includeImages = true,
            includeVideos = false,
            includeGifs = false,
            includeHidden = true,
            includeFiles = false
        )

        fun MediaItemUI.isImageFile(): Boolean {
            return this is MediaItemUI.File && this.mediaType == MediaItemUI.File.MediaType.IMAGE
        }

        fun MediaItemUI.containsOnlyImages(): Boolean {
            return this is MediaItemUI.Album && this.imagesCount > 0 /* && this.videosCount == 0 && this.gifsCount == 0 */ // uncomment to test strict filtering
        }

        assert(
            realResult.all {
                it.isImageFile() || it.containsOnlyImages()
            }
        )
    }

    @Test
    fun `videos filtering works correctly`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            includeImages = false,
            includeVideos = true,
            includeGifs = false,
            includeHidden = true,
            includeFiles = false
        )

        fun MediaItemUI.isVideoFile(): Boolean {
            return this is MediaItemUI.File && this.mediaType == MediaItemUI.File.MediaType.VIDEO
        }

        fun MediaItemUI.containsOnlyVideos(): Boolean {
            return this is MediaItemUI.Album && this.videosCount > 0 /* && this.imagesCount == 0 && this.gifsCount == 0 */
        }

        assert(
            realResult.all {
                it.isVideoFile() || it.containsOnlyVideos()
            }
        )
    }

    @Test
    fun `gifs filtering works correctly`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            includeImages = false,
            includeVideos = false,
            includeGifs = true,
            includeHidden = true,
            includeFiles = false
        )

        fun MediaItemUI.isGifFile(): Boolean {
            return this is MediaItemUI.File && this.mediaType == MediaItemUI.File.MediaType.GIF
        }

        fun MediaItemUI.containsOnlyGifs(): Boolean {
            return this is MediaItemUI.Album && this.gifsCount > 0 /* && this.imagesCount == 0 && this.videosCount == 0 */
        }

        assert(
            realResult.all {
                it.isGifFile() || it.containsOnlyGifs()
            }
        )
    }

    @Test
    fun `search in plain mode returns plain lists of albums whose files paths contains search query`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            treeModeEnabled = false,
            searchQuery = "movies",
            includeHidden = true
        )
        val desiredResult = listOf(
            MediaItemUI.Album(Path(storageMovie1.path).parent.pathString),
            MediaItemUI.Album(Path(storageDeepMovie.path).parent.pathString),
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `search in tree mode returns nested lists of albums whose files paths contains search query`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = null,
            searchQuery = "movies",
            treeModeEnabled = true,
            includeHidden = true
        )
        val desiredResult = listOf(
            MediaItemUI.Album(Path(storageMovie1.path).parent.pathString),
        )
        assert(realResult.map { it.path }.toSet() == desiredResult.map { it.path }.toSet())
    }

    @Test
    fun `albums on top works fine`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = Path(storageMovie1.path).parent.pathString,
            showAlbumsFirst = true
        )
        val desiredResult = listOf(
            MediaItemUI.Album(Path(storageDeepMovie.path).parent.pathString),
            MediaItemUI.File(storageMovie1.path),
            MediaItemUI.File(storageMovie2.path)
        )
        assert(realResult.map { it.path } == desiredResult.map { it.path })
    }

    @Test
    fun `files on top works fine`() {
        val realResult = files.createItemsListToDisplay(
            targetAlbumPath = Path(storageMovie1.path).parent.pathString,
            showFilesFirst = true
        )
        val desiredResult = listOf(
            MediaItemUI.File(storageMovie1.path),
            MediaItemUI.File(storageMovie2.path),
            MediaItemUI.Album(Path(storageDeepMovie.path).parent.pathString)
        )
        assert(realResult.map { it.path } == desiredResult.map { it.path })
    }
}
