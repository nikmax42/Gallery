package nikmax.gallery.gallery.explorer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
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
import nikmax.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.core.utils.PermissionsUtils
import nikmax.gallery.gallery.core.data.media.ConflictResolution
import nikmax.gallery.gallery.core.data.media.FileOperation
import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.applyFilters
import nikmax.gallery.gallery.core.utils.ItemsUtils.applySorting
import nikmax.gallery.gallery.core.utils.ItemsUtils.mapToUi
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
    private val galleryRepo: MediaItemsRepo
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
        
        data class OperationStarted(val operations: List<FileOperation>) : SnackBar
        data class OperationFinished(val completeItems: Int, val failedItems: Int) : SnackBar
    }
    
    
    // Raw data flows
    private val _galleryPreferencesFlow = GalleryPreferencesUtils.getPreferencesFlow(context)
    private var _dataResourceFlow: MutableStateFlow<Resource<List<MediaItemData>>> =
        MutableStateFlow(Resource.Loading(emptyList()))
    
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
        
        viewModelScope.launch { keepDataFlowUpdated() }
        viewModelScope.launch { keepItemsFlowUpdated() }
        viewModelScope.launch { keepLoadingFlowUpdated() }
        viewModelScope.launch { keepContentTypeFlowUpdated() }
        viewModelScope.launch { keepPermissionStatusFlowUpdated() }
        
        viewModelScope.launch { reflectContentTypeFlowChanges() }
        viewModelScope.launch { reflectNavigationStackFlowChanges() }
        viewModelScope.launch { reflectItemsFlowChanges() }
        viewModelScope.launch { reflectLoadingChanges() }
        viewModelScope.launch { reflectSelectedItemsFlowChanges() }
        viewModelScope.launch { reflectSearchQueryFlowChanges() }
    }
    
    private suspend fun onRefresh() {
        galleryRepo.rescan()
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
    
    private suspend fun onCopyOrMove(itemsToCopyOrMove: List<MediaItemUI>, move: Boolean = false) {
        // pick destination album
        val destinationAlbumPath = try {
            awaitForAlbumPickerResult()
        }
        catch (_: CancellationException) {
            return
        }
        val destinationFilesPaths = itemsToCopyOrMove.map { item ->
            "$destinationAlbumPath/${item.name}"
        }
        // check for conflict filenames in the picked album
        // await for conflict resolutions
        val conflictsResolutions = destinationFilesPaths.mapIndexed { index, destinationPath ->
            val alreadyExists = galleryRepo.checkExistence(destinationPath)
            when (alreadyExists) {
                true -> try {
                    awaitForConflictResolverDialogResult(itemsToCopyOrMove[index])
                }
                catch (_: CancellationException) {
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
        performFileOperations(fileOperations, galleryRepo, viewModelScope)
    }
    
    private suspend fun onRename(itemsToRename: List<MediaItemUI>) {
        // await for after-rename paths from dialogs
        val newPaths = itemsToRename.map { item ->
            try {
                awaitForRenamerDialogResult(item)
            }
            catch (_: CancellationException) {
                return
            }
        }
        // check for filename conflicts and await for resolutions
        val conflictsResolutions = newPaths.mapIndexed { index, newPath ->
            val alreadyExists = galleryRepo.checkExistence(newPath)
            when (alreadyExists) {
                true -> try {
                    awaitForConflictResolverDialogResult(itemsToRename[index])
                }
                catch (_: CancellationException) {
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
        performFileOperations(fileOperations, galleryRepo, viewModelScope)
    }
    
    private suspend fun onDelete(itemsToDelete: List<MediaItemUI>) {
        try {
            awaitForDeletionConfirmationDialogResult(itemsToDelete)
            val fileOperations = itemsToDelete.map { FileOperation.Delete(it.path) }
            performFileOperations(fileOperations, galleryRepo, viewModelScope)
        }
        catch (_: CancellationException) {
            return
        }
    }
    
    
    private suspend fun keepDataFlowUpdated() {
        combine(
            _galleryPreferencesFlow,
            _navStackFlow,
            _searchQueryFlow
        ) { prefs, navStack, searchQuery ->
            when (searchQuery.isNullOrBlank()) {
                //if there is not search query - use album content data
                true -> galleryRepo.getAlbumContentFlow(
                    path = navStack.lastOrNull(),
                    searchQuery = searchQuery,
                    treeMode = prefs.appearance.nestedAlbumsEnabled
                )
                //on search use search result data
                false -> galleryRepo.getSearchResultFlow(
                    query = searchQuery,
                    basePath = navStack.lastOrNull()
                )
            }
            
        }.collectLatest { newDataFlow ->
            newDataFlow.collectLatest { newData ->
                _dataResourceFlow.update { newData }
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
    
    private suspend fun keepLoadingFlowUpdated() {
        _dataResourceFlow.collectLatest { data ->
            _isLoadingFlow.update {
                data is Resource.Loading
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
            if (permissionStatus == PermissionsUtils.PermissionStatus.DENIED)
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
            else if (isLoading && navStack.isEmpty() && items.isEmpty())
                UIState.Content.Initialization
            else
                UIState.Content.Normal
        }.collectLatest { newContentType ->
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(content = newContentType)
                }
            }
        }
    }
    
    private fun keepPermissionStatusFlowUpdated() {
        timer(period = 3000) {
            val storageStatus = PermissionsUtils.checkPermission(
                PermissionsUtils.AppPermissions.MANAGE_EXTERNAL_STORAGE,
                context
            )
            _permissionStatusFlow.update { storageStatus }
        }
    }
    
    
    
    private suspend fun reflectNavigationStackFlowChanges() {
        _navStackFlow.collectLatest { navEntries ->
            _uiState.update { it.copy(albumPath = navEntries.lastOrNull()) }
        }
    }
    
    private suspend fun reflectItemsFlowChanges() {
        _itemsFlow.collectLatest { newItems ->
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        items = newItems,
                        // remove selection from items that not exists anymore
                        selectedItems = it.selectedItems.filter { selectedItem ->
                            newItems.contains(
                                selectedItem
                            )
                        }
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
    
    private suspend fun reflectSearchQueryFlowChanges() {
        _searchQueryFlow.collectLatest { newQuery ->
            _uiState.update {
                it.copy(searchQuery = newQuery)
            }
        }
    }
    
    private suspend fun reflectSelectedItemsFlowChanges() {
        _selectedItemsFlow.collectLatest { selectedItems ->
            _uiState.update {
                it.copy(selectedItems = selectedItems)
            }
        }
    }
    
    private suspend fun reflectContentTypeFlowChanges() {
        _contentFlow.collectLatest { contentType ->
            _uiState.update {
                it.copy(content = contentType)
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
        // show snackbar with operations type
        _event.emit(Event.ShowSnackbar(SnackBar.OperationStarted(operations)))
        
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
                // when all operations finished: show snackbar with result
                if (completeOperationsCount == operations.size) {
                    val successfulOperationsCount = workInfos.count { it.state == WorkInfo.State.SUCCEEDED }
                    val failedOperationsCount = workInfos.count { it.state == WorkInfo.State.FAILED }
                    viewModelScope.launch {
                        _event.emit(
                            Event.ShowSnackbar(
                                SnackBar.OperationFinished(
                                    completeItems = successfulOperationsCount,
                                    failedItems = failedOperationsCount
                                )
                            )
                        )
                    }
                }
            }
    }
}
