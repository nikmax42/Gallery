package mtree.viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mtree.core.data.MediaItemData
import mtree.core.data.MediaItemsRepo
import mtree.core.data.Resource
import mtree.core.domain.models.FileOperation
import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.Sort
import mtree.core.domain.usecases.CreateItemsListToDisplayUc
import mtree.core.domain.usecases.PerformFileOperationsUc
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesUtils
import mtree.core.ui.models.ConflictResolutionUi
import mtree.core.ui.models.MediaItemUI
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    galleryAlbumsRepo: MediaItemsRepo,
    @ApplicationContext private val context: Context,
    private val createItemsListToDisplayUc: CreateItemsListToDisplayUc,
    private val performFileOperationsUc: PerformFileOperationsUc
) : ViewModel() {
    
    private val _initialFilePath = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _controlsIsShownFlow = MutableStateFlow(true)
    private val _dialog = MutableStateFlow<Dialog>(Dialog.None)
    
    val uiState = combine(
        galleryAlbumsRepo.getMediaAlbumsFlow(),
        MtreePreferencesUtils.getPreferencesFlow(context),
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
                is Action.Launch -> onLaunch(action.filePath)
                Action.SwitchControls -> onControlsVisibilityChange()
                is Action.Copy -> onCopyOrMove(listOf(action.file))
                is Action.Move -> onCopyOrMove(listOf(action.file), true)
                is Action.Rename -> onRename(listOf(action.file))
                is Action.Delete -> onDelete(listOf(action.file))
            }
        }
    }
    
    
    private fun onLaunch(filePath: String) {
        _initialFilePath.update { filePath }
    }
    
    private fun onControlsVisibilityChange() {
        _controlsIsShownFlow.update { !it }
    }
    
    private suspend fun onCopyOrMove(items: List<MediaItemUI>, move: Boolean = false) {
        try {
            val destinationDirectory = awaitForDestinationPath()
            var conflictResolutionForAll: ConflictResolutionUi? = null
            val operations = items.map { item ->
                val newPath = "$destinationDirectory/${item.name}"
                //if target file already exists:
                // if resolution with "apply to all" checkbox is not set yet - show dialog
                // else use "applied to all" resolution
                val conflictResolution = when (Path(newPath).exists()) {
                    true -> when (conflictResolutionForAll == null) {
                        true -> awaitForConflictResolution(item).let { resolution ->
                            if (resolution.applyToAll) conflictResolutionForAll = resolution
                            resolution
                        }
                        false -> conflictResolutionForAll!!
                    }
                    false -> ConflictResolutionUi.default
                }
                when (move) {
                    true -> FileOperation.Move(
                        sourceFilePath = item.path,
                        destinationFilePath = newPath,
                        conflictResolution = conflictResolution.mapToDomain()
                    )
                    false -> FileOperation.Copy(
                        sourceFilePath = item.path,
                        destinationFilePath = newPath,
                        conflictResolution = conflictResolution.mapToDomain()
                    )
                }
            }
            performFileOperations(operations)
        }
        catch (e: Exception) {
            setDialog(Dialog.None)
        }
    }
    
    private suspend fun onRename(items: List<MediaItemUI>) {
        try {
            var conflictResolutionForAll: ConflictResolutionUi? = null
            val operations = items.map { item ->
                val newPath = awaitForNewName(item).let { newName ->
                    val directoryPath = Path(item.path).parent.pathString
                    "$directoryPath/$newName"
                }
                //if target file already exists:
                // if resolution with "apply to all" checkbox is not set yet - show dialog
                // else use "applied to all" resolution
                val conflictResolution = when (Path(newPath).exists()) {
                    true -> when (conflictResolutionForAll == null) {
                        true -> awaitForConflictResolution(item).let { resolution ->
                            if (resolution.applyToAll) conflictResolutionForAll = resolution
                            resolution
                        }
                        false -> conflictResolutionForAll!!
                    }
                    false -> ConflictResolutionUi.default
                }
                FileOperation.Rename(
                    originalFilePath = item.path,
                    newFilePath = newPath,
                    conflictResolution = conflictResolution.mapToDomain()
                )
            }
            performFileOperations(operations)
        }
        catch (e: Exception) {
            setDialog(Dialog.None)
        }
    }
    
    private suspend fun onDelete(itemsToDelete: List<MediaItemUI>) {
        try {
            awaitForConfirmation(itemsToDelete)
            val operations = itemsToDelete.map {
                FileOperation.Delete(it.path)
            }
            performFileOperations(operations)
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
    
    private fun performFileOperations(operations: List<FileOperation>) {
        performFileOperationsUc.execute(operations)
    }
}
