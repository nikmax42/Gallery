package mtree.core.utils

object RenamingUtils {
    
    /**
     * Checks if file name is valid.
     *
     * Valid filename:
     *
     * 1. starts with a letter or a dot
     * 2. don't start with minus or space
     * 2. contains only letters, numbers - and _
     * 3. length is between 1 and 255 symbols
     *
     * @return true if valid, false if not
     */
    fun fileNameIsValid(filename: String): Boolean {
        val filenameRegex = Regex("^(?!- )\\.*[\\w\\-_ +]{1,255}$")
        return filenameRegex.matches(filename)
    }
    
    fun fileExtensionIsValid(extension: String): Boolean {
        val extensionWithDot = if (extension.startsWith('.')) extension else ".${extension}"
        val extensionRegex = Regex("\\.[a-zA-Z0-9]{1,10}\$")
        return extensionRegex.matches(extensionWithDot)
    }
}
