package nikmax.gallery.gallery.explorer.components.preferences_sheet

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class GalleryPreferencesSheetVmTest {
    
    private lateinit var repo: FakeGalleryPreferencesRepo
    private lateinit var vm: GalleryPreferencesSheetVm
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        repo = FakeGalleryPreferencesRepo()
        vm = GalleryPreferencesSheetVm(repo)
        vm.onAction(Action.Launch)
    }
    
    @Test
    fun initial_content_is_appearance() = runTest {
        vm.state.test {
            val initialContent = awaitItem().content
            assert(initialContent is UiState.Content.Appearance)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun tab_change_switches_content() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeTab(Tab.FILTERING)
            )
            delay(2000)
            val content = awaitItem().content
            assert(content is UiState.Content.Filtering)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun mode_change_updates_preference_and_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeMode(GalleryMode.PLAIN)
            )
            delay(5000)
            val mode = (awaitItem().content as UiState.Content.Appearance).mode
            assert(mode == GalleryMode.PLAIN)
            val prefs = repo.getPreferencesFlow().first()
            assert(prefs.appearance.nestedAlbumsEnabled == false)
        }
    }
}
