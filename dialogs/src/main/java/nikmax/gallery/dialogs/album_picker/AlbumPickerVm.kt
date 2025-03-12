package nikmax.gallery.dialogs.album_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nikmax.gallery.core.ItemsUtils.createItemsListToDisplay
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.MediaFileData
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.GalleryPreferences
import nikmax.gallery.data.preferences.PreferencesRepo
import javax.inject.Inject

@HiltViewModel
class AlbumPickerVm
@Inject constructor(
    private val itemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {

    data class UiState(
        val items: List<MediaItemUI> = listOf(),
        val loading: Boolean = false,
        val preferences: GalleryPreferences = GalleryPreferences()
    )

    sealed interface UserAction {
        data class Launch(val initialAlbumPath: String?) : UserAction
        data class NavigateIn(val albumPath: String) : UserAction
        data object NavigateBack : UserAction
        data object Refresh : UserAction
        data object Confirm : UserAction
    }

    sealed interface Event {
        data object DismissDialog : Event
        data class ConfirmDialog(val selectedPath: String) : Event
    }


    private val _navStack = MutableStateFlow(listOf<String?>())

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch(action.initialAlbumPath)
                is UserAction.NavigateIn -> navigateIn(action.albumPath)
                UserAction.NavigateBack -> navigateBack()
                UserAction.Refresh -> onRefresh()
                UserAction.Confirm -> onConfirm()
            }
        }
    }


    private suspend fun onLaunch(initialAlbumPath: String?) {
        _navStack.update { emptyList() }
        navigateIn(initialAlbumPath)
        observeFlows()
    }

    private fun navigateIn(albumPath: String?) {
        _navStack.update { it + albumPath }
    }

    private suspend fun navigateBack() {
        when (_navStack.value.isNotEmpty()) {
            true -> _navStack.update { it.dropLast(1) }
            false -> _event.emit(Event.DismissDialog)
        }
    }

    private suspend fun onRefresh() {
        itemsRepo.rescan()
    }

    private suspend fun onConfirm() {
        val path = _navStack.value.lastOrNull()
        if (path != null) _event.emit(Event.ConfirmDialog(path))
    }

    private suspend fun observeFlows() {
        combine(
            itemsRepo.getFilesResourceFlow(),
            prefsRepo.getPreferencesFlow(),
            _navStack
        ) { filesRes, prefs, navStack ->
            val currentPath = navStack.lastOrNull()
            val newItems = createItemsList(
                filesResource = filesRes,
                albumPath = currentPath,
                albumsMode = prefs.albumsMode,
                sortingOrder = prefs.sortingOrder,
                descendSortingEnabled = prefs.descendSorting,
                selectedFilters = prefs.enabledFilters,
                showHidden = prefs.showHidden
            )
            _state.value.copy(
                items = newItems,
                preferences = prefs,
                loading = filesRes is Resource.Loading
            )
        }.collectLatest { newState ->
            _state.update { newState }
        }
    }

    private fun createItemsList(
        filesResource: Resource<List<MediaFileData>>,
        albumPath: String?,
        albumsMode: GalleryPreferences.AlbumsMode,
        selectedFilters: Set<GalleryPreferences.Filter>,
        sortingOrder: GalleryPreferences.SortingOrder,
        descendSortingEnabled: Boolean,
        showHidden: Boolean
    ): List<MediaItemUI> {
        return when (filesResource) {
            is Resource.Success -> filesResource.data
            is Resource.Loading -> filesResource.data
            is Resource.Error -> TODO()
        }.createItemsListToDisplay(
            targetAlbumPath = albumPath,
            albumsMode = albumsMode,
            appliedFilters = selectedFilters,
            sortingOrder = sortingOrder,
            useDescendSorting = descendSortingEnabled,
            includeHidden = showHidden
        )
    }
}
