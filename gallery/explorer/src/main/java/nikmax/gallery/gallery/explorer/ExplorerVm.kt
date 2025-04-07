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
import nikmax.gallery.core.utils.PermissionsUtils
import nikmax.gallery.gallery.core.data.media.ConflictResolution
import nikmax.gallery.gallery.core.data.media.FileOperation
import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.data.preferences.GalleryPreferencesRepo
import nikmax.gallery.gallery.core.mappers.MediaItemMapper.mapToUi
import nikmax.gallery.gallery.core.ui.MediaItemUI
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
    private val prefsRepo: GalleryPreferencesRepo,
    private val galleryRepo: MediaItemsRepo
) : ViewModel() {
    
    // Raw data flows
    private val _navStackFlow = MutableStateFlow(emptyList<String>())
    private val _galleryPreferencesFlow = MutableStateFlow(GalleryPreferences())
    private var _dataResourceFlow: MutableStateFlow<Resource<List<MediaItemData>>> = MutableStateFlow(
        Resource.Loading(emptyList())
    )
    
    // UI-related data flows
    private val _itemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _isLoadingFlow = MutableStateFlow(true)
    private val _searchQueryFlow = MutableStateFlow<String?>(null)
    private val _selectedItemsFlow = MutableStateFlow(emptyList<MediaItemUI>())
    private val _contentFlow = MutableStateFlow(UiState.Content.Initialization)
    private val _permissionStatusFlow = MutableStateFlow(PermissionsUtils.PermissionStatus.GRANTED)
    
    private val _uiState = MutableStateFlow(UiState())
    internal val uiState = _uiState.asStateFlow()
    
    private val _event = MutableSharedFlow<Event>()
    internal val event = _event.asSharedFlow()
    
    
    internal fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Launch -> onLaunch()
                Action.Refresh -> onRefresh()
                is Action.ItemOpen -> onItemOpen(action.item)
                Action.NavigateOutOfAlbum -> onNavigateBack()
                is Action.SearchQueryChange -> onSearchQueryChange(action.newQuery)
                is Action.ItemsSelectionChange -> onSelectionChange(action.newSelection)
                is Action.ItemsCopy -> onCopyOrMove(action.itemsToCopy)
                is Action.ItemsMove -> onCopyOrMove(action.itemsToMove, move = true)
                is Action.ItemsRename -> onRename(action.itemsToRename)
                is Action.ItemsDelete -> onDelete(action.itemsToDelete)
            }
        }
    }
    
    
    private fun onLaunch() {
        //initiate automatic rescan only when there is no items to display
        viewModelScope.launch {
            if (_itemsFlow.value.isEmpty()) onRefresh()
        }
        
        viewModelScope.launch { keepPreferences() }
        viewModelScope.launch { keepDataFlow() }
        viewModelScope.launch { keepItemsFlow() }
        viewModelScope.launch { keepLoadingFlow() }
        viewModelScope.launch { keepContentTypeFlow() }
        viewModelScope.launch { keepPermissionStatusFlow() }
        
        viewModelScope.launch { reflectContentTypeFlow() }
        viewModelScope.launch { reflectNavigationStackFlow() }
        viewModelScope.launch { reflectItemsFlow() }
        viewModelScope.launch { reflectPreferencesFlow() }
        viewModelScope.launch { reflectLoadingFlow() }
        viewModelScope.launch { reflectSelectedItemsFlow() }
        viewModelScope.launch { reflectSearchQueryFlow() }
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
    
    
    private suspend fun keepPreferences() {
        prefsRepo
            .getPreferencesFlow()
            .collectLatest { prefs ->
                _galleryPreferencesFlow.update { prefs }
            }
    }
    
    private suspend fun keepDataFlow() {
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
                    treeMode = prefs.galleryMode == GalleryPreferences.GalleryMode.TREE,
                    includeImages = prefs.showImages,
                    includeVideos = prefs.showVideos,
                    includeGifs = prefs.showGifs,
                    includeUnhidden = prefs.showUnHidden,
                    includeHidden = prefs.showHidden,
                    includeFiles = prefs.showFiles,
                    includeAlbums = prefs.showAlbums,
                    sortingOrder = when (prefs.sortOrder) {
                        GalleryPreferences.SortOrder.CREATION_DATE -> MediaItemsRepo.SortOrder.CREATION_DATE
                        GalleryPreferences.SortOrder.MODIFICATION_DATE -> MediaItemsRepo.SortOrder.MODIFICATION_DATE
                        GalleryPreferences.SortOrder.NAME -> MediaItemsRepo.SortOrder.NAME
                        GalleryPreferences.SortOrder.EXTENSION -> MediaItemsRepo.SortOrder.EXTENSION
                        GalleryPreferences.SortOrder.SIZE -> MediaItemsRepo.SortOrder.SIZE
                        GalleryPreferences.SortOrder.RANDOM -> MediaItemsRepo.SortOrder.RANDOM
                    },
                    descendSorting = prefs.descendSortOrder,
                    albumsFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP,
                    filesFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.FILES_ON_TOP
                )
                //on search use search result data
                false -> galleryRepo.getSearchResultFlow(
                    query = searchQuery,
                    basePath = navStack.lastOrNull(),
                    includeImages = prefs.showImages,
                    includeVideos = prefs.showVideos,
                    includeGifs = prefs.showGifs,
                    includeUnhidden = prefs.showUnHidden,
                    includeHidden = prefs.showHidden,
                    includeFiles = prefs.showFiles,
                    includeAlbums = prefs.showAlbums,
                    sortingOrder = when (prefs.sortOrder) {
                        GalleryPreferences.SortOrder.CREATION_DATE -> MediaItemsRepo.SortOrder.CREATION_DATE
                        GalleryPreferences.SortOrder.MODIFICATION_DATE -> MediaItemsRepo.SortOrder.MODIFICATION_DATE
                        GalleryPreferences.SortOrder.NAME -> MediaItemsRepo.SortOrder.NAME
                        GalleryPreferences.SortOrder.EXTENSION -> MediaItemsRepo.SortOrder.EXTENSION
                        GalleryPreferences.SortOrder.SIZE -> MediaItemsRepo.SortOrder.SIZE
                        GalleryPreferences.SortOrder.RANDOM -> MediaItemsRepo.SortOrder.RANDOM
                    },
                    descendSorting = prefs.descendSortOrder,
                    albumsFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.ALBUMS_ON_TOP,
                    filesFirst = prefs.placeOnTop == GalleryPreferences.PlaceOnTop.FILES_ON_TOP
                )
            }
            
        }.collectLatest { newDataFlow ->
            newDataFlow.collectLatest { newData ->
                _dataResourceFlow.update { newData }
            }
        }
    }
    
    private suspend fun keepItemsFlow() {
        combine(_dataResourceFlow, _galleryPreferencesFlow) { data, prefs ->
            when (data) {
                is Resource.Success -> data.data
                is Resource.Loading -> data.data
                is Resource.Error -> emptyList()
            }.mapToUi()
        }.collectLatest { newItemsList ->
            _itemsFlow.update { newItemsList }
        }
    }
    
    private suspend fun keepLoadingFlow() {
        _dataResourceFlow.collectLatest { data ->
            _isLoadingFlow.update {
                data is Resource.Loading
            }
        }
    }
    
    private suspend fun keepContentTypeFlow() {
        combine(
            _isLoadingFlow,
            _itemsFlow,
            _navStackFlow,
            _permissionStatusFlow
        ) { isLoading, items, navStack, permissionStatus ->
            if (permissionStatus == PermissionsUtils.PermissionStatus.DENIED)
                UiState.Content.Error.PermissionNotGranted(
                    onGrantClick = {
                        PermissionsUtils.requestPermission(
                            PermissionsUtils.AppPermissions.MANAGE_EXTERNAL_STORAGE,
                            context
                        )
                    }
                )
            else if (!isLoading && items.isEmpty())
                UiState.Content.Error.NothingFound
            else if (isLoading && navStack.isEmpty() && items.isEmpty())
                UiState.Content.Initialization
            else
                UiState.Content.Normal
        }.collectLatest { newContentType ->
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(content = newContentType)
                }
            }
        }
    }
    
    private fun keepPermissionStatusFlow() {
        timer(period = 3000) {
            val storageStatus = PermissionsUtils.checkPermission(
                PermissionsUtils.AppPermissions.MANAGE_EXTERNAL_STORAGE,
                context
            )
            _permissionStatusFlow.update { storageStatus }
        }
    }
    
    
    
    private suspend fun reflectNavigationStackFlow() {
        _navStackFlow.collectLatest { navEntries ->
            _uiState.update { it.copy(albumPath = navEntries.lastOrNull()) }
        }
    }
    
    private suspend fun reflectItemsFlow() {
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
    
    private suspend fun reflectPreferencesFlow() {
        _galleryPreferencesFlow.collectLatest { prefs ->
            _uiState.update {
                it.copy(
                    portraitGridColumns = prefs.portraitGridColumns,
                    landscapeGridColumns = prefs.landscapeGridColumns
                )
            }
        }
    }
    
    private suspend fun reflectLoadingFlow() {
        _isLoadingFlow.collectLatest { isLoading ->
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = isLoading) }
            }
        }
    }
    
    private suspend fun reflectSearchQueryFlow() {
        _searchQueryFlow.collectLatest { newQuery ->
            _uiState.update {
                it.copy(searchQuery = newQuery)
            }
        }
    }
    
    private suspend fun reflectSelectedItemsFlow() {
        _selectedItemsFlow.collectLatest { selectedItems ->
            _uiState.update {
                it.copy(selectedItems = selectedItems)
            }
        }
    }
    
    private suspend fun reflectContentTypeFlow() {
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
