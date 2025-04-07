package nikmax.gallery.gallery.viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.Path
import kotlin.io.path.pathString

@HiltViewModel
class ViewerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepo: GalleryPreferencesRepo,
    private val mediaItemsRepo: MediaItemsRepo,
) : ViewModel() {
    
    
    
    // Raw data flows
    private val _galleryPreferencesFlow = MutableStateFlow(GalleryPreferences())
    private val _dataResourceFlow: MutableStateFlow<Resource<List<MediaItemData>>> = MutableStateFlow(
        Resource.Loading(emptyList())
    )
    
    // UI-related data flows
    private val _filesFlow = MutableStateFlow(emptyList<MediaItemUI.File>())
    private val _isLoadingFlow = MutableStateFlow(false)
    private val _controlsIsShownFlow = MutableStateFlow(true)
    
    private val _uiState = MutableStateFlow(UiState())
    internal val uiState = _uiState.asStateFlow()
    
    private val _event = MutableSharedFlow<Event>()
    internal val event = _event.asSharedFlow()
    
    
    internal fun onAction(action: Action) {
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
        _uiState.update { it.copy(content = UiState.Content.Initiating) }
        viewModelScope.launch {
            Path(filePath).parent.pathString.let { albumPath ->
                keepDataFlowUpdated(albumPath)
            }
        }
        
        viewModelScope.launch { keepPreferences() }
        viewModelScope.launch { keepFilesFlow() }
        viewModelScope.launch { keepLoadingFlow() }
        
        viewModelScope.launch { reflectFilesFlow() }
        viewModelScope.launch { reflectLoadingFlow() }
        viewModelScope.launch { reflectControlsVisibilityFlow() }
    }
    
    
    private suspend fun onCopyOrMove(
        files: List<MediaItemUI.File>,
        move: Boolean = false
    ) {
        val destinationAlbumPath = try {
            awaitForAlbumPickerResult()
        }
        catch (e: CancellationException) {
            return
        }
        val operations = files.map { item ->
            val destinationFilePath = "$destinationAlbumPath/${item.name}"
            val conflicts = mediaItemsRepo.checkExistence(destinationFilePath)
            val resolution = when (conflicts) {
                true -> try {
                    awaitForConflictResolverResult(item)
                }
                catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
            when (move) {
                true -> FileOperation.Move(item.path, destinationFilePath, resolution)
                false -> FileOperation.Copy(item.path, destinationFilePath, resolution)
            }
        }
        performFileOperations(operations)
    }
    
    private suspend fun onRename(files: List<MediaItemUI>) {
        val operations = files.map { item ->
            val newPath = try {
                awaitForRenamerResult(item)
            }
            catch (e: CancellationException) {
                return
            }
            val conflicts = mediaItemsRepo.checkExistence(newPath)
            val resolution = when (conflicts) {
                true -> try {
                    awaitForConflictResolverResult(item)
                }
                catch (e: CancellationException) {
                    return
                }
                false -> ConflictResolution.KEEP_BOTH
            }
            FileOperation.Rename(item.path, newPath, resolution)
        }
        performFileOperations(operations)
    }
    
    private suspend fun onDelete(files: List<MediaItemUI>) {
        val deletionConfirmed = try {
            awaitForDeletionConfirm(files)
        }
        catch (e: CancellationException) {
            false
        }
        val operations = when (deletionConfirmed) {
            true -> files.map { FileOperation.Delete(it.path) }
            false -> return
        }
        performFileOperations(operations)
    }
    
    private fun onControlsVisibilityChange() {
        _controlsIsShownFlow.update { !it }
    }
    
    
    private suspend fun keepPreferences() {
        prefsRepo
            .getPreferencesFlow()
            .collectLatest { prefs ->
                _galleryPreferencesFlow.update { prefs }
            }
    }
    
    private suspend fun keepDataFlowUpdated(albumPath: String) {
        _galleryPreferencesFlow.collectLatest { prefs ->
            mediaItemsRepo.getAlbumContentFlow(
                path = albumPath,
                searchQuery = null,
                treeMode = false,
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
            ).collectLatest { albumFilesData ->
                _dataResourceFlow.update { albumFilesData }
            }
        }
    }
    
    private suspend fun keepFilesFlow() {
        combine(_dataResourceFlow, _galleryPreferencesFlow) { dataRes, prefs ->
            when (dataRes) {
                is Resource.Success -> dataRes.data
                is Resource.Loading -> dataRes.data
                is Resource.Error -> emptyList()
            }.mapToUi().filterIsInstance<MediaItemUI.File>()
        }.collectLatest { actualItemsList ->
            _filesFlow.update { actualItemsList }
        }
    }
    
    private suspend fun keepLoadingFlow() {
        _dataResourceFlow.collectLatest { filesDataResource ->
            _isLoadingFlow.update {
                filesDataResource is Resource.Loading
            }
        }
    }
    
    
    private suspend fun reflectFilesFlow() {
        _filesFlow.collectLatest { newFiles ->
            withContext(Dispatchers.Main) {
                when (newFiles.isEmpty() && _uiState.value.content !is UiState.Content.Initiating) {
                    //if there is no files remains after update - close viewer
                    true -> _event.emit(Event.CloseViewer)
                    false -> _uiState.update {
                        it.copy(
                            content = UiState.Content.Main(newFiles)
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun reflectLoadingFlow() {
        _isLoadingFlow.collectLatest { isLoading ->
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(refreshing = isLoading) }
            }
        }
    }
    
    private suspend fun reflectControlsVisibilityFlow() {
        _controlsIsShownFlow.collectLatest { controlsIsShown ->
            _uiState.update { it.copy(showControls = controlsIsShown) }
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
    
    private suspend fun awaitForRenamerResult(itemToRename: MediaItemUI): String {
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
    
    private suspend fun awaitForDeletionConfirm(itemsToDelete: List<MediaItemUI>): Boolean {
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
    
    private suspend fun awaitForConflictResolverResult(conflictItem: MediaItemUI): ConflictResolution {
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
    
    
    private suspend fun performFileOperations(operations: List<FileOperation>) {
        var completeOperationsCount = 0
        mediaItemsRepo
            .executeFileOperations(operations)
            .observeForever { workInfos ->
                workInfos.count { it.state.isFinished }.let {
                    if (it > completeOperationsCount) {
                        completeOperationsCount = it
                        viewModelScope.launch { mediaItemsRepo.rescan() }
                    }
                }
                /* TODO: post progress notification here */
            }
    }
}
