package mtree.core.domain.usecases

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
internal object UsecasesModule {
    
    @Provides
    fun provideCreateItemsListToDisplayUc(): CreateItemsListToDisplayUc {
        return CreateItemsListToDisplayUcImpl()
    }
    
    @Provides
    fun provideCopyOrMoveItemsUc(@ApplicationContext context: Context): CopyOrMoveItemsUc {
        return CopyOrMoveItemsUcImpl(context = context)
    }
    
    @Provides
    fun provideRenameItemsUc(@ApplicationContext context: Context): RenameItemsUc {
        return RenameItemsUcImpl(context = context)
    }
    
    @Provides
    fun provideDeleteItemsUc(@ApplicationContext context: Context): DeleteItemsUc {
        return DeleteItemsUcImpl(context = context)
    }
}
