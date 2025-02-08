package nikmax.gallery.data

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import nikmax.gallery.data.media.MediaFileData
import nikmax.gallery.data.media.MediaItemRepoImpl
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.media.MediaStoreDs
import org.junit.Before
import org.junit.Test
import kotlin.io.path.Path
import kotlin.io.path.pathString

class MediaItemRepoImplTest {

    class FakeMediaStoreDs(private val fakeFiles: List<MediaFileData>) : MediaStoreDs {
        private var _files = fakeFiles

        override fun rescanFiles() {
            Thread.sleep(3000)
            _files = fakeFiles
        }

        override fun getFiles(): List<MediaFileData> {
            return _files
        }
    }

    private val fakeFiles = listOf(
        MediaFileData(
            path = "/album1/file1.png",
            size = 1024L,
            dateCreated = 1633036800L,
            dateModified = 1633123200L,
            volume = MediaFileData.Volume.PRIMARY
        ),
        MediaFileData(
            path = "/album1/file2.jpg",
            size = 2048L,
            dateCreated = 1633113600L,
            dateModified = 1633200000L,
            volume = MediaFileData.Volume.PRIMARY
        ),
        MediaFileData(
            path = "/album1/file3.mp4",
            size = 4096L,
            dateCreated = 1633190400L,
            dateModified = 1633276800L,
            volume = MediaFileData.Volume.PRIMARY
        ),
        MediaFileData(
            path = "/album2/file1.jpg",
            size = 2048L,
            dateCreated = 1632950400L,
            dateModified = 1633036800L,
            volume = MediaFileData.Volume.SECONDARY
        )
    )

    private lateinit var repo: MediaItemsRepo

    @Before
    fun setup() {
        val ds = FakeMediaStoreDs(fakeFiles)
        repo = MediaItemRepoImpl(ds)
    }

    /*    @Test
       fun `rescan emits loading`() = runTest {
           repo.getMediaItems().test {
               // assert(awaitItem() !is Resource.Loading)
               repo.rescan()
               assert(awaitItem() is Resource.Loading)
               assert(awaitItem() !is Resource.Loading)
           }
       } */

    @Test
    fun `filtering with empty params returns all files`() = runTest {
        val correctResult = Resource.Success(fakeFiles)
        repo.rescan()
        repo.getFilesPlacedOnPath().test {
            val realResult = awaitItem()
            assert(realResult is Resource.Success && realResult == correctResult)
        }
    }

    @Test
    fun `filtering with full file path returns only that file`() = runTest {
        val correctResult = Resource.Success(listOf(fakeFiles.first()))
        repo.rescan()
        repo.getFilesPlacedOnPath(fakeFiles.first().path).test {
            val realResult = awaitItem()
            assert(realResult == correctResult)
        }
    }

    @Test
    fun `filtering with directory path returns only this file direct child files`() = runTest {
        val directoryPath = "/album2"
        val correctResult = Resource.Success(
            fakeFiles.filter {
                Path(it.path).parent.pathString == directoryPath
            }
        )
        repo.rescan()
        repo.getFilesPlacedOnPath(directoryPath).test {
            val realResult = awaitItem()
            assert(realResult == correctResult)
        }
    }

    @Test
    fun `path containing filtering returns list of file which paths contains given path`() {
        runTest {
            repo.rescan()
            val path = "album1"
            val correctResult = Resource.Success(
                fakeFiles.filter { it.path.contains(path) }
            )
            repo.getFilesContainsPath(path).test {
                val realResult = awaitItem()
                assert(realResult == correctResult)
            }
        }
    }

}
