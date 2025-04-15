package mtree.viewer

data class UiState(
    val showControls: Boolean = true,
    val isRefreshing: Boolean = false,
    val content: Content = Content.Initiating,
    val dialog: Dialog = Dialog.None
)
