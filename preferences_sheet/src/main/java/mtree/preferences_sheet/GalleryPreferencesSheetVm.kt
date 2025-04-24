package mtree.preferences_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesRepo
import javax.inject.Inject

@HiltViewModel
class GalleryPreferencesSheetVm
@Inject constructor(
    private val prefsRepo: MtreePreferencesRepo
) : ViewModel() {
    
    private val _preferences = prefsRepo
        .getPreferencesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MtreePreferences.default()
        )
    
    val uiState = _preferences.map { prefs ->
        UiState(
            mode = when (prefs.galleryMode) {
                MtreePreferences.GalleryMode.TREE -> GalleryMode.TREE
                MtreePreferences.GalleryMode.PLAIN -> GalleryMode.PLAIN
            },
            theme = when (prefs.theme) {
                MtreePreferences.Theme.SYSTEM -> AppTheme.SYSTEM
                MtreePreferences.Theme.LIGHT -> AppTheme.LIGHT
                MtreePreferences.Theme.DARK -> AppTheme.DARK
            },
            dynamicColors = when (prefs.dynamicColors) {
                MtreePreferences.DynamicColors.SYSTEM -> DynamicColors.SYSTEM
                MtreePreferences.DynamicColors.DISABLED -> DynamicColors.DISABLED
            },
            gridColumnsPortrait = prefs.portraitGridColumns,
            gridColumnsLandscape = prefs.landscapeGridColumns,
            placeOnTop = when (prefs.placeOnTop) {
                MtreePreferences.PlaceOnTop.ALBUMS_ON_TOP -> PlaceOnTop.ALBUMS
                MtreePreferences.PlaceOnTop.FILES_ON_TOP -> PlaceOnTop.FILES
                MtreePreferences.PlaceOnTop.NONE -> PlaceOnTop.NONE
            },
            sortOrder = when (prefs.sortOrder) {
                MtreePreferences.SortOrder.CREATION_DATE -> SortOrder.MODIFICATION_DATE
                MtreePreferences.SortOrder.MODIFICATION_DATE -> SortOrder.CREATIOIN_DATE
                MtreePreferences.SortOrder.NAME -> SortOrder.NAME
                MtreePreferences.SortOrder.EXTENSION -> SortOrder.EXTENSION
                MtreePreferences.SortOrder.SIZE -> SortOrder.SIZE
                MtreePreferences.SortOrder.RANDOM -> SortOrder.RANDOM
            },
            descendSorting = prefs.descendSortOrder,
            showImages = prefs.showImages,
            showVideos = prefs.showVideos,
            showGifs = prefs.showGifs,
            showHidden = prefs.showHidden,
            showUnHidden = prefs.showUnHidden,
            showFiles = prefs.showFiles,
            showAlbums = prefs.showAlbums
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.default()
    )
    
    fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
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
    
    
    private suspend fun onModeChanged(mode: GalleryMode) {
        val newMode = when (mode) {
            GalleryMode.TREE -> MtreePreferences.GalleryMode.TREE
            GalleryMode.PLAIN -> MtreePreferences.GalleryMode.PLAIN
        }
        val newPrefs = _preferences.value.copy(
            galleryMode = newMode
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onThemeChanged(theme: AppTheme) {
        val newTheme = when (theme) {
            AppTheme.SYSTEM -> MtreePreferences.Theme.SYSTEM
            AppTheme.LIGHT -> MtreePreferences.Theme.LIGHT
            AppTheme.DARK -> MtreePreferences.Theme.DARK
        }
        val newPrefs = _preferences.value.copy(
            theme = newTheme
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onDynamicColorsChanged(colors: DynamicColors) {
        val newColors = when (colors) {
            DynamicColors.SYSTEM -> MtreePreferences.DynamicColors.SYSTEM
            DynamicColors.DISABLED -> MtreePreferences.DynamicColors.DISABLED
        }
        val newPrefs = _preferences.value.copy(
            dynamicColors = newColors
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onGridColumnsPortraitChanged(portrait: Int) {
        val newPrefs = _preferences.value.copy(
            portraitGridColumns = portrait
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onGridColumnsLandscapeChanged(landscape: Int) {
        val newPrefs = _preferences.value.copy(
            landscapeGridColumns = landscape
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onPlaceOnTopChanged(placeOnTop: PlaceOnTop) {
        val newOnTop = when (placeOnTop) {
            PlaceOnTop.NONE -> MtreePreferences.PlaceOnTop.NONE
            PlaceOnTop.ALBUMS -> MtreePreferences.PlaceOnTop.ALBUMS_ON_TOP
            PlaceOnTop.FILES -> MtreePreferences.PlaceOnTop.FILES_ON_TOP
        }
        val newPrefs = _preferences.value.copy(
            placeOnTop = newOnTop
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onSortOrderChanged(sortOrder: SortOrder) {
        val newSortOrder = when (sortOrder) {
            SortOrder.NAME -> MtreePreferences.SortOrder.NAME
            SortOrder.MODIFICATION_DATE -> MtreePreferences.SortOrder.CREATION_DATE
            SortOrder.CREATIOIN_DATE -> MtreePreferences.SortOrder.MODIFICATION_DATE
            SortOrder.EXTENSION -> MtreePreferences.SortOrder.EXTENSION
            SortOrder.SIZE -> MtreePreferences.SortOrder.SIZE
            SortOrder.RANDOM -> MtreePreferences.SortOrder.RANDOM
        }
        val newPrefs = _preferences.value.copy(
            sortOrder = newSortOrder
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onDescendSortingChanged(descendSorting: Boolean) {
        val newPrefs = _preferences.value.copy(
            descendSortOrder = descendSorting
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeImagesChanged(includeImages: Boolean) {
        val newPrefs = _preferences.value.copy(
            showImages = includeImages
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeVideosChanged(includeVideos: Boolean) {
        val newPrefs = _preferences.value.copy(
            showVideos = includeVideos
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeGifsChanged(includeGifs: Boolean) {
        val newPrefs = _preferences.value.copy(
            showGifs = includeGifs
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeHiddenChanged(includeHidden: Boolean) {
        val newPrefs = _preferences.value.copy(
            showHidden = includeHidden
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeUnHiddenChanged(includeUnHidden: Boolean) {
        val newPrefs = _preferences.value.copy(
            showUnHidden = includeUnHidden
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeFilesChanged(includeFiles: Boolean) {
        val newPrefs = _preferences.value.copy(
            showFiles = includeFiles
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
    
    private suspend fun onIncludeAlbumsChanged(includeAlbums: Boolean) {
        val newPrefs = _preferences.value.copy(
            showAlbums = includeAlbums
        )
        prefsRepo.savePreferences(
            preferences = newPrefs,
        )
    }
}
