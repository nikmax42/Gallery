package mtree.explorer

sealed interface Content {
    data object Initialization : Content
    data object Main : Content
    data object NothingToDisplay : Content
}
