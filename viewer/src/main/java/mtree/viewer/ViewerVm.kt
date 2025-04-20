package mtree.viewer

import androidx.annotation.VisibleForTesting
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
import kotlin.io.path.Path
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    galleryAlbumsRepo: MediaItemsRepo,
    prefsRepo: MtreePreferencesRepo,
    private val createItemsListToDisplayUc: CreateItemsListToDisplayUc,
    private val copyOrMoveItemsUc: CopyOrMoveItemsUc,
    private val renameItemsUc: RenameItemsUc,
    private val deleteItemsUc: DeleteItemsUc
) : ViewModel() {
    
    @VisibleForTesting
    internal val _initialFilePath = MutableStateFlow<String?>(null)
    @VisibleForTesting
    internal val _searchQuery = MutableStateFlow<String?>(null)
    private val _controlsIsShownFlow = MutableStateFlow(true)
    private val _dialog = MutableStateFlow<Dialog>(Dialog.None)
    
    val uiState = combine(
        galleryAlbumsRepo.getMediaAlbumsFlow(),
        prefsRepo.getPreferencesFlow(),
        _initialFilePath,
        _controlsIsShownFlow,
        _dialog,
        _searchQuery
    ) {
        val albumsResource = it[0] as Resource<List<MediaItemData.Album>>
        val prefs = it[1] as MtreePreferences
        val initialFilePath = it[2] as String?
        val controlsIsShown = it[3] as Boolean
        val dialog = it[4] as Dialog
        val searchQuery = it[5] as String?
        
        val filesToShow = when (albumsResource) {
            is Resource.Success -> albumsResource.data
            is Resource.Loading -> albumsResource.data
            is Resource.Error -> emptyList()
        }.let { itemsData ->
            itemsData.map { it.mapToDomain() as MediaItemDomain.Album }
        }.let { albumsDomain ->
            val currentAlbumPath = Path(initialFilePath.toString()).parent.pathString
            createItemsListToDisplayUc.execute(
                galleryAlbums = albumsDomain,
                basePath = currentAlbumPath,
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
        }.map {
            it.mapToUi()
        }.filterIsInstance<MediaItemUI.File>()
        
        val content = when (filesToShow.isEmpty()) {
            true -> Content.NoFiles
            false -> Content.Main(
                files = filesToShow,
                //todo re-set after file operations or files list changes
                initialFile = filesToShow.find { it.path == initialFilePath } ?: filesToShow.first()
            )
        }
        
        UiState(
            showControls = controlsIsShown,
            isRefreshing = albumsResource is Resource.Loading,
            content = content,
            dialog = dialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState(
            showControls = true,
            isRefreshing = false,
            content = Content.Initiating,
            dialog = Dialog.None
        )
    )
    
    
    fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Launch -> onLaunch(action.initialFilePath, action.searchQuery)
                Action.SwitchControls -> onControlsVisibilityChange()
                is Action.Copy -> onCopyOrMove(listOf(action.file))
                is Action.Move -> onCopyOrMove(listOf(action.file), true)
                is Action.Rename -> onRename(listOf(action.file))
                is Action.Delete -> onDelete(listOf(action.file))
            }
        }
    }
    
    
    private fun onLaunch(initialFilePath: String, searchQuery: String? = null) {
        _initialFilePath.update { initialFilePath }
        _searchQuery.update { searchQuery }
    }
    
    private fun onControlsVisibilityChange() {
        _controlsIsShownFlow.update { !it }
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
                    it.resume(pickedPath)
                },
                onDismiss = {
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
                    it.resume(newName)
                },
                onDismiss = {
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
                    it.resume(resolution)
                },
                onDismiss = {
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
