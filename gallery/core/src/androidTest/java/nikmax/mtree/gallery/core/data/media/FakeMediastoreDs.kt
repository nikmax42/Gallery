package nikmax.mtree.gallery.core.data.media

class FakeMediastoreDs : MediastoreDs {
    
    internal val picture1 = MediaItemData.File(path = "/storage/emulated/0/Pictures/1.jpg")
    internal val picture2 = MediaItemData.File(path = "/storage/emulated/0/Pictures/2.jpg")
    internal val movie1 = MediaItemData.File(path = "/storage/emulated/0/Movies/1.mp4")
    internal val movie2 = MediaItemData.File(path = "/storage/emulated/0/Movies/2.mp4")
    internal val gif1 = MediaItemData.File(path = "/storage/emulated/0/Gifs/1.gif")
    internal val gif2 = MediaItemData.File(path = "/storage/emulated/0/Gifs/2.gif")
    
    internal val fakeFiles = listOf(
        picture1,
        picture2,
        movie1,
        movie2,
        gif1,
        gif2
    )
    
    override fun getImagesAndVideos(): List<MediaItemData.File> {
        return fakeFiles
    }
}
