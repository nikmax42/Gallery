package nikmax.mtree.gallery.viewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nikmax.mtree.gallery.core.data.preferences.GalleryPreferences
import nikmax.mtree.gallery.core.data.preferences.GalleryPreferencesRepo

class FakeGalleryPreferencesRepo(
    private val initialValues: GalleryPreferences = GalleryPreferences()
) : GalleryPreferencesRepo {
    
    private val _prefsFlow = MutableStateFlow(initialValues)
    
    override fun getPreferencesFlow(): Flow<GalleryPreferences> {
        return _prefsFlow
    }
    
    override suspend fun savePreferences(preferences: GalleryPreferences) {
        _prefsFlow.update { preferences }
    }
    
}
