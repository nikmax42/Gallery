package nikmax.mtree.gallery.viewer

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ViewerVmTest {
    
    private lateinit var vm: ViewerVm
    
    @Before
    fun setup() {
        vm = ViewerVm(
            prefsRepo = FakeGalleryPreferencesRepo(),
            mediaItemsRepo = FakeGalleryItemsRepo()
        )
    }
    
    @Test
    fun launches_with_initiating_placeholder_content() = runTest {
        vm.onAction(Action.Launch(""))
        vm.uiState.test {
            assert(awaitItem().content is UiState.Content.Initiating)
        }
    }
}
