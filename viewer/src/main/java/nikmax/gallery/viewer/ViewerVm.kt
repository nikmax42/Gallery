package nikmax.gallery.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nikmax.gallery.core.ItemsUtils.Mapping.mapDataFileToUiFile
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.applyFilters
import nikmax.gallery.core.ItemsUtils.Sorting.applySorting
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.PreferencesRepo
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    private val itemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {

    data class UIState(
        val showControls: Boolean = true,
        val loading: Boolean = false,
        val content: Content = Content.Preparing
    ) {
        sealed interface Content {
            data object Preparing : Content
            data class Ready(val files: List<MediaItemUI.File> = emptyList()) : Content
        }
    }

    sealed interface UserAction {
        data class Launch(val filePath: String) : UserAction
        data object Refresh : UserAction
        data object SwitchControls : UserAction
    }

    sealed interface Event {
        /* todo add one-time events here */
    }

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch(filePath = action.filePath)
                UserAction.Refresh -> onRefresh()
                UserAction.SwitchControls -> onSwitchControls()
            }
        }
    }


    private suspend fun onLaunch(filePath: String) {
        observeAlbumContent(filePath)
    }

    private suspend fun observeAlbumContent(filePath: String) {
        val albumPath = Path(filePath).parent.pathString
        combine(
            itemsRepo.getFilesFlow(),
            prefsRepo.getPreferencesFlow()
        ) { itemsResource, prefs ->
            val albumFiles = when (itemsResource) {
                is Resource.Success -> itemsResource.data.map { it.mapDataFileToUiFile() }
                is Resource.Loading -> itemsResource.data.map { it.mapDataFileToUiFile() }
                is Resource.Error -> TODO() // todo show error message instead
            }.filter { Path(it.path).parent.pathString == albumPath }
            val filteredFiles = albumFiles.applyFilters(selectedFilters = prefs.enabledFilters)
            val sortedFiles = filteredFiles.applySorting(
                sortingOrder = prefs.sortingOrder,
                descend = prefs.descendSorting
            )
            UIState(
                content = UIState.Content.Ready(
                    files = sortedFiles.map { it as MediaItemUI.File }
                )
            )
        }.collectLatest { newState ->
            _uiState.update { newState }
        }
    }

    private suspend fun onRefresh() {
        itemsRepo.rescan()
    }

    private fun onSwitchControls() {
        _uiState.update {
            it.copy(showControls = it.showControls.not())
        }
    }
}
