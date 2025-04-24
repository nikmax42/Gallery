package mtree.explorer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesRepo

internal class FakePrefsRepo : MtreePreferencesRepo {
    
    private val _prefs = MutableStateFlow(MtreePreferences.default())
    
    override fun getPreferencesFlow(): Flow<MtreePreferences> {
        return _prefs
    }
    
    override suspend fun savePreferences(preferences: MtreePreferences) {
        _prefs.update { preferences }
    }
}
