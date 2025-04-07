package nikmax.material_tree.gallery.dialogs.album_picker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nikmax.gallery.core.data.Resource
import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferences.GalleryMode
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferencesRepo
import nikmax.gallery.gallery.core.mappers.MediaItemMapper.mapToUi
import nikmax.gallery.gallery.core.ui.MediaItemUI
import javax.inject.Inject

@HiltViewModel
class AlbumPickerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val galleryRepo: MediaItemsRepo,
    private val prefsRepo: GalleryPreferencesRepo
) : ViewModel() {
    
    private val _navStackFlow = MutableStateFlow(listOf<MediaItemUI.Album?>())
    private var _dataResourceFlow: MutableStateFlow<Resource<List<MediaItemData>>> = MutableStateFlow(
        Resource.Loading(emptyList())
    )
    private val _galleryPreferencesFlow = MutableStateFlow(GalleryPreferences())
    
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(false)
    
    private val _uiState = MutableStateFlow(UiState())
    internal val state = _uiState.asStateFlow()
    
    private val _event = MutableSharedFlow<Event>()
    internal val event = _event.asSharedFlow()
    
    internal fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Launch -> onLaunch()
                is Action.NavigateIn -> navigateIn(action.album)
                Action.NavigateBack -> navigateBack()
                Action.Refresh -> onRefresh()
            }
        }
    }
    
    
    private fun onLaunch() {
        viewModelScope.launch { resetNavStack() }
        
        viewModelScope.launch { keepDataFlow() }
        viewModelScope.launch { keepPreferencesFlow() }
        viewModelScope.launch { keepLoadingFlow() }
        viewModelScope.launch { keepItemsFlow() }
        
        viewModelScope.launch { reflectLoadingFlowChanges() }
        viewModelScope.launch { reflectItemsFlowChanges() }
        viewModelScope.launch { reflectNavStackFlowChanges() }
    }
    
    private fun resetNavStack() {
        _navStackFlow.update { listOf(null) }
    }
    
    private fun navigateIn(album: MediaItemUI.Album?) {
        _navStackFlow.update { it + album }
    }
    
    private suspend fun navigateBack() {
        when (_navStackFlow.value.isNotEmpty()) {
            true -> _navStackFlow.update { it.dropLast(1) }
            false -> _event.emit(Event.DismissDialog)
        }
    }
    
    private suspend fun onRefresh() {
        galleryRepo.rescan()
    }
    
    
    private suspend fun keepDataFlow() {
        combine(
            _galleryPreferencesFlow,
            _navStackFlow,
        ) { prefs, navStack ->
            galleryRepo.getAlbumContentFlow(
                path = navStack.lastOrNull()?.path,
                searchQuery = null,
                treeMode = prefs.galleryMode === GalleryMode.TREE,
                includeImages = prefs.showImages,
                includeVideos = prefs.showVideos,
                includeGifs = prefs.showGifs,
                includeUnhidden = prefs.showUnHidden,
                includeHidden = prefs.showHidden,
                includeFiles = prefs.showFiles,
                includeAlbums = prefs.showAlbums,
                sortingOrder = when (prefs.sortOrder) {
                    GalleryPreferences.SortOrder.CREATION_DATE -> MediaItemsRepo.SortOrder.CREATION_DATE
                    GalleryPreferences.SortOrder.MODIFICATION_DATE -> MediaItemsRepo.SortOrder.MODIFICATION_DATE
                    GalleryPreferences.SortOrder.NAME -> MediaItemsRepo.SortOrder.NAME
                    GalleryPreferences.SortOrder.EXTENSION -> MediaItemsRepo.SortOrder.EXTENSION
                    GalleryPreferences.SortOrder.SIZE -> MediaItemsRepo.SortOrder.SIZE
                    GalleryPreferences.SortOrder.RANDOM -> MediaItemsRepo.SortOrder.RANDOM
                },
                descendSorting = prefs.descendSortOrder,
                albumsFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP,
                filesFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.FILES_ON_TOP
            )
        }.collectLatest { newDataFlow ->
            newDataFlow.collectLatest { newData ->
                _dataResourceFlow.update { newData }
            }
        }
    }
    
    private suspend fun keepPreferencesFlow() {
        prefsRepo
            .getPreferencesFlow()
            .collectLatest { prefs ->
                _galleryPreferencesFlow.update { prefs }
            }
    }
    
    private suspend fun keepLoadingFlow() {
        _dataResourceFlow.collectLatest { resource ->
            _isLoadingFlow.update {
                resource is Resource.Loading
            }
        }
    }
    
    private suspend fun keepItemsFlow() {
        combine(_dataResourceFlow, _galleryPreferencesFlow) { data, prefs ->
            when (data) {
                is Resource.Success -> data.data
                is Resource.Loading -> data.data
                is Resource.Error -> emptyList()
            }.mapToUi()
        }.collectLatest { newItemsList ->
            _itemsFlow.update { newItemsList }
        }
    }
    
    
    private suspend fun reflectLoadingFlowChanges() {
        _isLoadingFlow.collectLatest { isLoading ->
            _uiState.update {
                it.copy(
                    loading = isLoading
                )
            }
        }
    }
    
    private suspend fun reflectItemsFlowChanges() {
        _itemsFlow.collectLatest { newItems ->
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        items = newItems,
                    )
                }
            }
        }
    }
    
    private suspend fun reflectNavStackFlowChanges() {
        _navStackFlow.collectLatest { newNavStack ->
            val currentDirectory = newNavStack.lastOrNull()
            val isWritable = galleryRepo.checkWriteAccess(currentDirectory?.path.toString())
            _uiState.update {
                it.copy(
                    pickedAlbum = currentDirectory,
                    pickedAlbumIsNotWritable = !isWritable
                )
            }
        }
    }
}
