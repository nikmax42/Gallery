package nikmax.gallery.gallery.explorer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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
import nikmax.gallery.core.utils.PermissionsUtils
import nikmax.gallery.gallery.core.data.media.ConflictResolution
import nikmax.gallery.gallery.core.data.media.FileOperation
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.createItemsListToDisplay
import nikmax.material_tree.gallery.dialogs.Dialog
import javax.inject.Inject
import kotlin.concurrent.timer
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ExplorerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaItemsRepo: MediaItemsRepo
) : ViewModel() {

    data class UIState(
        val albumPath: String? = null,
        val items: List<MediaItemUI> = emptyList(),
        val selectedItems: List<MediaItemUI> = emptyList(),
        val searchQuery: String? = null,
        val isLoading: Boolean = true,
        val content: Content = Content.Initialization,
        val dialog: Dialog = Dialog.None,
    ) {
        sealed interface Content {
            data object Initialization : Content
            data object Normal : Content
            sealed interface Error : Content {
                data class PermissionNotGranted(val onGrantClick: () -> Unit) : Error
                data object NothingFound : Error
            }
        }
    }


    sealed interface UserAction {
        data class ScreenLaunch(val folderPath: String?) : UserAction
        data object Refresh : UserAction
        data class ItemOpen(val item: MediaItemUI) : UserAction
        data object NavigateOutOfAlbum : UserAction
        data class SearchQueryChange(val newQuery: String?) : UserAction
        data class ItemsSelectionChange(val newSelection: List<MediaItemUI>) : UserAction
        data class ItemsCopy(val itemsToCopy: List<MediaItemUI>) : UserAction
        data class ItemsMove(val itemsToMove: List<MediaItemUI>) : UserAction
        data class ItemsRename(val itemsToRename: List<MediaItemUI>) : UserAction
        data class ItemsDelete(val itemsToDelete: List<MediaItemUI>) : UserAction
    }


    sealed interface Event {
        data class OpenViewer(val file: MediaItemUI.File) : Event
        data class ShowSnackbar(val snackbar: SnackBar) : Event
    }


    sealed interface SnackBar {
        data class ProtectedItems(
            val protectedItems: List<MediaItemUI>,
            val onConfirm: () -> Unit
        ) : SnackBar
    }


    // Raw data flows
    private val _galleryPreferencesFlow = GalleryPreferencesUtils.getPreferencesFlow(context)
    private val _dataResourceFlow = mediaItemsRepo.getFilesResourceFlow()

    // UI-related data flows
    private val _navStackFlow = MutableStateFlow(emptyList<String>())
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(true)
    private val _searchQueryFlow = MutableStateFlow<String?>(null)
    private val _selectedItemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _contentFlow = MutableStateFlow(UIState.Content.Initialization)
    private val _permissionStatusFlow = MutableStateFlow(PermissionsUtils.PermissionStatus.GRANTED)

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()


    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.ScreenLaunch -> onLaunch()
                UserAction.Refresh -> onRefresh()
                is UserAction.ItemOpen -> onItemOpen(action.item)
                UserAction.NavigateOutOfAlbum -> onNavigateBack()
                is UserAction.SearchQueryChange -> onSearchQueryChange(action.newQuery)
                // is UserAction.PreferencesChange -> onPreferencesChange(action.newPreferences)
                is UserAction.ItemsSelectionChange -> onSelectionChange(action.newSelection)
                is UserAction.ItemsCopy -> onCopyOrMove(action.itemsToCopy)
                is UserAction.ItemsMove -> onCopyOrMove(action.itemsToMove, move = true)
                is UserAction.ItemsRename -> onRename(action.itemsToRename)
                is UserAction.ItemsDelete -> onDelete(action.itemsToDelete)
            }
        }
    }


    private fun onLaunch() {
        viewModelScope.launch { onRefresh() }

        viewModelScope.launch { keepItemsFlowUpdated() }
        viewModelScope.launch { keepLoadingFlowUpdated() }
        viewModelScope.launch { keepContentTypeFlowUpdated() }
        viewModelScope.launch { keepPermissionStatusFlowUpdated() }

        viewModelScope.launch { reflectContentTypeChanges() }
        viewModelScope.launch { reflectNavigationStackChanges() }
        viewModelScope.launch { reflectItemsChanges() }
        viewModelScope.launch { reflectLoadingChanges() }
        viewModelScope.launch { reflectSelectedItemsChanges() }
        viewModelScope.launch { reflectSearchQueryChanges() }
    }

    private suspend fun onRefresh() {
        mediaItemsRepo.rescan()
    }

    private suspend fun onItemOpen(item: MediaItemUI) {
        when (item) {
            is MediaItemUI.Album -> _navStackFlow.update { it.plus(item.path) }
            is MediaItemUI.File -> _event.emit(Event.OpenViewer(item))
        }
    }

    private fun onNavigateBack() {
        _navStackFlow.update { it.dropLast(1) }
    }

    private fun onSearchQueryChange(newQuery: String?) {
        _searchQueryFlow.update { newQuery }
    }

    private fun onSelectionChange(newSelection: List<MediaItemUI>) {
        _selectedItemsFlow.update { newSelection }
    }

    private suspend fun onPreferencesChange(newPreferences: GalleryPreferences) {
        GalleryPreferencesUtils.savePreferences(newPreferences, context)
    }

    private suspend fun onCopyOrMove(itemsToCopyOrMove: List<MediaItemUI>, move: Boolean = false) {
        // if selection contains protected items - prevent execution
        // and show snackbar with proposition to unselect protected items
        val protectedItems = itemsToCopyOrMove.filter { it.protected }
        if (protectedItems.isNotEmpty()) {
            showSnackbar(
                SnackBar.ProtectedItems(
                    protectedItems = protectedItems,
                    onConfirm = { onSelectionChange(itemsToCopyOrMove - protectedItems) }
                ),
            )
            return
        }
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
        val protectedItems = itemsToRename.filter { it.protected }
        if (protectedItems.isNotEmpty()) {
            showSnackbar(
                SnackBar.ProtectedItems(
                    protectedItems = protectedItems,
                    onConfirm = { onSelectionChange(itemsToRename - protectedItems) }
                ),
            )
            return
        }
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
        val protectedItems = itemsToDelete.filter { it.protected }
        if (protectedItems.isNotEmpty()) {
            showSnackbar(
                SnackBar.ProtectedItems(
                    protectedItems = protectedItems,
                    onConfirm = { onSelectionChange(itemsToDelete - protectedItems) }
                ),
            )
            return
        }
        try {
            awaitForDeletionConfirmationDialogResult(itemsToDelete)
            val fileOperations = itemsToDelete.map { FileOperation.Delete(it.path) }
            performFileOperations(fileOperations, mediaItemsRepo, viewModelScope)
        } catch (e: CancellationException) {
            return
        }
    }


    /**
     * Updated screen items list based on mediastore data, app preferences, current album path and search query
     *
     */
    private suspend fun keepItemsFlowUpdated() {
        combine(
            _dataResourceFlow,
            _galleryPreferencesFlow,
            _navStackFlow,
            _searchQueryFlow
        ) { dataRes, prefs, navStack, searchQuery ->
            val currentAlbumPath = navStack.lastOrNull()
            when (dataRes) {
                is Resource.Success -> dataRes.data
                is Resource.Loading -> dataRes.data
                is Resource.Error -> emptyList()
            }.createItemsListToDisplay(
                targetAlbumPath = currentAlbumPath,
                nestedAlbumsEnabled = prefs.appearance.nestedAlbumsEnabled,
                includeImages = prefs.filtering.includeImages,
                includeVideos = prefs.filtering.includeVideos,
                includeGifs = prefs.filtering.includeGifs,
                includeHidden = prefs.filtering.includeHidden,
                includeFilesOnly = prefs.filtering.includeFilesOnly,
                sortingOrder = prefs.sorting.order,
                descendSorting = prefs.sorting.descend,
                showAlbumsFirst = prefs.sorting.albumsFirst,
                searchQuery = searchQuery
            )
        }.collectLatest { actualItemsList ->
            _itemsFlow.update { actualItemsList.distinct() }
        }
    }

    private suspend fun keepLoadingFlowUpdated() {
        _dataResourceFlow.collectLatest { filesDataResource ->
            _isLoadingFlow.update {
                filesDataResource is Resource.Loading
            }
        }
    }

    private suspend fun keepContentTypeFlowUpdated() {
        combine(
            _isLoadingFlow,
            _itemsFlow,
            _navStackFlow,
            _permissionStatusFlow
        ) { isLoading, items, navStack, permissionStatus ->
            if (isLoading && navStack.isEmpty() && items.isEmpty())
                UIState.Content.Initialization
            else if (permissionStatus == PermissionsUtils.PermissionStatus.DENIED)
                UIState.Content.Error.PermissionNotGranted(
                    onGrantClick = {
                        PermissionsUtils.requestPermission(
                            PermissionsUtils.AppPermissions.MANAGE_EXTERNAL_STORAGE,
                            context
                        )
                    }
                )
            else if (!isLoading && items.isEmpty())
                UIState.Content.Error.NothingFound
            else
                UIState.Content.Normal
        }.collectLatest { newContentType ->
            _uiState.update {
                it.copy(content = newContentType)
            }
        }
    }

    private suspend fun keepPermissionStatusFlowUpdated() {
        timer(period = 3000) {
            val storageStatus = PermissionsUtils.checkPermission(
                PermissionsUtils.AppPermissions.MANAGE_EXTERNAL_STORAGE,
                context
            )
            _permissionStatusFlow.update { storageStatus }
        }
    }



    private suspend fun reflectNavigationStackChanges() {
        _navStackFlow.collectLatest { navEntries ->
            _uiState.update { it.copy(albumPath = navEntries.lastOrNull()) }
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

    private suspend fun reflectSearchQueryChanges() {
        _searchQueryFlow.collectLatest { newQuery ->
            _uiState.update {
                it.copy(searchQuery = newQuery)
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

    private suspend fun reflectContentTypeChanges() {
        _contentFlow.collectLatest { contentType ->
            _uiState.update {
                it.copy(content = contentType)
            }
        }
    }


    private suspend fun awaitForAlbumPickerResult(): String {
        return suspendCoroutine { cont ->
            setDialog(
                nikmax.material_tree.gallery.dialogs.Dialog.AlbumPicker(
                    onConfirm = { pickedAlbumPath ->
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                        cont.resume(pickedAlbumPath)
                    },
                    onDismiss = {
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
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
                    dialog = nikmax.material_tree.gallery.dialogs.Dialog.Renaming(
                        item = itemToRename,
                        onConfirm = { newPath ->
                            setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                            cont.resume(newPath)
                        },
                        onDismiss = {
                            setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
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
                nikmax.material_tree.gallery.dialogs.Dialog.DeletionConfirmation(
                    items = itemsToDelete,
                    onConfirm = {
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                        cont.resume(true)
                    },
                    onDismiss = {
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private suspend fun awaitForConflictResolverDialogResult(conflictItem: MediaItemUI): ConflictResolution {
        return suspendCoroutine { cont ->
            setDialog(
                nikmax.material_tree.gallery.dialogs.Dialog.ConflictResolver(
                    conflictItem = conflictItem,
                    onConfirm = { resolution ->
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                        cont.resume(resolution)
                    },
                    onDismiss = {
                        setDialog(nikmax.material_tree.gallery.dialogs.Dialog.None)
                        cont.resumeWithException(CancellationException())
                    }
                )
            )
        }
    }

    private fun setDialog(newDialog: Dialog) {
        _uiState.update { it.copy(dialog = newDialog) }
    }


    private suspend fun showSnackbar(snackbar: SnackBar) {
        _event.emit(Event.ShowSnackbar(snackbar))
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
}
