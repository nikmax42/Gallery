package nikmax.mtree.gallery.core.domain.usecases

import nikmax.mtree.gallery.core.domain.models.MediaItemDomain
import kotlin.io.path.Path
import kotlin.io.path.pathString


internal object FakeData {
    lateinit var image1: MediaItemDomain.File
    lateinit var image2: MediaItemDomain.File
    lateinit var video: MediaItemDomain.File
    lateinit var gif: MediaItemDomain.File
    lateinit var hiddenImage: MediaItemDomain.File
    lateinit var fakeFiles: List<MediaItemDomain.File>
    
    lateinit var images: MediaItemDomain.Album
    lateinit var videos: MediaItemDomain.Album
    lateinit var gifs: MediaItemDomain.Album
    lateinit var hiddenImages: MediaItemDomain.Album
    lateinit var albumsWithFiles: List<MediaItemDomain.Album>
    
    lateinit var storage: MediaItemDomain.Album
    lateinit var emulated: MediaItemDomain.Album
    lateinit var `0`: MediaItemDomain.Album
    lateinit var dcim: MediaItemDomain.Album
    
    lateinit var fakeAlbums: List<MediaItemDomain.Album>
    
    init {
        image1 = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Images/image1.png",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnail = null,
            mimetype = "image/png",
            duration = 0,
            uri = "",
        )
        image2 = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Images/image2.jpg",
            size = 4567,
            creationDate = 1643263400,
            modificationDate = 1643727372,
            thumbnail = null,
            mimetype = "image/jpg",
            duration = 0,
            uri = "",
        )
        
        video = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Videos/video.mp4",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnail = null,
            mimetype = "video/mp4",
            duration = 123456,
            uri = "",
        )
        
        gif = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Gifs/gif.gif",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnail = null,
            mimetype = "image/gif",
            duration = 123,
            uri = "",
        )
        
        hiddenImage = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/.Hidden/.hidden.png",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnail = null,
            mimetype = "image/png",
            duration = 0,
            uri = "",
        )
        
        val fakeFilesList = listOf(image1, image2, video, gif, hiddenImage)
        
        
        images = MediaItemDomain.Album(
            path = Path(image1.path).parent.pathString,
            size = image1.size,
            creationDate = image1.creationDate,
            modificationDate = image1.modificationDate,
            thumbnail = null,
            files = listOf(image1, image2),
            filesCount = 1,
            imagesCount = 1,
            videosCount = 0,
            gifsCount = 0,
            hiddenCount = 0,
            unhiddenCount = 1,
            albumsCount = 0
        )
        
        videos = MediaItemDomain.Album(
            path = Path(video.path).parent.pathString,
            size = video.size,
            creationDate = video.creationDate,
            modificationDate = video.modificationDate,
            thumbnail = null,
            files = listOf(video),
            filesCount = 1,
            imagesCount = 0,
            videosCount = 1,
            gifsCount = 0,
            hiddenCount = 0,
            unhiddenCount = 1,
            albumsCount = 0
        )
        
        gifs = MediaItemDomain.Album(
            path = Path(gif.path).parent.pathString,
            size = gif.size,
            creationDate = gif.creationDate,
            modificationDate = gif.modificationDate,
            thumbnail = null,
            files = listOf(gif),
            filesCount = 1,
            imagesCount = 0,
            videosCount = 0,
            gifsCount = 1,
            hiddenCount = 0,
            unhiddenCount = 1,
            albumsCount = 0
        )
        
        hiddenImages = MediaItemDomain.Album(
            path = Path(hiddenImage.path).parent.pathString,
            size = hiddenImage.size,
            creationDate = hiddenImage.creationDate,
            modificationDate = hiddenImage.modificationDate,
            thumbnail = null,
            files = listOf(hiddenImage),
            filesCount = 1,
            imagesCount = 1,
            videosCount = 0,
            gifsCount = 0,
            hiddenCount = 1,
            unhiddenCount = 0,
            albumsCount = 0
        )
        
        albumsWithFiles = listOf(images, videos, gifs, hiddenImages)
        
        
        storage = MediaItemDomain.Album(
            path = "/storage",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnail = null,
            files = emptyList(),
            filesCount = fakeFilesList.size,
            imagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            videosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            gifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            hiddenCount = fakeFilesList.count { it.isHidden },
            unhiddenCount = fakeFilesList.count { !it.isHidden },
            albumsCount = albumsWithFiles.size + 3
        )
        
        emulated = MediaItemDomain.Album(
            path = "/storage/emulated",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnail = null,
            files = emptyList(),
            filesCount = fakeFilesList.size,
            imagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            videosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            gifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            hiddenCount = fakeFilesList.count { it.isHidden },
            unhiddenCount = fakeFilesList.count { !it.isHidden },
            albumsCount = albumsWithFiles.size + 2
        )
        
        `0` = MediaItemDomain.Album(
            path = "/storage/emulated/0",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnail = null,
            files = emptyList(),
            filesCount = fakeFilesList.size,
            imagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            videosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            gifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            hiddenCount = fakeFilesList.count { it.isHidden },
            unhiddenCount = fakeFilesList.count { !it.isHidden },
            albumsCount = albumsWithFiles.size + 1
        )
        
        dcim = MediaItemDomain.Album(
            path = "/storage/emulated/0/DCIM",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnail = null,
            files = emptyList(),
            filesCount = fakeFilesList.size,
            imagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            videosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            gifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            hiddenCount = fakeFilesList.count { it.isHidden },
            unhiddenCount = fakeFilesList.count { !it.isHidden },
            albumsCount = albumsWithFiles.size
        )
        
        fakeAlbums = albumsWithFiles + listOf(storage, emulated, `0`, dcim)
    }
}
