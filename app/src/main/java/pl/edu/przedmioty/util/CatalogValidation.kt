package pl.edu.przedmioty.util

import pl.edu.przedmioty.model.ItemDraft

object CatalogValidation {
    const val MAX_IMAGE_BASE64_LENGTH = 650_000

    fun validateItem(draft: ItemDraft): Map<String, String> = buildMap {
        val name = draft.name.trim()
        val category = draft.category.trim()

        if (name.length !in 2..80) {
            put("name", "Nazwa musi mieć od 2 do 80 znaków.")
        }
        if (draft.description.length > 500) {
            put("description", "Opis może mieć maksymalnie 500 znaków.")
        }
        if (category.length !in 2..40) {
            put("category", "Kategoria musi mieć od 2 do 40 znaków.")
        }
        if (draft.imageBase64.length > MAX_IMAGE_BASE64_LENGTH) {
            put("image", "Zdjęcie jest zbyt duże. Wykonaj zdjęcie ponownie.")
        }
    }

    fun validateEmail(email: String): String? {
        val normalized = email.trim()
        val basicEmailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        return if (basicEmailRegex.matches(normalized)) null else "Wpisz poprawny adres e-mail."
    }

    fun validatePassword(password: String): String? =
        if (password.length >= 6) null else "Hasło musi mieć co najmniej 6 znaków."
}