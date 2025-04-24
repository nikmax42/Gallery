package mtree.dialogs.album_picker

import mtree.core.data.MediaItemData
import mtree.core.ui.models.MediaItemUI

internal object FakeItems {
    val fakeFileData = MediaItemData.File(
        path = "/storage/emulated/0/album/image.png",
        creationDate = 1L,
        modificationDate = 1L,
        size = 1L,
        thumbnail = null,
        mimeType = "image/png",
        duration = 1L,
        uri = "uri"
    )
    val fakeAlbumData = MediaItemData.Album(
        path = "/storage/emulated/0/album",
        ownFiles = listOf(fakeFileData),
        nestedMediaSize = 1L,
        nestedImagesCount = 1,
        nestedVideosCount = 0,
        nestedGifsCount = 0,
        nestedHiddenMediaCount = 0,
        nestedUnhiddenMediaCount = 1,
        nestedAlbumsCount = 0,
        creationDate = 1L,
        modificationDate = 1L,
        thumbnailPath = null
    )
    val fakeAlbumsData = listOf(
        fakeAlbumData
    )
    
    val fakeFileUi = MediaItemUI.File(
        path = "/storage/emulated/0/album/image.png",
        creationDate = 1L,
        modificationDate = 1L,
        size = 1L,
        thumbnail = null,
        mimetype = "image/png",
        duration = 1L,
        uri = "uri"
    )
    
    val fakeAlbumUi = MediaItemUI.Album.emptyFromPath("/test-album")
}
