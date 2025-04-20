package mtree.core.preferences

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Singleton
    @Provides
    fun providePreferencesRepo(@ApplicationContext context: Context): MtreePreferencesRepo {
        return MtreePreferencesRepoImpl(context)
    }
}
