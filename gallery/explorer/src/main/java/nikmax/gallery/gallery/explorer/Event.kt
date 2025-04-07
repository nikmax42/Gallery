package nikmax.gallery.gallery.explorer

import nikmax.gallery.gallery.core.ui.MediaItemUI

internal sealed interface Event {
    data class OpenViewer(val file: MediaItemUI.File) : Event
    data class ShowSnackbar(val snackbar: SnackBar) : Event
}
