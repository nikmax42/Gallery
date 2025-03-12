package nikmax.gallery.core

import nikmax.gallery.data.media.MediaFileData
import org.junit.Before

class MappersTest {
    private lateinit var image1: MediaFileData
    private lateinit var video1: MediaFileData

    @Before
    fun setup() {
        image1 = MediaFileData(
            path = "/image1.png",
            size = 123,
            dateCreated = 1733438400,
            dateModified = 1733438400,
            volume = MediaFileData.Volume.PRIMARY,
        )
        video1 = MediaFileData(
            path = "/video1.mp4",
            size = 456,
            dateCreated = 1643928000,
            dateModified = 1675528920,
            volume = MediaFileData.Volume.PRIMARY,
        )
    }

    /*  @Test
     fun `map to file returns correct ui model`() {
         val image1CorrectResult = MediaItemUI.File(
             path = image1.path,
             name = image1.name,
             size = image1.size,
             dateCreated = image1.dateCreated,
             dateModified = image1.dateModified,
             volume = when (image1.volume) {
                 MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                 MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
             },
             mimetype = image1.mimetype!!,
             thumbnail = image1.path
         )
         val image1RealResult = MediaItemDataToUiMapper.mapToFile(image1)
         assert(image1RealResult == image1CorrectResult)

         val video1CorrectResult = MediaItemUI.File(
             path = video1.path,
             name = video1.name,
             size = video1.size,
             dateCreated = video1.dateCreated,
             dateModified = video1.dateModified,
             volume = when (video1.volume) {
                 MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                 MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
             },
             mimetype = video1.mimetype!!,
             thumbnail = video1.path
         )
         val video1RealResult = MediaItemDataToUiMapper.mapToFile(video1)
         assert(video1RealResult == video1CorrectResult)
     }

     @Test
     fun `map to album returns correct ui model`() {
         val files = listOf(image1, video1)
         val albumCorrectResult = MediaItemUI.Album(
             path = Path(files.first().path).parent.pathString,
             name = Path(files.first().path).parent.name,
             size = files.sumOf { it.size },
             filesCount = files.size,
             dateCreated = files.minOf { it.dateCreated },
             dateModified = files.maxOf { it.dateModified },
             volume = MediaItemUI.Volume.PRIMARY, // Assuming the first file's volume
             thumbnail = files.minBy { it.dateCreated }.path
         )
         val albumRealResult = MediaItemDataToUiMapper.mapToAlbum(files)
         assert(albumRealResult == albumCorrectResult)
     } */
}
