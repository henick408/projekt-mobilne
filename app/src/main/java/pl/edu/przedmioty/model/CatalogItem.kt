package pl.edu.przedmioty.model

data class CatalogItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val imageBase64: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class ItemDraft(
    val name: String,
    val description: String,
    val category: String,
    val imageBase64: String,
)