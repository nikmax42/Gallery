package mtree.dialogs.album_picker

import app.cash.turbine.test
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mtree.core.data.MediaItemsRepo
import mtree.core.domain.usecases.CreateItemsListToDisplayUc
import mtree.core.preferences.MtreePreferencesRepo
import org.junit.Before
import org.junit.Test

class AlbumPickerVmTest {
    private lateinit var vm: AlbumPickerVm
    private lateinit var itemsRepo: MediaItemsRepo
    private lateinit var prefsRepo: MtreePreferencesRepo
    private lateinit var createItemsListToDisplayUc: CreateItemsListToDisplayUc
    
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        itemsRepo = FakeItemsRepo()
        prefsRepo = FakePrefsRepo()
        createItemsListToDisplayUc = FakeCreateItemsListToDisplayUc()
        vm = AlbumPickerVm(
            albumsRepo = itemsRepo,
            prefsRepo = prefsRepo,
            createItemsListToDisplayUc = createItemsListToDisplayUc
        )
    }
    
    
    @Test
    fun `initiates with default initialization content`() = runTest {
        vm.uiState.test {
            awaitItem().let {
                assert(it.content is Content.Initialization)
                assertNull(it.currentAlbum)
            }
        }
    }
    
    @Test
    fun `on launch - clears opened albums stack`() = runTest {
        vm._openedAlbumsStack.test {
            vm.onAction(Action.Launch)
            awaitItem().let {
                assert(it.isEmpty())
            }
        }
    }
    
    @Test
    fun `on album open - adds album to stack`() = runTest {
        vm._openedAlbumsStack.test {
            awaitItem()
            val album = FakeItems.fakeAlbumUi
            vm.onAction(Action.NavigateInsideAlbum(album))
            awaitItem().let {
                assert(it.contains(album))
            }
        }
    }
    
    @Test
    fun `on navigate back - removes album from stack`() = runTest {
        vm._openedAlbumsStack.test {
            awaitItem()
            val album = FakeItems.fakeAlbumUi
            vm.onAction(Action.NavigateInsideAlbum(album))
            awaitItem()
            vm.onAction(Action.NavigateBack)
            awaitItem().let {
                assert(!it.contains(album))
            }
        }
    }
}
