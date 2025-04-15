package mtree.preferences_sheet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesUtils
import javax.inject.Inject

@HiltViewModel
class GalleryPreferencesSheetVm
@Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _preferences = MtreePreferencesUtils
        .getPreferencesFlow(context)
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
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
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
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
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
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onGridColumnsPortraitChanged(portrait: Int) {
        val newPrefs = _preferences.value.copy(
            portraitGridColumns = portrait
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onGridColumnsLandscapeChanged(landscape: Int) {
        val newPrefs = _preferences.value.copy(
            landscapeGridColumns = landscape
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
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
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
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
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onDescendSortingChanged(descendSorting: Boolean) {
        val newPrefs = _preferences.value.copy(
            descendSortOrder = descendSorting
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeImagesChanged(includeImages: Boolean) {
        val newPrefs = _preferences.value.copy(
            showImages = includeImages
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeVideosChanged(includeVideos: Boolean) {
        val newPrefs = _preferences.value.copy(
            showVideos = includeVideos
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeGifsChanged(includeGifs: Boolean) {
        val newPrefs = _preferences.value.copy(
            showGifs = includeGifs
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeHiddenChanged(includeHidden: Boolean) {
        val newPrefs = _preferences.value.copy(
            showHidden = includeHidden
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeUnHiddenChanged(includeUnHidden: Boolean) {
        val newPrefs = _preferences.value.copy(
            showUnHidden = includeUnHidden
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeFilesChanged(includeFiles: Boolean) {
        val newPrefs = _preferences.value.copy(
            showFiles = includeFiles
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
    
    private suspend fun onIncludeAlbumsChanged(includeAlbums: Boolean) {
        val newPrefs = _preferences.value.copy(
            showAlbums = includeAlbums
        )
        MtreePreferencesUtils.savePreferences(
            preferences = newPrefs,
            context = context
        )
    }
}
