package nikmax.mtree.gallery.viewer

internal sealed interface Event {
    data object CloseViewer : Event
}
