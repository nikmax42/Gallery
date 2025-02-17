package nikmax.gallery.dialogs.renaming

internal object RenamingUtils {

    fun fileNameIsValid(filename: String): Boolean {
        val filenameRegex = Regex("^(?!-)[\\w+]{1,255}\$")
        return filenameRegex.matches(filename)
    }

    fun fileExtensionIsValid(extension: String): Boolean {
        val extensionWithDot = if (extension.startsWith('.')) extension else ".${extension}"
        val extensionRegex = Regex("\\.[a-zA-Z0-9]{1,10}\$")
        return extensionRegex.matches(extensionWithDot)
    }
}
