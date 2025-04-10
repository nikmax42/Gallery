package nikmax.mtree.gallery.explorer

import androidx.test.platform.app.InstrumentationRegistry
import nikmax.mtree.gallery.core.data.media.MediaItemsRepo
import nikmax.mtree.gallery.core.data.preferences.GalleryPreferencesRepo
import org.junit.Before

class ExplorerVmTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var fakeGalleryPrefsRepo: GalleryPreferencesRepo
    private lateinit var fakeMediaItemsRepo: MediaItemsRepo
    
    @Before
    fun setup() {
        fakeGalleryPrefsRepo = FakeGalleryPreferencesRepo()
        fakeMediaItemsRepo = FakeMediaItemsRepo()
    }
}
