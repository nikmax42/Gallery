package mtree.explorer.components.preferences_sheet

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nikmax.mtree.core.preferences.CorePreferences
import nikmax.mtree.gallery.core.data.preferences.GalleryPreferences
import org.junit.Before
import org.junit.Test

class GalleryPreferencesSheetVmTest {
    
    private lateinit var fakeGalleryPrefsRepo: FakeGalleryPreferencesRepo
    private lateinit var fakeCorePrefsRepo: FakeCorePreferencesRepo
    private lateinit var vm: GalleryPreferencesSheetVm
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeGalleryPrefsRepo = FakeGalleryPreferencesRepo()
        fakeCorePrefsRepo = FakeCorePreferencesRepo()
        vm = GalleryPreferencesSheetVm(
            galleryPrefsRepo = fakeGalleryPrefsRepo,
            corePreferencesRepo = fakeCorePrefsRepo
        )
        vm.onAction(Action.Launch)
    }
    
    
    @Test
    fun initial_tab_is_appearance() = runTest {
        vm.state.test {
            val initialState = awaitItem()
            assert(initialState.tab == Tab.APPEARANCE)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun tab_change_updates_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeTab(Tab.APPEARANCE)
            )
            assert(awaitItem().tab == Tab.APPEARANCE)
            
            vm.onAction(
                Action.ChangeTab(Tab.FILTERING)
            )
            assert(awaitItem().tab == Tab.FILTERING)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun mode_change_updates_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeMode(GalleryMode.TREE)
            )
            assert(awaitItem().mode == GalleryMode.TREE)
            
            vm.onAction(
                Action.ChangeMode(GalleryMode.PLAIN)
            )
            assert(awaitItem().mode == GalleryMode.PLAIN)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun mode_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeMode(GalleryMode.TREE)
            )
            assert(awaitItem().galleryMode == GalleryPreferences.GalleryMode.TREE)
            
            vm.onAction(
                Action.ChangeMode(GalleryMode.PLAIN)
            )
            assert(awaitItem().galleryMode == GalleryPreferences.GalleryMode.PLAIN)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun theme_change_updates_state() = runTest {
        vm.state.test {
            awaitItem()
            vm.onAction(
                Action.ChangeTheme(AppTheme.LIGHT)
            )
            assert(awaitItem().theme == AppTheme.LIGHT)
            
            vm.onAction(
                Action.ChangeTheme(AppTheme.DARK)
            )
            assert(awaitItem().theme == AppTheme.DARK)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun theme_change_updates_preference() = runTest {
        fakeCorePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeTheme(AppTheme.LIGHT)
            )
            assert(awaitItem().theme == CorePreferences.Theme.LIGHT)
            
            vm.onAction(
                Action.ChangeTheme(AppTheme.DARK)
            )
            assert(awaitItem().theme == CorePreferences.Theme.DARK)
            
            vm.onAction(
                Action.ChangeTheme(AppTheme.SYSTEM)
            )
            assert(awaitItem().theme == CorePreferences.Theme.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun dynamic_colors_change_updates_state() = runTest {
        vm.state.test {
            awaitItem()
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.DISABLED)
            )
            assert(awaitItem().dynamicColors == DynamicColors.DISABLED)
            
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.SYSTEM)
            )
            assert(awaitItem().dynamicColors == DynamicColors.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun dynamic_colors_change_updates_preference() = runTest {
        fakeCorePrefsRepo.getPreferencesFlow().test {
            awaitItem()
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.DISABLED)
            )
            assert(awaitItem().dynamicColors == CorePreferences.DynamicColors.DISABLED)
            
            vm.onAction(
                Action.ChangeDynamicColors(DynamicColors.SYSTEM)
            )
            assert(awaitItem().dynamicColors == CorePreferences.DynamicColors.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun grid_columns_portrait_change_updates_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeGridColumnsPortrait(3)
            )
            assert(awaitItem().gridColumnsPortrait == 3)
            
            vm.onAction(
                Action.ChangeGridColumnsPortrait(4)
            )
            assert(awaitItem().gridColumnsPortrait == 4)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun grid_columns_portrait_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun grid_columns_landscape_change_updates_state() = runTest {
        vm.state.test {
            awaitItem()
            vm.onAction(
                Action.ChangeGridColumnsLandscape(3)
            )
            assert(awaitItem().gridColumnsLandscape == 3)
            
            vm.onAction(
                Action.ChangeGridColumnsLandscape(4)
            )
            assert(awaitItem().gridColumnsLandscape == 4)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun grid_columns_landscape_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun place_on_top_change_updates_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.ALBUMS)
            )
            assert(awaitItem().placeOnTop == PlaceOnTop.ALBUMS)
            
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.FILES)
            )
            assert(awaitItem().placeOnTop == PlaceOnTop.FILES)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun place_on_top_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.ALBUMS)
            )
            assert(awaitItem().placeOnTop == GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP)
            
            vm.onAction(
                Action.ChangePlaceOnTop(PlaceOnTop.FILES)
            )
            assert(awaitItem().placeOnTop == GalleryPreferences.PlaceOnTop.FILES_ON_TOP)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun sort_order_change_updates_state() = runTest {
        vm.state.test {
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.DATE_CREATED)
            )
            assert(awaitItem().sortOrder == SortOrder.DATE_CREATED)
            
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.DATE_MODIFIED)
            )
            assert(awaitItem().sortOrder == SortOrder.DATE_MODIFIED)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun sort_order_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.DATE_CREATED)
            )
            assert(awaitItem().sortOrder == GalleryPreferences.SortOrder.CREATION_DATE)
            
            vm.onAction(
                Action.ChangeSortOrder(SortOrder.DATE_MODIFIED)
            )
            assert(awaitItem().sortOrder == GalleryPreferences.SortOrder.MODIFICATION_DATE)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    
    @Test
    fun descend_sorting_change_updates_state() = runTest {
        vm.state.test {
            awaitItem()
            vm.onAction(
                Action.ChangeDescendSorting(true)
            )
            assert(awaitItem().descendSorting == true)
            
            vm.onAction(
                Action.ChangeDescendSorting(false)
            )
            assert(awaitItem().descendSorting == false)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun descend_sorting_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_images_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_images_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_videos_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_videos_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_gifs_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_gifs_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_hidden_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_hidden_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_un_hidden_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_un_hidden_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_files_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_files_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
    fun include_albums_change_updates_state() = runTest {
        vm.state.test {
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
    fun include_albums_change_updates_preference() = runTest {
        fakeGalleryPrefsRepo.getPreferencesFlow().test {
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
}
