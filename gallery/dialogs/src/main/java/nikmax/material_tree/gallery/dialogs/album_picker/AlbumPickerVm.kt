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
import nikmax.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.applyFilters
import nikmax.gallery.gallery.core.utils.ItemsUtils.applySorting
import nikmax.gallery.gallery.core.utils.ItemsUtils.mapToUi
import javax.inject.Inject

@HiltViewModel
class AlbumPickerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val galleryRepo: MediaItemsRepo
) : ViewModel() {
    
    data class UiState(
        val items: List<MediaItemUI> = listOf(),
        val loading: Boolean = false,
        val pickedAlbum: MediaItemUI.Album? = null,
        val pickedAlbumIsNotWritable: Boolean = true
    )
    
    sealed interface UserAction {
        data object Launch : UserAction
        data class NavigateIn(val album: MediaItemUI.Album) : UserAction
        data object NavigateBack : UserAction
        data object Refresh : UserAction
    }
    
    sealed interface Event {
        data object DismissDialog : Event
    }
    
    
    private val _navStackFlow = MutableStateFlow(listOf<MediaItemUI.Album?>())
    private var _dataResourceFlow: MutableStateFlow<Resource<List<MediaItemData>>> =
        MutableStateFlow(Resource.Loading(emptyList()))
    private val _galleryPreferencesFlow = GalleryPreferencesUtils.getPreferencesFlow(context)
    
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(false)
    
    private val _uiState = MutableStateFlow(UiState())
    val state = _uiState.asStateFlow()
    
    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()
    
    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch()
                is UserAction.NavigateIn -> navigateIn(action.album)
                UserAction.NavigateBack -> navigateBack()
                UserAction.Refresh -> onRefresh()
            }
        }
    }
    
    
    private suspend fun onLaunch() {
        viewModelScope.launch { resetNavStack() }
        
        viewModelScope.launch { keepDataFlowUpdated() }
        viewModelScope.launch { keepLoadingFlowUpdated() }
        viewModelScope.launch { keepItemsFlowUpdated() }
        
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
    
    
    private suspend fun keepDataFlowUpdated() {
        combine(
            _galleryPreferencesFlow,
            _navStackFlow,
        ) { prefs, navStack ->
            galleryRepo.getAlbumContentFlow(
                path = navStack.lastOrNull()?.path,
                searchQuery = null,
                treeMode = prefs.appearance.nestedAlbumsEnabled
            )
        }.collectLatest { newDataFlow ->
            newDataFlow.collectLatest { newData ->
                _dataResourceFlow.update { newData }
            }
        }
    }
    
    private suspend fun keepLoadingFlowUpdated() {
        _dataResourceFlow.collectLatest { resource ->
            _isLoadingFlow.update {
                resource is Resource.Loading
            }
        }
    }
    
    private suspend fun keepItemsFlowUpdated() {
        combine(_dataResourceFlow, _galleryPreferencesFlow) { data, prefs ->
            when (data) {
                is Resource.Success -> data.data
                is Resource.Loading -> data.data
                is Resource.Error -> emptyList()
            }.mapToUi()
                .applyFilters(
                    includeImages = prefs.filtering.includeImages,
                    includeVideos = prefs.filtering.includeVideos,
                    includeGifs = prefs.filtering.includeGifs,
                    includeUnHidden = prefs.filtering.includeUnHidden,
                    includeHidden = prefs.filtering.includeHidden,
                    includeFiles = prefs.filtering.includeFiles,
                    includeAlbums = prefs.filtering.includeAlbums
                )
                .applySorting(
                    sortingOrder = prefs.sorting.order,
                    descend = prefs.sorting.descend,
                    albumsFirst = prefs.sorting.onTop == GalleryPreferences.Sorting.OnTop.ALBUMS_ON_TOP,
                    filesFirst = prefs.sorting.onTop == GalleryPreferences.Sorting.OnTop.FILES_ON_TOP
                )
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
