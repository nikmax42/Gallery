package nikmax.gallery.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nikmax.gallery.core.ItemsUtils.Mapping.mapDataFilesToUiFiles
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.applyFilters
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createAlbumOwnFilesList
import nikmax.gallery.core.ItemsUtils.Sorting.applySorting
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.ConflictResolution
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.PreferencesRepo
import nikmax.gallery.dialogs.Dialog
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class NewViewerVm
@Inject constructor(
    private val itemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {

    data class UIState(
        val showControls: Boolean = true,
        val loading: Boolean = false,
        val content: Content = Content.AlbumLoading,
        val dialog: Dialog = Dialog.None
    ) {
        sealed interface Content {
            data object AlbumLoading : Content
            data class Ready(val files: List<MediaItemUI.File> = emptyList()) : Content
        }
    }


    sealed interface UserAction {

    }


    sealed interface Event {

    }


    // Raw data flows
    private val _appPreferencesFlow = prefsRepo.getPreferencesFlow()
    private val _dataResourceFlow = itemsRepo.getFilesResourceFlow()

    // UI-related data flows
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()


    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.Launch -> {}
        }
    }


    private fun onLaunch(albumPath: String) {
        viewModelScope.launch { keepItemsFlowUpdated(albumPath) }
        viewModelScope.launch { keepLoadingFlowUpdated() }
    }


    private suspend fun onCopyOrMove(move: Boolean = false) {
    }

    private suspend fun onRename() {}
    private suspend fun onDelete() {}
    private suspend fun onShare() {}


    private suspend fun keepItemsFlowUpdated(albumPath: String) {
        combine(_dataResourceFlow, _appPreferencesFlow) { dataRes, prefs ->
            val allFilesData = when (dataRes) {
                is Resource.Success -> dataRes.data
                is Resource.Loading -> dataRes.data
                is Resource.Error -> emptyList()
            }
            val allFilesUi = allFilesData.mapDataFilesToUiFiles()
            val albumRelatedItems = allFilesUi.createAlbumOwnFilesList(albumPath)
            val filteredItems = albumRelatedItems.applyFilters(prefs.enabledFilters)
            val sortedItems = filteredItems.applySorting(prefs.sortingOrder, prefs.descendSorting)
            sortedItems
        }.collectLatest { actualItemsList ->
            _itemsFlow.update { actualItemsList }
        }
    }

    private suspend fun keepLoadingFlowUpdated() {
        _dataResourceFlow.collectLatest { filesDataResource ->
            _isLoadingFlow.update {
                filesDataResource is Resource.Loading
            }
        }
    }


    private suspend fun reflectItemsChanges() {
        _itemsFlow.collectLatest { newItems ->
            withContext(Dispatchers.Main) {
                // todo update items list
                //  keep focus on already opened item if it still exists
                //  swipe to the nearest existing otherwise
                /* _uiState.update {
                    it.copy(
                        items = newItems,
                    )
                } */
            }
        }
    }

    private suspend fun reflectLoadingChanges() {
        _isLoadingFlow.collectLatest { isLoading ->
            withContext(Dispatchers.Main) {
                // todo reflect loading state in UI
                //_uiState.update { it.copy(isLoading = isLoading) }
            }
        }
    }


    private suspend fun awaitForAlbumPickerResult(): String {
        return suspendCoroutine { cont ->
            setDialog(
                Dialog.AlbumPicker(
                    onConfirm = { pickedAlbumPath ->
                        setDialog(Dialog.None)
                        cont.resume(pickedAlbumPath)
                    },
                    onDismiss = {
                        setDialog(Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private suspend fun awaitForRenamerDialogResult(itemToRename: MediaItemUI): String {
        return suspendCoroutine { cont ->
            _uiState.update {
                it.copy(
                    dialog = Dialog.Renaming(
                        item = itemToRename,
                        onConfirm = { newPath ->
                            setDialog(Dialog.None)
                            cont.resume(newPath)
                        },
                        onDismiss = {
                            setDialog(Dialog.None)
                            cont.resumeWithException(CancellationException())
                        }
                    )
                )
            }
        }
    }

    private suspend fun awaitForDeletionConfirmationDialogResult(itemsToDelete: List<MediaItemUI>): Boolean {
        return suspendCoroutine { cont ->
            setDialog(
                Dialog.DeletionConfirmation(
                    items = itemsToDelete,
                    onConfirm = {
                        setDialog(Dialog.None)
                        cont.resume(true)
                    },
                    onDismiss = {
                        setDialog(Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private suspend fun awaitForConflictResolverDialogResult(conflictItem: MediaItemUI): ConflictResolution {
        return suspendCoroutine { cont ->
            setDialog(
                Dialog.ConflictResolver(
                    conflictItem = conflictItem,
                    onConfirm = { resolution ->
                        setDialog(Dialog.None)
                        cont.resume(resolution)
                    },
                    onDismiss = {
                        setDialog(Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private fun setDialog(newDialog: Dialog) {
        _uiState.update { it.copy(dialog = newDialog) }
    }
}
