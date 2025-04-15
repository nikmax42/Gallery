package mtree.core.domain.models

data class Sort(
    val order: Order,
    val descend: Boolean,
    val placeFirst: PlaceFirst
) {
    enum class Order { NAME, EXTENSION, SIZE, CREATION_DATE, MODIFICATION_DATE, RANDOM }
    enum class PlaceFirst { ALBUMS, FILES, NONE }
    
    companion object {
        fun byNameAscendWithoutPlacing() = Sort(
            order = Order.NAME,
            descend = false,
            placeFirst = PlaceFirst.NONE
        )
    }
}
