package nikmax.gallery.data.media

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaDataModule {

    @Singleton
    @Provides
    fun providesMediaItemsRepo(@ApplicationContext context: Context): MediaItemsRepo {
        val mediaStoreDsImpl = MediaStoreDsImpl(context)
        return MediaItemRepoImpl(mediaStoreDsImpl)
    }
}
