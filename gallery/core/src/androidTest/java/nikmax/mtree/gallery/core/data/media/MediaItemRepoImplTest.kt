package nikmax.mtree.gallery.core.data.media

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before

class MediaItemRepoImplTest {
    
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var repo: MediaItemsRepo
    
    @Before
    fun setUp() {
        repo = MediaItemsRepoImpl(
            mediastoreDs = FakeMediastoreDs(),
            context = context
        )
    }
}
