package nikmax.mtree.gallery.explorer

import nikmax.mtree.gallery.core.ui.MediaItemUI

internal sealed interface Event {
    data class OpenViewer(val file: MediaItemUI.File) : Event
    data class ShowSnackbar(val snackbar: SnackBar) : Event
}
