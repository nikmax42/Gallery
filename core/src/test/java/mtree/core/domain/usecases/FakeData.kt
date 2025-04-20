package mtree.core.domain.usecases

import mtree.core.domain.models.MediaItemDomain
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
            thumbnailPath = null,
            mimetype = "image/png",
            duration = 0,
            uri = "",
        )
        image2 = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Images/image2.jpg",
            size = 4567,
            creationDate = 1643263400,
            modificationDate = 1643727372,
            thumbnailPath = null,
            mimetype = "image/jpg",
            duration = 0,
            uri = "",
        )
        
        video = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Videos/video.mp4",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnailPath = null,
            mimetype = "video/mp4",
            duration = 123456,
            uri = "",
        )
        
        gif = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/Gifs/gif.gif",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnailPath = null,
            mimetype = "image/gif",
            duration = 123,
            uri = "",
        )
        
        hiddenImage = MediaItemDomain.File(
            path = "/storage/emulated/0/DCIM/.Hidden/.hidden.png",
            size = 1234,
            creationDate = 1643723400,
            modificationDate = 1643723400,
            thumbnailPath = null,
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
            thumbnailPath = null,
            ownFiles = listOf(image1, image2),
            nestedFilesCount = 1,
            nestedImagesCount = 1,
            nestedVideosCount = 0,
            nestedGifsCount = 0,
            nestedHiddenMediaCount = 0,
            nestedUnhiddenMediaCount = 1,
            nestedAlbumsCount = 0
        )
        
        videos = MediaItemDomain.Album(
            path = Path(video.path).parent.pathString,
            size = video.size,
            creationDate = video.creationDate,
            modificationDate = video.modificationDate,
            thumbnailPath = null,
            ownFiles = listOf(video),
            nestedFilesCount = 1,
            nestedImagesCount = 0,
            nestedVideosCount = 1,
            nestedGifsCount = 0,
            nestedHiddenMediaCount = 0,
            nestedUnhiddenMediaCount = 1,
            nestedAlbumsCount = 0
        )
        
        gifs = MediaItemDomain.Album(
            path = Path(gif.path).parent.pathString,
            size = gif.size,
            creationDate = gif.creationDate,
            modificationDate = gif.modificationDate,
            thumbnailPath = null,
            ownFiles = listOf(gif),
            nestedFilesCount = 1,
            nestedImagesCount = 0,
            nestedVideosCount = 0,
            nestedGifsCount = 1,
            nestedHiddenMediaCount = 0,
            nestedUnhiddenMediaCount = 1,
            nestedAlbumsCount = 0
        )
        
        hiddenImages = MediaItemDomain.Album(
            path = Path(hiddenImage.path).parent.pathString,
            size = hiddenImage.size,
            creationDate = hiddenImage.creationDate,
            modificationDate = hiddenImage.modificationDate,
            thumbnailPath = null,
            ownFiles = listOf(hiddenImage),
            nestedFilesCount = 1,
            nestedImagesCount = 1,
            nestedVideosCount = 0,
            nestedGifsCount = 0,
            nestedHiddenMediaCount = 1,
            nestedUnhiddenMediaCount = 0,
            nestedAlbumsCount = 0
        )
        
        albumsWithFiles = listOf(images, videos, gifs, hiddenImages)
        
        
        storage = MediaItemDomain.Album(
            path = "/storage",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnailPath = null,
            ownFiles = emptyList(),
            nestedFilesCount = fakeFilesList.size,
            nestedImagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            nestedVideosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            nestedGifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            nestedHiddenMediaCount = fakeFilesList.count { it.isHidden },
            nestedUnhiddenMediaCount = fakeFilesList.count { !it.isHidden },
            nestedAlbumsCount = albumsWithFiles.size + 3
        )
        
        emulated = MediaItemDomain.Album(
            path = "/storage/emulated",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnailPath = null,
            ownFiles = emptyList(),
            nestedFilesCount = fakeFilesList.size,
            nestedImagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            nestedVideosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            nestedGifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            nestedHiddenMediaCount = fakeFilesList.count { it.isHidden },
            nestedUnhiddenMediaCount = fakeFilesList.count { !it.isHidden },
            nestedAlbumsCount = albumsWithFiles.size + 2
        )
        
        `0` = MediaItemDomain.Album(
            path = "/storage/emulated/0",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnailPath = null,
            ownFiles = emptyList(),
            nestedFilesCount = fakeFilesList.size,
            nestedImagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            nestedVideosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            nestedGifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            nestedHiddenMediaCount = fakeFilesList.count { it.isHidden },
            nestedUnhiddenMediaCount = fakeFilesList.count { !it.isHidden },
            nestedAlbumsCount = albumsWithFiles.size + 1
        )
        
        dcim = MediaItemDomain.Album(
            path = "/storage/emulated/0/DCIM",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            thumbnailPath = null,
            ownFiles = emptyList(),
            nestedFilesCount = fakeFilesList.size,
            nestedImagesCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.IMAGE },
            nestedVideosCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.VIDEO },
            nestedGifsCount = fakeFilesList.count { it.mediaType == MediaItemDomain.File.MediaType.GIF },
            nestedHiddenMediaCount = fakeFilesList.count { it.isHidden },
            nestedUnhiddenMediaCount = fakeFilesList.count { !it.isHidden },
            nestedAlbumsCount = albumsWithFiles.size
        )
        
        fakeAlbums = albumsWithFiles + listOf(storage, emulated, `0`, dcim)
    }
}
