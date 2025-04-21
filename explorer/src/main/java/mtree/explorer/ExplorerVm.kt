package mtree.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mtree.core.data.MediaItemData
import mtree.core.data.MediaItemsRepo
import mtree.core.data.Resource
import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.Sort
import mtree.core.domain.usecases.CopyOrMoveItemsUc
import mtree.core.domain.usecases.CreateItemsListToDisplayUc
import mtree.core.domain.usecases.DeleteItemsUc
import mtree.core.domain.usecases.RenameItemsUc
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesRepo
import mtree.core.ui.models.ConflictResolutionUi
import mtree.core.ui.models.MediaItemUI
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ExplorerVm
@Inject constructor(
    private val galleryAlbumsRepo: MediaItemsRepo,
    private val preferencesRepo: MtreePreferencesRepo,
    private val createItemsListToDisplayUc: CreateItemsListToDisplayUc,
    private val copyOrMoveItemsUc: CopyOrMoveItemsUc,
    private val renameItemsUc: RenameItemsUc,
    private val deleteItemsUc: DeleteItemsUc
) : ViewModel() {
    
    private val _albumPath = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow<String?>(null)
    
    private val _galleryAlbums = galleryAlbumsRepo
        .getMediaAlbumsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Resource.Loading(emptyList())
        )
    private val _preferences = preferencesRepo
        .getPreferencesFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            MtreePreferences.default()
        )
    
    private val _selectedItems = MutableStateFlow(emptyList<MediaItemUI>())
    private val _dialog = MutableStateFlow<Dialog>(Dialog.None)
    
    val uiState = combine(
        _galleryAlbums,
        _preferences,
        _albumPath,
        _searchQuery,
        _dialog,
        _selectedItems
    ) {
        val albumsResource = it[0] as Resource<List<MediaItemData.Album>>
        val prefs = it[1] as MtreePreferences
        val albumPath = it[2] as String?
        val searchQuery = it[3] as String?
        val dialog = it[4] as Dialog
        val selectedItems = it[5] as List<MediaItemUI>
        
        val itemsToDisplay = when (albumsResource) {
            is Resource.Success -> albumsResource.data
            is Resource.Loading -> albumsResource.data
            is Resource.Error -> emptyList()
        }.map { albumData ->
            albumData.mapToDomain() as MediaItemDomain.Album
        }.let { albumsDomain ->
            createItemsListToDisplayUc.execute(
                galleryAlbums = albumsDomain,
                basePath = albumPath,
                searchQuery = searchQuery,
                galleryMode = when (prefs.galleryMode) {
                    MtreePreferences.GalleryMode.TREE -> GalleryMode.TREE
                    MtreePreferences.GalleryMode.PLAIN -> GalleryMode.PLAIN
                },
                filters = Filters(
                    includeAlbums = prefs.showAlbums,
                    includeFiles = prefs.showFiles,
                    includeImages = prefs.showImages,
                    includeVideos = prefs.showVideos,
                    includeGifs = prefs.showGifs,
                    includeUnhidden = prefs.showUnHidden,
                    includeHidden = prefs.showHidden
                ),
                sort = Sort(
                    order = when (prefs.sortOrder) {
                        MtreePreferences.SortOrder.CREATION_DATE -> Sort.Order.CREATION_DATE
                        MtreePreferences.SortOrder.MODIFICATION_DATE -> Sort.Order.MODIFICATION_DATE
                        MtreePreferences.SortOrder.NAME -> Sort.Order.NAME
                        MtreePreferences.SortOrder.EXTENSION -> Sort.Order.EXTENSION
                        MtreePreferences.SortOrder.SIZE -> Sort.Order.SIZE
                        MtreePreferences.SortOrder.RANDOM -> Sort.Order.RANDOM
                    },
                    descend = prefs.descendSortOrder,
                    placeFirst = when (prefs.placeOnTop) {
                        MtreePreferences.PlaceOnTop.NONE -> Sort.PlaceFirst.NONE
                        MtreePreferences.PlaceOnTop.ALBUMS_ON_TOP -> Sort.PlaceFirst.ALBUMS
                        MtreePreferences.PlaceOnTop.FILES_ON_TOP -> Sort.PlaceFirst.FILES
                    }
                )
            )
        }.map { itemDomain ->
            itemDomain.mapToUi()
        }
        val isLoading = albumsResource is Resource.Loading
        val content =
            if (isLoading)
                Content.Shimmer
            else if (itemsToDisplay.isEmpty())
                Content.NothingToDisplay
            else
                Content.Main
        
        UiState(
            albumPath = albumPath,
            items = itemsToDisplay,
            selectedItems = selectedItems,
            searchQuery = searchQuery,
            isLoading = isLoading,
            portraitGridColumns = prefs.portraitGridColumns,
            landscapeGridColumns = prefs.landscapeGridColumns,
            content = content,
            dialog = dialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(
            albumPath = null,
            items = emptyList(),
            selectedItems = emptyList(),
            searchQuery = null,
            isLoading = false,
            portraitGridColumns = 3,
            landscapeGridColumns = 4,
            content = Content.Shimmer,
            dialog = Dialog.None
        )
    )
    
    
    internal fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Launch -> onLaunch(action.albumPath, action.searchQuery)
                Action.Refresh -> onRefresh()
                Action.ResetFiltersAndSearch -> onFiltersReset()
                is Action.SearchQueryChange -> onSearchQueryChange(action.newQuery)
                is Action.ItemsSelectionChange -> onSelectionChange(action.newSelection)
                is Action.ItemsCopy -> onCopyOrMove(action.itemsToCopy)
                is Action.ItemsMove -> onCopyOrMove(action.itemsToMove, move = true)
                is Action.ItemsRename -> onRename(action.itemsToRename)
                is Action.ItemsDelete -> onDelete(action.itemsToDelete)
            }
        }
    }
    
    
    private fun onLaunch(albumPath: String?, searchQuery: String?) {
        _albumPath.update { albumPath }
        _searchQuery.update { searchQuery }
    }
    
    private suspend fun onRefresh() {
        galleryAlbumsRepo.rescan()
    }
    
    private suspend fun onFiltersReset() {
        _preferences.value.copy(
            showImages = true,
            showVideos = true,
            showGifs = true,
            showFiles = true,
            showAlbums = true,
            showUnHidden = true,
            showHidden = false
        ).let {
            preferencesRepo.savePreferences(it)
        }
    }
    
    private fun onSearchQueryChange(newQuery: String?) {
        _searchQuery.update { newQuery }
    }
    
    private fun onSelectionChange(newSelection: List<MediaItemUI>) {
        _selectedItems.update { newSelection }
    }
    
    private suspend fun onCopyOrMove(items: List<MediaItemUI>, move: Boolean = false) {
        try {
            copyOrMoveItemsUc.execute(
                items = items.map { it.mapToDomain() },
                move = move,
                onDestinationDirectoryRequired = {
                    awaitForDestinationPath()
                },
                onConflictResolutionRequired = { item ->
                    awaitForConflictResolution(
                        item.mapToUi()
                    ).mapToNewDomain()
                },
                onFilesystemOperationsStarted = {
                    //TODO("show snack")
                },
                onFilesystemOperationsFinished = {
                    //TODO("show snack")
                }
            )
        }
        catch (e: Exception) {
            setDialog(Dialog.None)
        }
    }
    
    private suspend fun onRename(items: List<MediaItemUI>) {
        try {
            renameItemsUc.execute(
                items = items.map { it.mapToDomain() },
                onNewNameRequired = { item ->
                    awaitForNewName(item.mapToUi())
                },
                onConflictResolutionRequired = { item ->
                    awaitForConflictResolution(
                        item.mapToUi()
                    ).mapToNewDomain()
                },
                onFilesystemOperationsStarted = {
                    //TODO("show snack")
                },
                onFilesystemOperationsFinished = {
                    //TODO("show snack")
                }
            )
        }
        catch (e: Exception) {
            setDialog(Dialog.None)
        }
    }
    
    private suspend fun onDelete(items: List<MediaItemUI>) {
        try {
            deleteItemsUc.execute(
                items = items.map { it.mapToDomain() },
                onConfirmationRequired = { awaitForConfirmation(items) },
                onFilesystemOperationsStarted = {
                    TODO("show snack")
                },
                onFilesystemOperationsFinished = {
                    TODO("show snack")
                }
            )
        }
        catch (e: Exception) {
            setDialog(Dialog.None)
        }
    }
    
    
    private suspend fun awaitForDestinationPath(): String = suspendCoroutine {
        setDialog(
            Dialog.AlbumPicker(
                onConfirm = { pickedPath ->
                    setDialog(Dialog.None)
                    it.resume(pickedPath)
                },
                onDismiss = {
                    setDialog(Dialog.None)
                    it.resumeWithException(CancellationException())
                }
            )
        )
    }
    
    private suspend fun awaitForNewName(item: MediaItemUI): String = suspendCoroutine {
        setDialog(
            Dialog.Renaming(
                item = item,
                onConfirm = { newName ->
                    setDialog(Dialog.None)
                    it.resume(newName)
                },
                onDismiss = {
                    setDialog(Dialog.None)
                    it.resumeWithException(CancellationException())
                }
            )
        )
    }
    
    private suspend fun awaitForConflictResolution(item: MediaItemUI): ConflictResolutionUi = suspendCoroutine {
        setDialog(
            Dialog.ConflictResolver(
                conflictItem = item,
                onConfirm = { resolution ->
                    setDialog(Dialog.None)
                    it.resume(resolution)
                },
                onDismiss = {
                    setDialog(Dialog.None)
                    it.resumeWithException(CancellationException())
                }
            )
        )
    }
    
    private suspend fun awaitForConfirmation(itemsToDelete: List<MediaItemUI>): Boolean {
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
    
    private fun setDialog(newDialog: Dialog) {
        _dialog.update { newDialog }
    }
}
