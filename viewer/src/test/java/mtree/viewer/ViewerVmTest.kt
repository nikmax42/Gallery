package mtree.viewer

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mtree.core.data.MediaItemsRepo
import mtree.core.preferences.MtreePreferencesRepo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ViewerVmTest {
    private lateinit var vm: ViewerVm
    private lateinit var fakeGalleryAlbumsRepo: MediaItemsRepo
    private lateinit var fakePrefsRepo: MtreePreferencesRepo
    private lateinit var fakeCreateItemsListToDisplayUc: FakeCreateItemsListToDisplayUc
    private lateinit var fakeCopyOrMoveItemsUc: FakeCopyOrMoveItemsUc
    private lateinit var fakeRenameItemsUc: FakeRenameItemsUc
    private lateinit var fakeDeleteItemsUc: FakeDeleteItemsUc
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        
        fakeGalleryAlbumsRepo = FakeItemsRepo()
        fakePrefsRepo = FakePrefsRepo()
        fakeCreateItemsListToDisplayUc = FakeCreateItemsListToDisplayUc()
        fakeCopyOrMoveItemsUc = FakeCopyOrMoveItemsUc()
        fakeRenameItemsUc = FakeRenameItemsUc()
        fakeDeleteItemsUc = FakeDeleteItemsUc()
        
        vm = ViewerVm(
            galleryAlbumsRepo = fakeGalleryAlbumsRepo,
            prefsRepo = fakePrefsRepo,
            createItemsListToDisplayUc = fakeCreateItemsListToDisplayUc,
            copyOrMoveItemsUc = fakeCopyOrMoveItemsUc,
            renameItemsUc = fakeRenameItemsUc,
            deleteItemsUc = fakeDeleteItemsUc
        )
    }
    
    
    @Test
    fun `on launch - sets file path`() = runTest {
        vm._initialFilePath.test {
            awaitItem().let {
                assertNull(it)
            }
            val testPath = "path.png"
            vm.onAction(
                Action.Launch(
                    initialFilePath = testPath,
                    searchQuery = null
                )
            )
            awaitItem().let {
                assertEquals(testPath, it)
            }
        }
    }
    
    @Test
    fun `on launch - sets search query`() = runTest {
        vm._searchQuery.test {
            awaitItem().let {
                assertNull(it)
            }
            val testQuery = "query123"
            vm.onAction(
                Action.Launch(
                    initialFilePath = "",
                    searchQuery = testQuery
                )
            )
            awaitItem().let {
                assertEquals(testQuery, it)
            }
        }
    }
    
    /*  @Test
     fun `on controls switch updates state`() = runTest {
         vm.uiState.test {
             //awaitItem()
             skipItems(1)
             vm.onAction(Action.SwitchControls)
             awaitItem().let {
                 assertTrue(it.showControls)
             }
             vm.onAction(Action.SwitchControls)
             awaitItem().let {
                 assertFalse(it.showControls)
             }
         }
     } */
}
