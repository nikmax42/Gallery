package nikmax.gallery.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createFlatAlbumsList
import nikmax.gallery.core.ItemsUtils.SearchingAndFiltering.createNestedAlbumsList
import nikmax.gallery.core.ItemsUtils.Sorting.applySorting
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.ConflictResolution
import nikmax.gallery.data.media.FileOperation
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.GalleryPreferences
import nikmax.gallery.data.preferences.PreferencesRepo
import nikmax.gallery.dialogs.Dialog
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
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
        val items: List<MediaItemUI> = emptyList(),
        val selectedItems: List<MediaItemUI> = emptyList(),
        val isLoading: Boolean = false,
        val appPreferences: GalleryPreferences = GalleryPreferences(),
        val content: Content = Content.Exploring,
        val dialog: Dialog = Dialog.None,
        val error: Error = Error.None
    ) {
        sealed interface Content {
            data object Exploring : Content

            data class Searching(
                val searchQuery: String,
                val foundItems: List<MediaItemUI>
            ) : Content
        }

        sealed interface Error {
            data object None : Error
            data class PermissionNotGranted(val onGrantClick: () -> Unit)
            data class NoItemsToDisplayFound(val onRefreshClick: () -> Unit)
        }
    }


    sealed interface UserAction {
        data class ScreenLaunch(val folderPath: String?) : UserAction
        data object Refresh : UserAction
        data class SearchQueryChange(val newQuery: String?) : UserAction
        data class ItemsSelectionChange(val newSelection: List<MediaItemUI>) : UserAction
        data class PreferencesChange(val newPreferences: GalleryPreferences) : UserAction
        data class ItemsCopy(val itemsToCopy: List<MediaItemUI>) : UserAction
        data class ItemsMove(val itemsToMove: List<MediaItemUI>) : UserAction
        data class ItemsRename(val itemsToRename: List<MediaItemUI>) : UserAction
        data class ItemsDelete(val itemsToDelete: List<MediaItemUI>) : UserAction
    }


    // Raw data flows
    private val _appPreferencesFlow = prefsRepo.getPreferencesFlow()
    private val _dataResourceFlow = mediaItemsRepo.getFilesResourceFlow()

    // UI-related data flows
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(false)
    private val _searchQueryFlow = MutableStateFlow<String?>(null)
    private val _selectedItemsFlow = MutableStateFlow(emptyList<MediaItemUI>())

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()


    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.ScreenLaunch -> onLaunch(action.folderPath)
                UserAction.Refresh -> onRefresh()
                is UserAction.SearchQueryChange -> onSearchQueryChange(action.newQuery)
                is UserAction.PreferencesChange -> onPreferencesChange(action.newPreferences)
                is UserAction.ItemsSelectionChange -> onSelectionChange(action.newSelection)
                is UserAction.ItemsCopy -> onCopyOrMove(action.itemsToCopy)
                is UserAction.ItemsMove -> onCopyOrMove(action.itemsToMove, move = true)
                is UserAction.ItemsRename -> onRename(action.itemsToRename)
                is UserAction.ItemsDelete -> onDelete(action.itemsToDelete)
            }
        }
    }


    private fun onLaunch(folderPath: String?) {
        /*  if (folderPath == null) */ viewModelScope.launch { onRefresh() }
        // observe raw data flows changes
        viewModelScope.launch { keepItemsFlowUpdated(folderPath) }
        viewModelScope.launch { keepLoadingFlowUpdated() }
        // reflect flows changes in UI
        viewModelScope.launch { reflectItemsChanges() }
        viewModelScope.launch { reflectLoadingChanges() }
        viewModelScope.launch { reflectPreferencesChanges() }
        viewModelScope.launch { reflectSelectedItemsChanges() }
        viewModelScope.launch { reflectSearchQueryChanges() }
    }

    private suspend fun onRefresh() {
        mediaItemsRepo.rescan()
    }

    private fun onSearchQueryChange(newQuery: String?) {
        _searchQueryFlow.update { newQuery }
    }

    private fun onSelectionChange(newSelection: List<MediaItemUI>) {
        _selectedItemsFlow.update { newSelection }
    }

    private suspend fun onPreferencesChange(newPreferences: GalleryPreferences) {
        prefsRepo.savePreferences(newPreferences)
    }

    private suspend fun onCopyOrMove(itemsToCopyOrMove: List<MediaItemUI>, move: Boolean = false) {
        // pick destination album
        val destinationAlbumPath = try {
            awaitForAlbumPickerResult()
        } catch (e: CancellationException) {
            return
        }
        val destinationFilesPaths = itemsToCopyOrMove.map { item ->
            "$destinationAlbumPath/${item.name}"
        }
        // check for conflict filenames in the picked album
        // await for conflict resolutions
        val conflictsResolutions = destinationFilesPaths.mapIndexed { index, destinationPath ->
            val alreadyExists = mediaItemsRepo.checkExistence(destinationPath)
            when (alreadyExists) {
                true -> try {
                    awaitForConflictResolverDialogResult(itemsToCopyOrMove[index])
                } catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
        }
        val fileOperations = itemsToCopyOrMove.mapIndexed { index, item ->
            when (move) {
                true -> FileOperation.Move(
                    sourceFilePath = item.path,
                    destinationFilePath = destinationFilesPaths[index],
                    conflictResolution = conflictsResolutions[index]
                )
                false -> FileOperation.Copy(
                    sourceFilePath = item.path,
                    destinationFilePath = destinationFilesPaths[index],
                    conflictResolution = conflictsResolutions[index]
                )
            }
        }
        performFileOperations(fileOperations, mediaItemsRepo, viewModelScope)
    }

    private suspend fun onRename(itemsToRename: List<MediaItemUI>) {
        // await for after-rename paths from dialogs
        val newPaths = itemsToRename.map { item ->
            try {
                awaitForRenamerDialogResult(item)
            } catch (e: CancellationException) {
                return
            }
        }
        // check for filename conflicts and await for resolutions
        val conflictsResolutions = newPaths.mapIndexed { index, newPath ->
            val alreadyExists = mediaItemsRepo.checkExistence(newPath)
            when (alreadyExists) {
                true -> try {
                    awaitForConflictResolverDialogResult(itemsToRename[index])
                } catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
        }
        val fileOperations = itemsToRename.mapIndexed { index, item ->
            FileOperation.Rename(
                originalFilePath = item.path,
                newFilePath = newPaths[index],
                conflictResolution = conflictsResolutions[index]
            )
        }
        performFileOperations(fileOperations, mediaItemsRepo, viewModelScope)
    }

    private suspend fun onDelete(itemsToDelete: List<MediaItemUI>) {
        try {
            awaitForDeletionConfirmationDialogResult(itemsToDelete)
            val fileOperations = itemsToDelete.map { FileOperation.Delete(it.path) }
            performFileOperations(fileOperations, mediaItemsRepo, viewModelScope)
        } catch (e: CancellationException) {
            return
        }
    }


    // update album content based on raw data and preferences flows
    private suspend fun keepItemsFlowUpdated(folderPath: String?) {
        combine(_dataResourceFlow, _appPreferencesFlow) { dataRes, prefs ->
            val allFilesData = when (dataRes) {
                is Resource.Success -> dataRes.data
                is Resource.Loading -> dataRes.data
                is Resource.Error -> emptyList()
            }
            val allFilesUi = allFilesData.mapDataFilesToUiFiles()
            val albumRelatedItems = allFilesUi.createAlbumRelatedItemsList(folderPath, prefs)
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
                _uiState.update {
                    it.copy(
                        items = newItems,
                        // remove selection from items that not exists anymore
                        selectedItems = it.selectedItems.filter { selectedItem -> newItems.contains(selectedItem) }
                    )
                }
            }
        }
    }

    private suspend fun reflectLoadingChanges() {
        _isLoadingFlow.collectLatest { isLoading ->
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = isLoading) }
            }
        }
    }

    private suspend fun reflectPreferencesChanges() {
        _appPreferencesFlow.collectLatest { prefs ->
            _uiState.update { it.copy(appPreferences = prefs) }
        }
    }

    /**
     * Reflect search results based on query and items
     *
     */
    private suspend fun reflectSearchQueryChanges() {
        combine(_searchQueryFlow, _itemsFlow) { query, items ->
            if (query != null) items
                .filter { item -> item.path.contains(query, ignoreCase = true) }
                .let { foundItems -> UIState.Content.Searching(query, foundItems) }
            else UIState.Content.Exploring
        }.collectLatest { newContent ->
            _uiState.update {
                it.copy(content = newContent)
            }
        }
    }

    private suspend fun reflectSelectedItemsChanges() {
        _selectedItemsFlow.collectLatest { selectedItems ->
            _uiState.update {
                it.copy(selectedItems = selectedItems)
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


    private suspend fun performFileOperations(
        operations: List<FileOperation>,
        mediaItemsRepo: MediaItemsRepo,
        scope: CoroutineScope
    ) {
        var completeOperationsCount = 0
        mediaItemsRepo
            .executeFileOperations(operations)
            .observeForever { workInfos ->
                workInfos.count { it.state.isFinished }.let {
                    if (it > completeOperationsCount) {
                        completeOperationsCount = it
                        scope.launch { mediaItemsRepo.rescan() }
                    }
                }
                /* TODO: post progress notification here */
            }
    }


    // todo move to utils object?
    private fun List<MediaItemUI.File>.createAlbumRelatedItemsList(
        folderPath: String?,
        prefs: GalleryPreferences
    ): List<MediaItemUI> {
        // filter only items related to target album
        val uiItems = when (prefs.albumsMode) {
            GalleryPreferences.AlbumsMode.PLAIN -> when (folderPath.isNullOrEmpty()) {
                true -> this.createFlatAlbumsList()
                false -> this.createAlbumOwnFilesList(folderPath)
            }
            GalleryPreferences.AlbumsMode.NESTED -> this.createNestedAlbumsList(folderPath)
        }
        // apply filtering based on user preferences
        val filteredItems = uiItems.applyFilters(prefs.enabledFilters)
        // apply sorting based on user preferences
        val sortedItems = filteredItems.applySorting(prefs.sortingOrder, prefs.descendSorting)
        return sortedItems
    }
}
