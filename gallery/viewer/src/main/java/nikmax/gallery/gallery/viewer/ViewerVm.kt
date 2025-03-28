package nikmax.gallery.gallery.viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nikmax.gallery.core.data.Resource
import nikmax.gallery.gallery.core.data.media.ConflictResolution
import nikmax.gallery.gallery.core.data.media.FileOperation
import nikmax.gallery.gallery.core.data.media.MediaItemsRepo
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.ItemsUtils.createItemsListToDisplay
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
    private val mediaItemsRepo: MediaItemsRepo,
) : ViewModel() {
    
    data class UIState(
        val files: List<MediaItemUI.File> = emptyList(),
        val isLoading: Boolean = false,
        val showControls: Boolean = true,
        val dialog: nikmax.material_tree.gallery.dialogs.Dialog = nikmax.material_tree.gallery.dialogs.Dialog.None
    )
    
    
    sealed interface UserAction {
        data class Launch(val filePath: String) : UserAction
        data object SwitchControls : UserAction
        data class Copy(val file: MediaItemUI.File) : UserAction
        data class Move(val file: MediaItemUI.File) : UserAction
        data class Rename(val file: MediaItemUI.File) : UserAction
        data class Delete(val file: MediaItemUI.File) : UserAction
    }
    
    
    sealed interface Event {
    
    }
    
    
    // Raw data flows
    private val _galleryPreferencesFlow = GalleryPreferencesUtils.getPreferencesFlow(context)
    private val _dataResourceFlow = mediaItemsRepo.getFilesResourceFlow()
    
    // UI-related data flows
    private val _filesFlow = MutableStateFlow(emptyList<MediaItemUI.File>())
    private val _isLoadingFlow = MutableStateFlow(false)
    private val _controlsIsShownFlow = MutableStateFlow(true)
    
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()
    
    
    fun onAction(action: UserAction) {
        viewModelScope.launch {
            when (action) {
                is UserAction.Launch -> onLaunch(action.filePath)
                UserAction.SwitchControls -> onControlsVisibilityChange()
                is UserAction.Copy -> onCopyOrMove(listOf(action.file))
                is UserAction.Move -> onCopyOrMove(listOf(action.file), true)
                is UserAction.Rename -> onRename(listOf(action.file))
                is UserAction.Delete -> onDelete(listOf(action.file))
            }
        }
    }
    
    
    private fun onLaunch(filePath: String) {
        viewModelScope.launch {
            Path(filePath).parent.pathString.let { albumPath ->
                updateFilesFlow(albumPath)
            }
        }
        viewModelScope.launch { updateLoadingFlow() }
        
        viewModelScope.launch { reflectFilesChanges() }
        viewModelScope.launch { reflectLoadingChanges() }
        viewModelScope.launch { reflectControlsVisibilityChanges() }
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
    
    
    private suspend fun updateFilesFlow(albumPath: String) {
        combine(_dataResourceFlow, _galleryPreferencesFlow) { dataRes, prefs ->
            when (dataRes) {
                is Resource.Success -> dataRes.data
                is Resource.Loading -> dataRes.data
                is Resource.Error -> emptyList()
            }.createItemsListToDisplay(
                targetAlbumPath = albumPath,
                treeModeEnabled = prefs.appearance.nestedAlbumsEnabled,
                includeImages = prefs.filtering.includeImages,
                includeVideos = prefs.filtering.includeVideos,
                includeGifs = prefs.filtering.includeGifs,
                includeUnHidden = prefs.filtering.includeUnHidden,
                includeHidden = prefs.filtering.includeHidden,
                includeFiles = true,
                includeAlbums = false,
                sortingOrder = prefs.sorting.order,
                descendSorting = prefs.sorting.descend,
            ).filterIsInstance<MediaItemUI.File>()
        }.collectLatest { actualItemsList ->
            _filesFlow.update { actualItemsList }
        }
    }
    
    private suspend fun updateLoadingFlow() {
        _dataResourceFlow.collectLatest { filesDataResource ->
            _isLoadingFlow.update {
                filesDataResource is Resource.Loading
            }
        }
    }
    
    
    private suspend fun reflectFilesChanges() {
        _filesFlow.collectLatest { newFiles ->
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(files = newFiles) }
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
    
    private suspend fun reflectControlsVisibilityChanges() {
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
    
    private fun setDialog(newDialog: nikmax.material_tree.gallery.dialogs.Dialog) {
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
