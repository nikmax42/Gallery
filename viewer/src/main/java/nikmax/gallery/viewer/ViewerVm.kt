package nikmax.gallery.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nikmax.gallery.core.MediaItemDataToUiMapper
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.MediaItemsRepo
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    private val mediaItemsRepo: MediaItemsRepo
) : ViewModel() {

    data class UIState(val content: Content = Content.Preparing) {
        sealed interface Content {
            data object Preparing : Content
            data class Ready(
                val files: List<MediaItemUI.File> = emptyList(),
                val showControls: Boolean = true,
                val loading: Boolean = false
            ) : Content
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
        observeItems(filePath)
    }

    private suspend fun observeItems(filePath: String) {
        val albumPath = Path(filePath).parent.pathString
        mediaItemsRepo
            .getFilesPlacedOnPath(albumPath)
            .collectLatest { res ->
                val files = when (res) {
                    is Resource.Success -> res.data.map { MediaItemDataToUiMapper.mapToFile(it) }
                    is Resource.Loading -> res.data.map { MediaItemDataToUiMapper.mapToFile(it) }
                    is Resource.Error -> emptyList() // todo show error message instead
                }
                _uiState.update {
                    it.copy(
                        content = UIState.Content.Ready(
                            files = files,
                            loading = res is Resource.Loading
                        )
                    )
                }
            }
    }

    private suspend fun onRefresh() {
        mediaItemsRepo.rescan()
    }

    private suspend fun onSwitchControls() {
        when (val content = _uiState.value.content) {
            UIState.Content.Preparing -> {}
            is UIState.Content.Ready -> _uiState.update {
                it.copy(
                    content = content.copy(
                        showControls = content.showControls.not()
                    )
                )
            }
        }

    }
}
