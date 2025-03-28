package nikmax.material_tree.gallery.dialogs.album_picker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nikmax.gallery.core.data.Resource
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.createItemsListToDisplay
import javax.inject.Inject

@HiltViewModel
class AlbumPickerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val itemsRepo: MediaItemsRepo
) : ViewModel() {
    
    data class UiState(
        val items: List<MediaItemUI> = listOf(),
        val loading: Boolean = false,
        val selectedAlbum: MediaItemUI.Album? = null
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
    
    
    private val _navEntriesFlow = MutableStateFlow(listOf<MediaItemUI.Album?>())
    
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    
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
        resetNavStack()
        observeFlows()
    }
    
    private fun resetNavStack() {
        _navEntriesFlow.update { listOf(null) }
    }
    
    private fun navigateIn(album: MediaItemUI.Album?) {
        _navEntriesFlow.update { it + album }
    }
    
    private suspend fun navigateBack() {
        when (_navEntriesFlow.value.isNotEmpty()) {
            true -> _navEntriesFlow.update { it.dropLast(1) }
            false -> _event.emit(Event.DismissDialog)
        }
    }
    
    private suspend fun onRefresh() {
        itemsRepo.rescan()
    }
    
    private suspend fun observeFlows() {
        combine(
            itemsRepo.getFilesResourceFlow(),
            GalleryPreferencesUtils.getPreferencesFlow(context),
            _navEntriesFlow
        ) { filesRes, prefs, navStack ->
            val newItems = when (filesRes) {
                is Resource.Success -> filesRes.data
                is Resource.Loading -> filesRes.data
                is Resource.Error -> TODO()
            }.createItemsListToDisplay(
                targetAlbumPath = navStack.lastOrNull()?.path,
                treeModeEnabled = prefs.appearance.nestedAlbumsEnabled,
                includeImages = prefs.filtering.includeImages,
                includeVideos = prefs.filtering.includeVideos,
                includeGifs = prefs.filtering.includeGifs,
                includeUnHidden = prefs.filtering.includeUnHidden,
                includeHidden = prefs.filtering.includeHidden,
                includeFiles = prefs.filtering.includeFiles,
                includeAlbums = prefs.filtering.includeAlbums,
                sortingOrder = prefs.sorting.order,
                descendSorting = prefs.sorting.descend,
                searchQuery = null
            )
            _state.value.copy(
                items = newItems,
                loading = filesRes is Resource.Loading,
                selectedAlbum = navStack.lastOrNull()
            )
        }.collectLatest { newState ->
            _state.update { newState }
        }
    }
}
