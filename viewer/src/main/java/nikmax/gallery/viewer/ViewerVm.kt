package nikmax.gallery.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nikmax.gallery.core.ItemsUtils.Mapping.mapDataFileToUiFile
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.applyFilters
import nikmax.gallery.core.ItemsUtils.Sorting.applySorting
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.ConflictResolution
import nikmax.gallery.data.media.FileOperation
import nikmax.gallery.data.media.FileOperationWorker
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.PreferencesRepo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.Path
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    private val mediaItemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {

    data class UIState(
        val showControls: Boolean = true,
        val loading: Boolean = false,
        val content: Content = Content.Preparing,
        val dialog: Dialog = Dialog.None
    ) {
        sealed interface Content {
            data object Preparing : Content
            data class Ready(val files: List<MediaItemUI.File> = emptyList()) : Content
        }

        sealed interface Dialog {
            data object None : Dialog

            data class DeletionConfirmation(
                val items: List<MediaItemUI>,
                val onConfirm: () -> Unit,
                val onDismiss: () -> Unit
            ) : Dialog

            data class Renaming(
                val item: MediaItemUI,
                val onConfirm: (newPath: String) -> Unit,
                val onDismiss: () -> Unit
            ) : Dialog

            data class AlbumPicker(
                val onConfirm: (selectedPath: String) -> Unit,
                val onDismiss: () -> Unit
            ) : Dialog

            data class ConflictResolver(
                val conflictItem: MediaItemUI,
                val onConfirm: (resolution: ConflictResolution) -> Unit,
                val onDismiss: () -> Unit
            ) : Dialog
        }
    }

    sealed interface UserAction {
        data class Launch(val filePath: String) : UserAction
        data object Refresh : UserAction
        data object SwitchControls : UserAction
        data class Copy(val file: MediaItemUI.File) : UserAction
        data class Move(val file: MediaItemUI.File) : UserAction
        data class Rename(val file: MediaItemUI.File) : UserAction
        data class Delete(val file: MediaItemUI.File) : UserAction
        data class Share(val file: MediaItemUI.File) : UserAction
    }

    sealed interface Event {
        data object CloseViewer : Event
    }

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch(initialFilePath = action.filePath)
                UserAction.Refresh -> refresh()
                UserAction.SwitchControls -> onSwitchControls()
                is UserAction.Copy -> onCopyOrMove(listOf(action.file))
                is UserAction.Move -> onCopyOrMove(listOf(action.file))
                is UserAction.Rename -> onRename(listOf(action.file))
                is UserAction.Delete -> onDelete(listOf(action.file))
                is UserAction.Share -> onShare(action.file)
            }
        }
    }


    private suspend fun onLaunch(initialFilePath: String) {
        observeAlbumContent(initialFilePath)
    }

    private suspend fun observeAlbumContent(initialFilePath: String) {
        val albumPath = Path(initialFilePath).parent.pathString
        combine(
            mediaItemsRepo.getFilesFlow(),
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
            // close viewer if files list is empty after refreshing
            if (newState.content is UIState.Content.Ready && newState.content.files.isEmpty()) {
                _event.emit(Event.CloseViewer)
            }
        }
    }


    private suspend fun refresh() {
        mediaItemsRepo.rescan()
    }


    private fun onSwitchControls() {
        _uiState.update {
            it.copy(showControls = it.showControls.not())
        }
    }


    private suspend fun onCopyOrMove(items: List<MediaItemUI>, move: Boolean = false) {
        val destinationAlbumPath = try {
            awaitForDestinationPath()
        } catch (e: CancellationException) {
            return
        }
        val operations = items.map { item ->
            val destinationFilePath = "$destinationAlbumPath/${item.name}"
            val conflicts = mediaItemsRepo.checkExistence(destinationFilePath)
            val resolution = when (conflicts) {
                true -> try {
                    awaitForConflictResolution(item)
                } catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
            when (move) {
                true -> FileOperation.Move(item.path, destinationFilePath, resolution)
                false -> FileOperation.Copy(item.path, destinationFilePath, resolution)
            }
        }
        // todo swipe to the next or previous file or close viewer if file moved
        executeFileOperations(operations = operations)
    }

    private suspend fun onRename(items: List<MediaItemUI>) {
        val operations = items.map { item ->
            val newPath = try {
                awaitForNewPath(item = item)
            } catch (e: CancellationException) {
                return
            }
            val conflicts = mediaItemsRepo.checkExistence(newPath)
            val resolution = when (conflicts) {
                true -> try {
                    awaitForConflictResolution(item)
                } catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
            FileOperation.Rename(item.path, newPath, resolution)
        }
        executeFileOperations(operations = operations)
    }

    private suspend fun onDelete(items: List<MediaItemUI>) {
        val deletionConfirmed = try {
            awaitForDeletionConfirmation(items = items)
        } catch (e: CancellationException) {
            false
        }
        val operations = when (deletionConfirmed) {
            true -> items.map { FileOperation.Delete(it.path) }
            false -> return
        }
        executeFileOperations(operations = operations)
    }

    private suspend fun executeFileOperations(operations: List<FileOperation>) {
        val completeOperations = mutableSetOf<FileOperation>()
        mediaItemsRepo
            .executeFileOperations(operations = operations)
            .observeForever {
                it.forEach { workInfo ->
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        try {
                            val operationJson = workInfo.outputData.getString(
                                FileOperationWorker.Keys.FILE_OPERATION_JSON.name
                            )
                            val operation = Json.decodeFromString<FileOperation>(operationJson!!)
                            if (!completeOperations.contains(operation)) {
                                completeOperations += operation
                                viewModelScope.launch { refresh() }
                                // todo post notification here
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Unable to parse operation object from work output")
                        }
                    }
                }
            }
    }


    private suspend fun onShare(file: MediaItemUI.File) {
        TODO("Implement file sharing using file URI")
    }


    private suspend fun awaitForNewPath(item: MediaItemUI): String = suspendCoroutine { cont ->
        _uiState.update {
            it.copy(
                dialog = UIState.Dialog.Renaming(
                    item = item,
                    onConfirm = { newPath ->
                        setDialog(UIState.Dialog.None)
                        cont.resume(newPath)
                    },
                    onDismiss = {
                        setDialog(UIState.Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private suspend fun awaitForDeletionConfirmation(items: List<MediaItemUI>): Boolean =
        suspendCoroutine { cont ->
            setDialog(
                UIState.Dialog.DeletionConfirmation(
                    items = items,
                    onConfirm = {
                        setDialog(UIState.Dialog.None)
                        cont.resume(true)
                    },
                    onDismiss = {
                        setDialog(UIState.Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }

    private suspend fun awaitForDestinationPath(): String = suspendCoroutine { cont ->
        setDialog(
            UIState.Dialog.AlbumPicker(
                onConfirm = { selectedPath ->
                    setDialog(UIState.Dialog.None)
                    cont.resume(selectedPath)
                },
                onDismiss = {
                    setDialog(UIState.Dialog.None)
                    cont.resumeWithException(CancellationException())
                }
            )
        )
    }

    private suspend fun awaitForConflictResolution(conflictItem: MediaItemUI): ConflictResolution =
        suspendCoroutine { cont ->
            setDialog(
                UIState.Dialog.ConflictResolver(
                    conflictItem = conflictItem,
                    onConfirm = { resolution ->
                        setDialog(UIState.Dialog.None)
                        cont.resume(resolution)
                    },
                    onDismiss = {
                        setDialog(UIState.Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }

    private fun setDialog(dialog: UIState.Dialog) {
        _uiState.update { it.copy(dialog = dialog) }
    }


    private suspend fun closeViewer() {
        _event.emit(Event.CloseViewer)
    }

    private fun switchToTheNearestFile() {
    }
}
