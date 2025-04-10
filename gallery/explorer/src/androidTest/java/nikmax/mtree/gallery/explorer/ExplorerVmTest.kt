package nikmax.mtree.gallery.explorer

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import nikmax.mtree.core.data.Resource
import nikmax.mtree.gallery.core.data.media.MediaItemsRepo
import nikmax.mtree.gallery.core.data.preferences.GalleryPreferencesRepo
import nikmax.mtree.gallery.core.mappers.MediaItemMapper.mapToUi
import nikmax.mtree.gallery.core.ui.MediaItemUI
import org.junit.Before
import org.junit.Test

class ExplorerVmTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var fakeGalleryPrefsRepo: GalleryPreferencesRepo
    private lateinit var fakeMediaItemsRepo: MediaItemsRepo
    private lateinit var vm: ExplorerVm
    
    @Before
    fun setup() {
        fakeGalleryPrefsRepo = FakeGalleryPreferencesRepo()
        fakeMediaItemsRepo = FakeMediaItemsRepo()
        vm = ExplorerVm(
            context = context,
            galleryPrefsRepo = fakeGalleryPrefsRepo,
            mediaItemsRepo = fakeMediaItemsRepo
        )
        vm.onAction(Action.Launch)
    }
    
    @Test
    fun on_ALBUM_open_changes_current_PATH() = runTest {
        vm.uiState.test {
            assert(expectMostRecentItem().albumPath == null)
            val album = MediaItemUI.Album("/test")
            vm.onAction(Action.ItemOpen(album))
            assert(awaitItem().albumPath == album.path)
        }
    }
    
    @Test
    fun on_FILE_open_emits_EVENT() = runTest {
        vm.event.test {
            val file = MediaItemUI.File("/test")
            vm.onAction(Action.ItemOpen(file))
            assert(awaitItem() is Event.OpenViewer)
        }
    }
    
    @Test
    fun on_REFRESH_performs_repo_RESCAN() = runTest {
        fakeMediaItemsRepo.getMediaItemsFlow(
            path = null,
            searchQuery = null,
            treeMode = false
        ).test {
            vm.onAction(Action.Refresh)
            assert(awaitItem() is Resource.Loading)
        }
    }
    
    @Test
    fun on_navigate_BACK_returns_to_previous_PATH() = runTest {
        vm.uiState.test {
            assert(expectMostRecentItem().albumPath == null)
            val album = MediaItemUI.Album("/test")
            vm.onAction(Action.ItemOpen(album))
            assert(awaitItem().albumPath == album.path)
            vm.onAction(Action.NavigateToParentAlbum)
            delay(2000)
            assert(expectMostRecentItem().albumPath == null)
        }
    }
    
    @Test
    fun on_SEARCH_query_change_updates_STATE() = runTest {
        vm.uiState.test {
            assert(expectMostRecentItem().searchQuery == null)
            val query = "test"
            vm.onAction(Action.SearchQueryChange(query))
            assert(awaitItem().searchQuery == query)
        }
    }
    
    @Test
    fun on_SEARCH_query_change_updates_STATES_with_new_ITEMS() = runTest {
        vm.uiState.test {
            val oldItems = expectMostRecentItem().items
            val query = "test"
            vm.onAction(Action.SearchQueryChange(query))
            assert(awaitItem().items != oldItems)
        }
    }
    
    @Test
    fun on_items_SELECTION_adds_items_if_its_not_selected_yet() = runTest {
        vm.uiState.test {
            assert(expectMostRecentItem().selectedItems.isEmpty())
            val itemsToSelect = listOf(
                FakeMediaItemsRepo.picture1.mapToUi()
            )
            vm.onAction(
                Action.ItemsSelectionChange(itemsToSelect)
            )
            assert(awaitItem().selectedItems == itemsToSelect)
        }
    }
    
    @Test
    fun on_items_SELECTION_updates_STATE() = runTest {
        vm.uiState.test {
            val itemsToSelect = listOf(
                FakeMediaItemsRepo.picture1.mapToUi()
            )
            vm.onAction(
                Action.ItemsSelectionChange(itemsToSelect)
            )
            assert(awaitItem().selectedItems == itemsToSelect)
            vm.onAction(
                Action.ItemsSelectionChange(itemsToSelect)
            )
            assert(awaitItem().selectedItems.isEmpty())
        }
    }
}
