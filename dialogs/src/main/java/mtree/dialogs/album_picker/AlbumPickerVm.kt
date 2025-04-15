package mtree.dialogs.album_picker

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
import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.Sort
import mtree.core.domain.usecases.CreateItemsListToDisplayUc
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesUtils
import mtree.core.ui.models.MediaItemUI
import javax.inject.Inject

@HiltViewModel
class AlbumPickerVm
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val albumsRepo: MediaItemsRepo,
    private val createItemsListToDisplayUc: CreateItemsListToDisplayUc,
) : ViewModel() {
    
    private val _openedAlbumsStack = MutableStateFlow(listOf<MediaItemUI.Album>())
    
    val uiState = combine(
        albumsRepo.getMediaAlbumsFlow(),
        MtreePreferencesUtils.getPreferencesFlow(context),
        _openedAlbumsStack
    ) {
        val albumsResource = it[0] as Resource<List<MediaItemData.Album>>
        val prefs = it[1] as MtreePreferences
        val openedAlbumsStack = it[2] as List<MediaItemUI.Album>
        
        val isLoading = albumsResource is Resource.Loading
        val itemsToDisplay = when (albumsResource) {
            is Resource.Success -> albumsResource.data
            is Resource.Loading -> albumsResource.data
            is Resource.Error -> emptyList()
        }.map { albumsData ->
            albumsData.mapToDomain() as MediaItemDomain.Album
        }.let { albumsDomain ->
            createItemsListToDisplayUc.execute(
                galleryAlbums = albumsDomain,
                basePath = openedAlbumsStack.lastOrNull()?.path,
                searchQuery = null,
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
        }
        
        val content = TODO()
        
        UiState(
            currentAlbum = openedAlbumsStack.lastOrNull(),
            isLoading = isLoading,
            portraitGridColumns = prefs.portraitGridColumns,
            landscapeGridColumns = prefs.landscapeGridColumns,
            content = content
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState(
            currentAlbum = null,
            isLoading = false,
            portraitGridColumns = 3,
            landscapeGridColumns = 4,
            content = Content.Initialization,
        )
    )
    
    internal fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Launch -> onLaunch()
                is Action.NavigateInsideAlbum -> navigateIntoAlbum(action.album)
                Action.NavigateBack -> navigateBack()
                Action.Refresh -> onRefresh()
            }
        }
    }
    
    
    private fun onLaunch() {
        viewModelScope.launch { resetNavStack() }
    }
    
    private fun resetNavStack() {
        _openedAlbumsStack.update { emptyList() }
    }
    
    private fun navigateIntoAlbum(album: MediaItemUI.Album) {
        _openedAlbumsStack.update { it + album }
    }
    
    private suspend fun navigateBack() {
        /*  when (_openedAlbumsStack.value.isNotEmpty()) {
             true -> _openedAlbumsStack.update { it.dropLast(1) }
             false -> _event.emit(Event.DismissDialog)
         } */
        _openedAlbumsStack.update { it.dropLast(1) }
    }
    
    private suspend fun onRefresh() {
        albumsRepo.rescan()
    }
}
