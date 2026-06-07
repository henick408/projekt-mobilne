package pl.edu.przedmioty.util

import pl.edu.przedmioty.model.CatalogItem
import java.util.Locale

object SearchMatcher {
    fun matches(item: CatalogItem, query: String): Boolean {
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        if (normalizedQuery.isBlank()) return true

        return listOf(item.name, item.description, item.category)
            .any { value -> value.lowercase(Locale.getDefault()).contains(normalizedQuery) }
    }
}