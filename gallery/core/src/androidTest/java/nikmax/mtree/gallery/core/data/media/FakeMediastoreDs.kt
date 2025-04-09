package nikmax.mtree.gallery.core.data.media

class FakeMediastoreDs : MediastoreDs {
    
    private val picture1 = MediaItemData.File(path = "/storage/emulated/0/Pictures/1.jpg")
    private val picture2 = MediaItemData.File(path = "/storage/emulated/0/Pictures/2.jpg")
    private val movie1 = MediaItemData.File(path = "/storage/emulated/0/Movies/1.mp4")
    private val movie2 = MediaItemData.File(path = "/storage/emulated/0/Movies/2.mp4")
    private val gif1 = MediaItemData.File(path = "/storage/emulated/0/Gifs/1.gif")
    private val gif2 = MediaItemData.File(path = "/storage/emulated/0/Gifs/2.gif")
    
    override fun getImagesAndVideos(): List<MediaItemData.File> {
        return listOf(
            picture1,
            picture2,
            movie1,
            movie2,
            gif1,
            gif2
        )
    }
}
