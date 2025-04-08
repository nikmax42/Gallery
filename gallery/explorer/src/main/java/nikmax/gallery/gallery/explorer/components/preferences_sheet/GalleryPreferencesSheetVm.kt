package nikmax.gallery.gallery.explorer.components.preferences_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nikmax.gallery.core.preferences.CorePreferences
import nikmax.gallery.core.preferences.CorePreferencesRepo
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferencesRepo
import javax.inject.Inject

@HiltViewModel
internal class GalleryPreferencesSheetVm
@Inject constructor(
    private val corePreferencesRepo: CorePreferencesRepo,
    private val galleryPrefsRepo: GalleryPreferencesRepo
) : ViewModel() {
    
    private val _corePrefsFLow = MutableStateFlow(CorePreferences())
    private val _galleryPrefsFLow = MutableStateFlow(GalleryPreferences())
    
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    
    internal fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                Action.Launch -> onLaunch()
                is Action.ChangeTab -> onTabChanged(action.tab)
                is Action.ChangeMode -> onModeChanged(action.galleryMode)
                is Action.ChangeTheme -> onThemeChanged(action.theme)
                is Action.ChangeDynamicColors -> onDynamicColorsChanged(action.colors)
                is Action.ChangeGridColumnsPortrait -> onGridColumnsPortraitChanged(action.columns)
                is Action.ChangeGridColumnsLandscape -> onGridColumnsLandscapeChanged(action.columns)
                is Action.ChangePlaceOnTop -> onPlaceOnTopChanged(action.placeOnTop)
                is Action.ChangeSortOrder -> onSortOrderChanged(action.sortOrder)
                is Action.ChangeDescendSorting -> onDescendSortingChanged(action.enabled)
                is Action.ChangeIncludeImages -> onIncludeImagesChanged(action.enabled)
                is Action.ChangeIncludeVideos -> onIncludeVideosChanged(action.enabled)
                is Action.ChangeIncludeGifs -> onIncludeGifsChanged(action.enabled)
                is Action.ChangeIncludeHidden -> onIncludeHiddenChanged(action.enabled)
                is Action.ChangeIncludeUnHidden -> onIncludeUnHiddenChanged(action.enabled)
                is Action.ChangeIncludeFiles -> onIncludeFilesChanged(action.enabled)
                is Action.ChangeIncludeAlbums -> onIncludeAlbumsChanged(action.enabled)
            }
        }
    }
    
    
    private suspend fun keepCorePrefsFlow() {
        corePreferencesRepo
            .getPreferencesFlow()
            .collectLatest { prefs ->
                _corePrefsFLow.update { prefs }
            }
    }
    
    private suspend fun keepGalleryPrefsFlow() {
        galleryPrefsRepo
            .getPreferencesFlow()
            .collectLatest { prefs ->
                println(prefs.portraitGridColumns)
                _galleryPrefsFLow.update { prefs }
            }
    }
    
    private suspend fun reflectCorePreferencesFlow() {
        _corePrefsFLow.collectLatest { corePrefs ->
            _state.update {
                it.copy(
                    theme = when (corePrefs.theme) {
                        CorePreferences.Theme.SYSTEM -> AppTheme.SYSTEM
                        CorePreferences.Theme.LIGHT -> AppTheme.LIGHT
                        CorePreferences.Theme.DARK -> AppTheme.DARK
                    },
                    dynamicColors = when (corePrefs.dynamicColors) {
                        CorePreferences.DynamicColors.DISABLED -> DynamicColors.DISABLED
                        CorePreferences.DynamicColors.SYSTEM -> DynamicColors.SYSTEM
                    }
                )
            }
        }
    }
    
    private suspend fun reflectGalleryPreferencesFlow() {
        _galleryPrefsFLow.collectLatest { galleryPrefs ->
            _state.update {
                it.copy(
                    mode = when (galleryPrefs.galleryMode) {
                        GalleryPreferences.GalleryMode.PLAIN -> GalleryMode.PLAIN
                        GalleryPreferences.GalleryMode.TREE -> GalleryMode.TREE
                    },
                    gridColumnsPortrait = galleryPrefs.portraitGridColumns,
                    gridColumnsLandscape = galleryPrefs.landscapeGridColumns,
                    placeOnTop = when (galleryPrefs.placeOnTop) {
                        GalleryPreferences.PlaceOnTop.NONE -> PlaceOnTop.NONE
                        GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP -> PlaceOnTop.ALBUMS
                        GalleryPreferences.PlaceOnTop.FILES_ON_TOP -> PlaceOnTop.FILES
                    },
                    sortOrder = when (galleryPrefs.sortOrder) {
                        GalleryPreferences.SortOrder.CREATION_DATE -> SortOrder.DATE_CREATED
                        GalleryPreferences.SortOrder.MODIFICATION_DATE -> SortOrder.DATE_MODIFIED
                        GalleryPreferences.SortOrder.NAME -> SortOrder.NAME
                        GalleryPreferences.SortOrder.EXTENSION -> SortOrder.EXTENSION
                        GalleryPreferences.SortOrder.SIZE -> SortOrder.SIZE
                        GalleryPreferences.SortOrder.RANDOM -> SortOrder.RANDOM
                    },
                    descendSorting = galleryPrefs.descendSortOrder,
                    showImages = galleryPrefs.showImages,
                    showVideos = galleryPrefs.showVideos,
                    showGifs = galleryPrefs.showGifs,
                    showUnHidden = galleryPrefs.showUnHidden,
                    showHidden = galleryPrefs.showHidden,
                    showFiles = galleryPrefs.showFiles,
                    showAlbums = galleryPrefs.showAlbums
                )
            }
        }
    }
    
    
    private fun onLaunch() {
        viewModelScope.launch(Dispatchers.IO) { keepCorePrefsFlow() }
        viewModelScope.launch(Dispatchers.IO) { keepGalleryPrefsFlow() }
        
        viewModelScope.launch { reflectCorePreferencesFlow() }
        viewModelScope.launch { reflectGalleryPreferencesFlow() }
    }
    
    private fun onTabChanged(tab: Tab) {
        _state.update {
            it.copy(tab = tab)
        }
    }
    
    private suspend fun onModeChanged(mode: GalleryMode) {
        val newMode = when (mode) {
            GalleryMode.TREE -> GalleryPreferences.GalleryMode.TREE
            GalleryMode.PLAIN -> GalleryPreferences.GalleryMode.PLAIN
        }
        val newPrefs = _galleryPrefsFLow.value.copy(
            galleryMode = newMode
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onThemeChanged(theme: AppTheme) {
        val newTheme = when (theme) {
            AppTheme.SYSTEM -> CorePreferences.Theme.SYSTEM
            AppTheme.LIGHT -> CorePreferences.Theme.LIGHT
            AppTheme.DARK -> CorePreferences.Theme.DARK
        }
        val newPrefs = _corePrefsFLow.value.copy(
            theme = newTheme
        )
        corePreferencesRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onDynamicColorsChanged(colors: DynamicColors) {
        val newColors = when (colors) {
            DynamicColors.SYSTEM -> CorePreferences.DynamicColors.SYSTEM
            DynamicColors.DISABLED -> CorePreferences.DynamicColors.DISABLED
        }
        val newPrefs = _corePrefsFLow.value.copy(
            dynamicColors = newColors
        )
        corePreferencesRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onGridColumnsPortraitChanged(portrait: Int) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            portraitGridColumns = portrait
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onGridColumnsLandscapeChanged(landscape: Int) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            landscapeGridColumns = landscape
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onPlaceOnTopChanged(placeOnTop: PlaceOnTop) {
        val newOnTop = when (placeOnTop) {
            PlaceOnTop.NONE -> GalleryPreferences.PlaceOnTop.NONE
            PlaceOnTop.ALBUMS -> GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP
            PlaceOnTop.FILES -> GalleryPreferences.PlaceOnTop.FILES_ON_TOP
        }
        val newPrefs = _galleryPrefsFLow.value.copy(
            placeOnTop = newOnTop
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onSortOrderChanged(sortOrder: SortOrder) {
        val newSortOrder = when (sortOrder) {
            SortOrder.NAME -> GalleryPreferences.SortOrder.NAME
            SortOrder.DATE_CREATED -> GalleryPreferences.SortOrder.CREATION_DATE
            SortOrder.DATE_MODIFIED -> GalleryPreferences.SortOrder.MODIFICATION_DATE
            SortOrder.EXTENSION -> GalleryPreferences.SortOrder.EXTENSION
            SortOrder.SIZE -> GalleryPreferences.SortOrder.SIZE
            SortOrder.RANDOM -> GalleryPreferences.SortOrder.RANDOM
        }
        val newPrefs = _galleryPrefsFLow.value.copy(
            sortOrder = newSortOrder
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onDescendSortingChanged(descendSorting: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            descendSortOrder = descendSorting
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeImagesChanged(includeImages: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showImages = includeImages
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeVideosChanged(includeVideos: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showVideos = includeVideos
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeGifsChanged(includeGifs: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showGifs = includeGifs
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeHiddenChanged(includeHidden: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showHidden = includeHidden
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeUnHiddenChanged(includeUnHidden: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showUnHidden = includeUnHidden
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeFilesChanged(includeFiles: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showFiles = includeFiles
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
    private suspend fun onIncludeAlbumsChanged(includeAlbums: Boolean) {
        val newPrefs = _galleryPrefsFLow.value.copy(
            showAlbums = includeAlbums
        )
        galleryPrefsRepo.savePreferences(newPrefs)
    }
    
}
