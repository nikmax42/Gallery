package mtree.explorer

sealed interface Snackbar {
    data class CopyingStarted(val itemsCount: Int) : Snackbar
    data object CopyingFinished : Snackbar
    
    data class MovingStarted(val itemsCount: Int) : Snackbar
    data object MovingFinished : Snackbar
    
    data class RenamingStarted(val itemsCount: Int) : Snackbar
    data object RenamingFinished : Snackbar
    
    data class DeletionStarted(val itemsCount: Int) : Snackbar
    data object DeletionFinished : Snackbar
}
