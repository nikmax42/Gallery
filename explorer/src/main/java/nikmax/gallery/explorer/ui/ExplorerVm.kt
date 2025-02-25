package nikmax.gallery.explorer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import kotlinx.serialization.json.Json
import nikmax.gallery.core.ItemsUtils.Mapping.mapDataFilesToUiFiles
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.applyFilters
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createAlbumOwnFilesList
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createFlatAlbumsList
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createNestedAlbumsList
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.excludeHidden
import nikmax.gallery.core.ItemsUtils.Sorting.applySorting
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.ConflictResolution
import nikmax.gallery.data.media.FileOperation
import nikmax.gallery.data.media.FileOperationWorker
import nikmax.gallery.data.media.MediaFileData
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.GalleryPreferences
import nikmax.gallery.data.preferences.PreferencesRepo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class ExplorerVm
@Inject constructor(
    private val mediaItemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {
    data class UIState(
        val screenItems: List<MediaItemUI> = emptyList(),
        val loading: Boolean = false,
        val preferences: GalleryPreferences = GalleryPreferences(),
        val mode: Mode = Mode.Viewing,
        val dialog: Dialog = Dialog.None
    ) {
        sealed interface Mode {
            data object Viewing : Mode

            data class Selection(val selectedItems: List<MediaItemUI>) : Mode

            data class Searching(
                val searchQuery: String = "",
                val foundedItems: List<MediaItemUI> = emptyList()
            ) : Mode
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
        data class Launch(val path: String?) : UserAction
        data object Refresh : UserAction
        data class SearchQueryChange(val newQuery: String) : UserAction
        data class Search(val query: String) : UserAction
        data class UpdatePreferences(val preferences: GalleryPreferences) : UserAction
        data class OpenItem(val item: MediaItemUI) : UserAction
        data class ChangeItemSelection(val item: MediaItemUI) : UserAction
        data object SelectAllItems : UserAction
        data object ClearSelection : UserAction
        data class Copy(val items: List<MediaItemUI>) : UserAction
        data class Move(val items: List<MediaItemUI>) : UserAction
        data class Rename(val items: List<MediaItemUI>) : UserAction
        data class Delete(val items: List<MediaItemUI>) : UserAction
    }

    sealed interface Event {
        data class OpenFile(val file: MediaItemUI.File) : Event
        data class OpenAlbum(val album: MediaItemUI.Album) : Event
    }

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch(action.path)
                UserAction.Refresh -> onRefresh()
                is UserAction.OpenItem -> openItem(action.item)
                is UserAction.SearchQueryChange -> onSearchQueryChange(action.newQuery)
                is UserAction.Search -> onSearch(action.query)
                is UserAction.UpdatePreferences -> updatePreferences(action.preferences)
                is UserAction.ChangeItemSelection -> changeItemSelection(action.item)
                UserAction.SelectAllItems -> selectAllItems()
                UserAction.ClearSelection -> clearSelection()
                is UserAction.Copy -> onCopyOrMove(action.items)
                is UserAction.Move -> onCopyOrMove(action.items, move = true)
                is UserAction.Rename -> onRename(action.items)
                is UserAction.Delete -> onDelete(action.items)
            }
        }
    }


    private fun onLaunch(albumPath: String?) {
        viewModelScope.launch { observeFilesAndPreferences(albumPath) }
        // initiate media scan if it's a first screen
        if (albumPath == null) viewModelScope.launch { onRefresh() }
    }

    private suspend fun onRefresh() {
        withContext(Dispatchers.IO) {
            mediaItemsRepo.rescan()
        }
    }

    private suspend fun observeFilesAndPreferences(albumPath: String?) {
        combine(
            mediaItemsRepo.getFilesFlow(),
            prefsRepo.getPreferencesFlow()
        ) { filesRes, prefs ->
            val newItems = createItemsList(
                filesResource = filesRes,
                albumPath = albumPath,
                albumsMode = prefs.albumsMode,
                sortingOrder = prefs.sortingOrder,
                descendSortingEnabled = prefs.descendSorting,
                selectedFilters = prefs.enabledFilters,
                showHidden = prefs.showHidden
            )
            _uiState.value.copy(
                screenItems = newItems,
                preferences = prefs,
                loading = filesRes is Resource.Loading
            )
        }.collectLatest { newState ->
            _uiState.update { newState }
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

        val filesData = when (filesResource) {
            is Resource.Success -> filesResource.data
            is Resource.Loading -> filesResource.data
            is Resource.Error -> TODO()
        }
        // convert files data models to files ui models
        val filesUi = filesData.mapDataFilesToUiFiles()
        // apply primary filtering based on albums mode and target path
        val itemsUi = when (albumsMode) {
            GalleryPreferences.AlbumsMode.PLAIN -> {
                when (albumPath == null) {
                    true -> filesUi.createFlatAlbumsList()
                    false -> filesUi.createAlbumOwnFilesList(albumPath)
                }
            }
            GalleryPreferences.AlbumsMode.NESTED -> filesUi.createNestedAlbumsList(
                albumPath = albumPath ?: "/storage/"
            )
        }.apply { if (!showHidden) this.excludeHidden() }
        // apply secondary filtering based on selected preferences
        val filteredItemsUi = itemsUi.applyFilters(selectedFilters = selectedFilters)
        // apply sorting based on selected preference
        val sortedItemsUi = filteredItemsUi.applySorting(
            sortingOrder = sortingOrder,
            descend = descendSortingEnabled
        )
        return sortedItemsUi
    }


    private suspend fun openItem(item: MediaItemUI) {
        val event = when (item) {
            is MediaItemUI.File -> Event.OpenFile(item)
            is MediaItemUI.Album -> Event.OpenAlbum(item)
        }
        _event.emit(event)
    }


    private fun changeItemSelection(item: MediaItemUI) {
        when (val mode = _uiState.value.mode) {
            UIState.Mode.Viewing,
            is UIState.Mode.Searching -> _uiState.update {
                it.copy(
                    mode = UIState.Mode.Selection(selectedItems = listOf(item))
                )
            }
            is UIState.Mode.Selection -> {
                val selectedItems = mode.selectedItems
                val newSelectedItems = when (selectedItems.contains(item)) {
                    true -> selectedItems - item
                    false -> selectedItems + item
                }
                val newState = when (newSelectedItems.isEmpty()) {
                    true -> _uiState.value.copy(mode = UIState.Mode.Viewing)
                    false -> _uiState.value.copy(
                        mode = UIState.Mode.Selection(selectedItems = newSelectedItems)
                    )
                }
                _uiState.update { newState }
            }
        }
    }

    private fun selectAllItems() {
        val newState = when (val mode = _uiState.value.mode) {
            UIState.Mode.Viewing, is UIState.Mode.Selection -> _uiState.value.copy(
                mode = UIState.Mode.Selection(selectedItems = _uiState.value.screenItems)
            )
            is UIState.Mode.Searching -> _uiState.value.copy(
                mode = UIState.Mode.Selection(selectedItems = mode.foundedItems)
            )
        }
        _uiState.update { newState }
    }

    private fun clearSelection() {
        // todo return to previous mode instead of viewing (to return back to search mode, for example)
        val mode = _uiState.value.mode
        if (mode is UIState.Mode.Selection) {
            _uiState.update {
                it.copy(mode = UIState.Mode.Viewing)
            }
        }
    }


    private fun onSearchQueryChange(newQuery: String) {
        when (newQuery.isEmpty()) {
            true -> onSearchCancel()
            false -> onSearch(newQuery)
        }
    }

    private fun onSearch(query: String) {
        val filteredItems = _uiState.value.screenItems.filter {
            it.path.contains(query, ignoreCase = true)
        }
        _uiState.update {
            it.copy(
                mode = UIState.Mode.Searching(
                    searchQuery = query,
                    foundedItems = filteredItems
                )
            )
        }
    }

    private fun onSearchCancel() {
        _uiState.update {
            it.copy(mode = UIState.Mode.Viewing)
        }
    }


    private suspend fun updatePreferences(preferences: GalleryPreferences) {
        prefsRepo.savePreferences(preferences)
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
        // turn of selection mode after operation performed
        _uiState.update { it.copy(mode = UIState.Mode.Viewing) }
        executeFileOperations(operations)
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
        _uiState.update { it.copy(mode = UIState.Mode.Viewing) }
        executeFileOperations(operations)
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
        _uiState.update { it.copy(mode = UIState.Mode.Viewing) }
        executeFileOperations(operations)
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
                                viewModelScope.launch { onRefresh() }
                                // todo post notification here
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Unable to parse operation object from work output")
                        }
                    }
                }
            }
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
}
