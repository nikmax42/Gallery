package mtree.preferences_sheet

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mtree.core.preferences.MtreePreferences
import org.junit.Before
import org.junit.Test

class GalleryPreferencesSheetVmTest {
    
    private lateinit var fakePrefsRepo: FakePrefsRepo
    private lateinit var vm: GalleryPreferencesSheetVm
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        fakePrefsRepo = FakePrefsRepo()
        vm = GalleryPreferencesSheetVm(
            prefsRepo = fakePrefsRepo
        )
    }
    
    
    @Test
    fun mode_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeMode(GalleryMode.TREE)
            )
            assert(awaitItem().galleryMode == MtreePreferences.GalleryMode.TREE)
            
            vm.onAction(
                Action.ChangeMode(GalleryMode.PLAIN)
            )
            assert(awaitItem().galleryMode == MtreePreferences.GalleryMode.PLAIN)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun theme_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeTheme(AppTheme.LIGHT)
            )
            assert(awaitItem().theme == MtreePreferences.Theme.LIGHT)
            
            vm.onAction(
                Action.ChangeTheme(AppTheme.DARK)
            )
            assert(awaitItem().theme == MtreePreferences.Theme.DARK)
            
            vm.onAction(
                Action.ChangeTheme(AppTheme.SYSTEM)
            )
            assert(awaitItem().theme == MtreePreferences.Theme.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun dynamic_colors_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.DISABLED)
            )
            assert(awaitItem().dynamicColors == MtreePreferences.DynamicColors.DISABLED)
            
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.SYSTEM)
            )
            assert(awaitItem().dynamicColors == MtreePreferences.DynamicColors.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun grid_columns_portrait_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeGridColumnsPortrait(3)
            )
            assert(awaitItem().portraitGridColumns == 3)
            
            vm.onAction(
                Action.ChangeGridColumnsPortrait(4)
            )
            assert(awaitItem().portraitGridColumns == 4)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun grid_columns_landscape_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeGridColumnsLandscape(3)
            )
            assert(awaitItem().landscapeGridColumns == 3)
            
            vm.onAction(
                Action.ChangeGridColumnsLandscape(4)
            )
            assert(awaitItem().landscapeGridColumns == 4)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun place_on_top_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.ALBUMS)
            )
            assert(awaitItem().placeOnTop == MtreePreferences.PlaceOnTop.ALBUMS_ON_TOP)
            
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.FILES)
            )
            assert(awaitItem().placeOnTop == MtreePreferences.PlaceOnTop.FILES_ON_TOP)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun sort_order_change_updates_state() = runTest {
        vm.uiState.test {
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.CREATIOIN_DATE)
            )
            assert(awaitItem().sortOrder == SortOrder.CREATIOIN_DATE)
            
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.MODIFICATION_DATE)
            )
            assert(awaitItem().sortOrder == SortOrder.MODIFICATION_DATE)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun sort_order_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.CREATIOIN_DATE)
            )
            assert(awaitItem().sortOrder == MtreePreferences.SortOrder.CREATION_DATE)
            
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.MODIFICATION_DATE)
            )
            assert(awaitItem().sortOrder == MtreePreferences.SortOrder.MODIFICATION_DATE)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun descend_sorting_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeDescendSorting(true)
            )
            assert(awaitItem().descendSortOrder == true)
            
            vm.onAction(
                Action.ChangeDescendSorting(false)
            )
            assert(awaitItem().descendSortOrder == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    
    @Test
    fun include_images_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeImages(true)
            )
            assert(awaitItem().showImages == true)
            
            vm.onAction(
                Action.ChangeIncludeImages(false)
            )
            assert(awaitItem().showImages == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_videos_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeVideos(true)
            )
            assert(awaitItem().showVideos == true)
            
            vm.onAction(
                Action.ChangeIncludeVideos(false)
            )
            assert(awaitItem().showVideos == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_gifs_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeGifs(true)
            )
            assert(awaitItem().showGifs == true)
            
            vm.onAction(
                Action.ChangeIncludeGifs(false)
            )
            assert(awaitItem().showGifs == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_hidden_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeIncludeHidden(true)
            )
            assert(awaitItem().showHidden == true)
            
            vm.onAction(
                Action.ChangeIncludeHidden(false)
            )
            assert(awaitItem().showHidden == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_un_hidden_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeUnHidden(true)
            )
            assert(awaitItem().showUnHidden == true)
            
            vm.onAction(
                Action.ChangeIncludeUnHidden(false)
            )
            assert(awaitItem().showUnHidden == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_files_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeFiles(true)
            )
            assert(awaitItem().showFiles == true)
            
            vm.onAction(
                Action.ChangeIncludeFiles(false)
            )
            assert(awaitItem().showFiles == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun include_albums_change_updates_preference() = runTest {
        fakePrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeIncludeAlbums(true)
            )
            assert(awaitItem().showAlbums == true)
            
            vm.onAction(
                Action.ChangeIncludeAlbums(false)
            )
            assert(awaitItem().showAlbums == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun `preferences update triggers state update`() = runTest {
        vm.uiState.test {
            val testValue = 42
            val newPrefs = fakePrefsRepo
                .getPreferencesFlow()
                .first()
                .copy(portraitGridColumns = testValue)
            skipItems(2)
            fakePrefsRepo.savePreferences(newPrefs)
            awaitItem().let {
                assert(it.gridColumnsPortrait == testValue)
            }
        }
    }
}
