package nikmax.mtree.gallery.core.data.media

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MediaDataModule {
    
    @Singleton
    @Provides
    fun providesMediaItemsRepo(
        @ApplicationContext context: Context,
        mediastoreDs: MediastoreDs
    ): MediaItemsRepo {
        return MediaItemRepoImpl(
            context = context,
            mediastoreDs = mediastoreDs
        )
    }
    
    @Singleton
    @Provides
    fun providesMediastoreDs(@ApplicationContext context: Context): MediastoreDs {
        return MediastoreDsImpl(context = context)
    }
}
