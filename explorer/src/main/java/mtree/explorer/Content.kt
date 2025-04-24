package mtree.explorer

sealed interface Content {
    data object Shimmer : Content
    data object Main : Content
    data object NothingToDisplay : Content
}
