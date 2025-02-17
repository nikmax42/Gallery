package nikmax.gallery.explorer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import nikmax.gallery.core.MediaItemDataToUiMapper
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.Resource
import nikmax.gallery.data.media.MediaFileData
import nikmax.gallery.data.media.MediaItemsRepo
import nikmax.gallery.data.preferences.GalleryPreferences
import nikmax.gallery.data.preferences.PreferencesRepo
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.pathString


@HiltViewModel
class ExplorerVm
@Inject constructor(
    private val mediaItemsRepo: MediaItemsRepo,
    private val prefsRepo: PreferencesRepo
) : ViewModel() {
    data class UIState(
        val items: List<MediaItemUI> = emptyList(),
        val loading: Boolean = false,
        val mode: Mode = Mode.Viewing,
        val preferences: GalleryPreferences = GalleryPreferences()
    ) {
        sealed interface Mode {
            data object Viewing : Mode
            data class Selection(
                val items: List<MediaItemUI>,
                val selectedItems: List<MediaItemUI>
            ) : Mode

            data class Searching(
                val searchQuery: String = "",
                val foundedItems: List<MediaItemUI> = emptyList()
            ) : Mode
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
                items = newItems,
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
        // if nested albums view selected - show album content
        // if plain albums view selected - show plain albums list or opened album files
        val itemsUi = when (albumsMode) {
            GalleryPreferences.AlbumsMode.PLAIN -> {
                when (albumPath == null) {
                    true -> createFlatAlbumsList(filesData)
                    false -> createAlbumOwnFilesList(filesData, albumPath)
                }
            }
            GalleryPreferences.AlbumsMode.NESTED -> createNestedAlbumsList(
                files = filesData,
                albumPath = albumPath ?: "/storage/"
            )
        }.let {
            when (showHidden) {
                true -> it
                false -> it.filterNot { itemUi -> itemUi.hidden }
            }
        }
        // apply filtering
        val filteredItemsUi = itemsUi.applyFilters(selectedFilters)
        // apply sorting
        val sortedItemsUi = filteredItemsUi.applySorting(
            sortingOrder, descendSortingEnabled
        )
        return sortedItemsUi
    }

    private fun List<MediaItemUI>.applyFilters(
        selectedFilters: Set<GalleryPreferences.Filter>
    ): List<MediaItemUI> {

        val imagesFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.IMAGES)) {
            true -> this.filter { item ->
                when (item) {
                    is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.IMAGE
                    is MediaItemUI.Album -> item.imagesCount > 0
                }
            }
            false -> emptyList()
        }

        val videosFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.VIDEOS)) {
            true -> this.filter { item ->
                when (item) {
                    is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.VIDEO
                    is MediaItemUI.Album -> item.videosCount > 0
                }
            }
            false -> emptyList()
        }

        val gifsFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.GIFS)) {
            true -> this.filter { item ->
                when (item) {
                    is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.GIF
                    is MediaItemUI.Album -> item.gifsCount > 0
                }
            }
            false -> emptyList()
        }

        return imagesFiltered + videosFiltered + gifsFiltered
    }

    private fun List<MediaItemUI>.applySorting(
        sortingOrder: GalleryPreferences.SortingOrder,
        descend: Boolean
    ): List<MediaItemUI> {
        return when (sortingOrder) {
            GalleryPreferences.SortingOrder.CREATION_DATE -> this.sortedBy { it.dateCreated }
            GalleryPreferences.SortingOrder.MODIFICATION_DATE -> this.sortedBy { it.dateModified }
            GalleryPreferences.SortingOrder.NAME -> this.sortedBy { it.name }
            GalleryPreferences.SortingOrder.SIZE -> this.sortedBy { it.size }
        }.let {
            when (descend) {
                true -> it.reversed()
                false -> it
            }
        }
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
            UIState.Mode.Viewing -> _uiState.update {
                it.copy(
                    mode = UIState.Mode.Selection(
                        items = _uiState.value.items,
                        selectedItems = listOf(item)
                    )
                )
            }
            is UIState.Mode.Searching -> _uiState.update {
                it.copy(
                    mode = UIState.Mode.Selection(
                        items = mode.foundedItems,
                        selectedItems = listOf(item)
                    )
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
                        mode = UIState.Mode.Selection(
                            items = mode.items,
                            selectedItems = newSelectedItems
                        )
                    )
                }
                _uiState.update { newState }
            }
        }
    }

    private fun selectAllItems() {
        val newState = when (val mode = _uiState.value.mode) {
            UIState.Mode.Viewing, is UIState.Mode.Selection -> _uiState.value.copy(
                mode = UIState.Mode.Selection(
                    items = _uiState.value.items,
                    selectedItems = _uiState.value.items
                )
            )
            is UIState.Mode.Searching -> _uiState.value.copy(
                mode = UIState.Mode.Selection(
                    items = mode.foundedItems,
                    selectedItems = mode.foundedItems
                )
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
        val filteredItems = _uiState.value.items.filter {
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


    private fun createFlatAlbumsList(files: List<MediaFileData>): List<MediaItemUI> {
        val albumsList = files
            // group files by its album path
            .groupBy { Path(it.path).parent.pathString }
            .let { groups ->
                // if only one album found - return its files list, else return albums list
                when (groups.size == 1) {
                    true -> createAlbumOwnFilesList(files, groups.keys.first())
                    false -> groups.map { MediaItemDataToUiMapper.mapToAlbum(it.value) }
                }
            }
        return albumsList
    }

    private fun createAlbumOwnFilesList(
        files: List<MediaFileData>,
        albumPath: String
    ): List<MediaItemUI.File> {
        return files
            .filter { Path(it.path).parent.pathString == albumPath }
            .map { MediaItemDataToUiMapper.mapToFile(it) }
    }

    /**
     * include target album's direct children
     * include target album's deep children that doesn't have any media albums between them and the target album
     * NOT include album if it's a target album's deep child, but has intermediate media album between.
     *
     *
     * For Example, we have the next directories structure:
     * ```
     * DCIM [3 images in directory itself]
     * DCIM/Pictures [2]
     * DCIM/Photos [0]
     * DCIM/Photos/Birthday [10]
     * DCIM/Photos/Cats [50]
     * DCIM/Movies/FolderWithoutOwnMedia/DeepFolderWithMedia [1]
     * ```
     * If the target albums is `/DCIM/`, then the next albums should be returned:
     * ```
     * DCIM [3] // Because have 3 own files
     * Pictures [2]
     * Photos [50] // Photos doesn't contain any files by itself, but has two child albums with media
     * DeepFolderWithMedia[1] // intermediate folder doesn't contain media files by itself, so `FolderWithoutOwnMedia` will be skipped
     * ```
     */
    private fun createNestedAlbumsList(
        files: List<MediaFileData>,
        albumPath: String?
    ): List<MediaItemUI> {
        val nestedFiles = files.filter { it.path.startsWith(albumPath.toString()) }
        val subAlbums = nestedFiles
            .groupBy { Path(it.path).parent.pathString }
            .map { MediaItemDataToUiMapper.mapToAlbum(it.value) }
            .filterNot { it.path == albumPath }
        // 1. filter albums that not include other album full path
        val filteredNestedAlbums = subAlbums.filterNot { album ->
            subAlbums
                .filterNot { it == album }
                .any { otherAlbum -> album.path.startsWith(otherAlbum.path) }
        }
        // 2. filter album own files
        val ownFiles = files.filter { file ->
            Path(file.path).parent.pathString == albumPath
        }.map { MediaItemDataToUiMapper.mapToFile(it) }
        return filteredNestedAlbums + ownFiles
    }


    private suspend fun updatePreferences(preferences: GalleryPreferences) {
        prefsRepo.savePreferences(preferences)
    }
}
