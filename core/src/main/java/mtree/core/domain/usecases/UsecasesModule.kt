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
    fun providePerformFileOperationsUc(@ApplicationContext context: Context): PerformFileOperationsUc {
        return PerformFileOperationsUcImpl(context = context)
    }
}
