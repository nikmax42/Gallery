package nikmax.gallery.gallery.viewer

internal sealed interface Event {
    data object CloseViewer : Event
}
