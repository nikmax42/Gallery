package mtree.explorer

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mtree.core.data.MediaItemsRepo
import mtree.core.data.Resource
import mtree.core.domain.usecases.CopyOrMoveItemsUc
import mtree.core.domain.usecases.CreateItemsListToDisplayUc
import mtree.core.domain.usecases.DeleteItemsUc
import mtree.core.domain.usecases.RenameItemsUc
import mtree.core.preferences.MtreePreferencesRepo
import mtree.core.ui.models.MediaItemUI
import org.junit.Before
import org.junit.Test

class ExplorerVmTest {
    private lateinit var vm: ExplorerVm
    private lateinit var itemsRepo: MediaItemsRepo
    private lateinit var prefsRepo: MtreePreferencesRepo
    private lateinit var fakeFilterUc: CreateItemsListToDisplayUc
    private lateinit var fakeCopyOrMoveItemsUc: CopyOrMoveItemsUc
    private lateinit var fakeRenameItemsUc: RenameItemsUc
    private lateinit var fakeDeleteItemsUc: DeleteItemsUc
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        itemsRepo = FakeItemsRepo()
        prefsRepo = FakePrefsRepo()
        fakeFilterUc = FakeCreateItemsListToDisplayUc()
        fakeCopyOrMoveItemsUc = FakeCopyOrMoveItemsUc()
        fakeRenameItemsUc = FakeRenameItemsUc()
        fakeDeleteItemsUc = FakeDeleteItemsUc()
        
        vm = ExplorerVm(
            galleryAlbumsRepo = itemsRepo,
            createItemsListToDisplayUc = fakeFilterUc,
            preferencesRepo = prefsRepo,
            copyOrMoveItemsUc = fakeCopyOrMoveItemsUc,
            renameItemsUc = fakeRenameItemsUc,
            deleteItemsUc = fakeDeleteItemsUc
        )
    }
    
    @Test
    fun `on launch - sets path and searchQuery`() = runTest {
        vm.uiState.test {
            val path = "path"
            val query = "query"
            skipItems(1)
            vm.onAction(
                Action.Launch(
                    albumPath = path,
                    searchQuery = query
                )
            )
            awaitItem().let {
                assert(it.albumPath == path)
                assert(it.searchQuery == query)
            }
        }
    }
    
    @Test
    fun `on refresh - performs repo rescan`() = runTest {
        itemsRepo.getMediaAlbumsFlow().test {
            assert(awaitItem() is Resource.Success)
            vm.onAction(Action.Refresh)
            assert(awaitItem() is Resource.Loading)
        }
    }
    
    @Test
    fun `on loading - resource loading state is true`() = runTest {
        vm.uiState.test {
            assert(awaitItem().isLoading == false)
            vm.onAction(Action.Refresh)
            assert(awaitItem().isLoading == true)
        }
    }
    
    @Test
    fun `onResetFiltersAndSearch - resets filters preferences to defaults`() = runTest {
        prefsRepo.getPreferencesFlow().test {
            vm.onAction(Action.ResetFiltersAndSearch)
            awaitItem().let {
                assert(it.showImages == true)
                assert(it.showVideos == true)
                assert(it.showGifs == true)
                assert(it.showAlbums == true)
                assert(it.showFiles == true)
                assert(it.showUnHidden == true)
                assert(it.showHidden == false)
            }
        }
    }
    
    @Test
    fun `onResetFiltersAndSearch - sets searchQuery to null`() = runTest {
        vm.uiState.test {
            vm.onAction(Action.ResetFiltersAndSearch)
            assert(awaitItem().searchQuery == null)
        }
    }
    
    @Test
    fun `onSearchQueryChange - updates searchQuery`() = runTest {
        vm.uiState.test {
            val query = "query"
            assert(awaitItem().searchQuery == null)
            vm.onAction(Action.SearchQueryChange(query))
            assert(awaitItem().searchQuery == query)
        }
    }
    
    @Test
    fun `onItemsSelectionChange - updates selectedItems`() = runTest {
        vm.uiState.test {
            awaitItem().let {
                assert(it.selectedItems.isEmpty())
            }
            val itemsToSelect = listOf(
                MediaItemUI.File.emptyFromPath("test.jpg"),
            )
            vm.onAction(Action.ItemsSelectionChange(itemsToSelect))
            awaitItem().let {
                assert(it.selectedItems == itemsToSelect)
            }
        }
    }
    
    
    /*   fun `on copy or move - sets dialog to Picker`() = runTest {
          vm.uiState.test {
              vm.onAction(Action.ItemsCopy())
              assert(awaitItem().dialog is Dialog.AlbumPicker)
              // (awaitItem().dialog as Dialog.AlbumPicker).onConfirm("path")
          }
      } */
    //todo test file operations actions
    
    /* @Test
    fun `on copy ot move - asks for destination path, then asks for conflicts resolution, then triggers start callback, then triggers end callback`() {
        vm.onAction(
            Action.ItemsCopy(
                listOf(FakeItems.fakeFileUi)
            )
        )
    } */
}
