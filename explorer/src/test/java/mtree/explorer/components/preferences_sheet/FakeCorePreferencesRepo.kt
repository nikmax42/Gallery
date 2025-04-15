package mtree.explorer.components.preferences_sheet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nikmax.mtree.core.preferences.CorePreferences
import nikmax.mtree.core.preferences.CorePreferencesRepo

class FakeCorePreferencesRepo : CorePreferencesRepo {
    
    private val _corePrefsFLow = MutableStateFlow(CorePreferences())
    
    override fun getPreferencesFlow(): Flow<CorePreferences> {
        return _corePrefsFLow
    }
    
    override suspend fun savePreferences(preferences: CorePreferences) {
        _corePrefsFLow.update { preferences }
    }
}
