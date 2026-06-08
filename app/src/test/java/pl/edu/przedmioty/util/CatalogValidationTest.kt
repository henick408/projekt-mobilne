package pl.edu.przedmioty.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.edu.przedmioty.model.ItemDraft

class CatalogValidationTest {
    private val valid = ItemDraft("Laptop", "Komputer", "Elektronika", "")

    @Test fun validItemHasNoErrors() {
        assertTrue(CatalogValidation.validateItem(valid).isEmpty())
    }

    @Test fun shortNameIsRejected() {
        assertEquals("Nazwa musi mieć od 2 do 80 znaków.", CatalogValidation.validateItem(valid.copy(name = "A"))["name"])
    }

    @Test fun longNameIsRejected() {
        assertTrue(CatalogValidation.validateItem(valid.copy(name = "A".repeat(81))).containsKey("name"))
    }

    @Test fun longDescriptionIsRejected() {
        assertTrue(CatalogValidation.validateItem(valid.copy(description = "A".repeat(501))).containsKey("description"))
    }

    @Test fun shortCategoryIsRejected() {
        assertTrue(CatalogValidation.validateItem(valid.copy(category = "X")).containsKey("category"))
    }

    @Test fun tooLargeImageIsRejected() {
        assertTrue(CatalogValidation.validateItem(valid.copy(imageBase64 = "A".repeat(650001))).containsKey("image"))
    }

    @Test fun correctEmailIsAccepted() {
        assertNull(CatalogValidation.validateEmail("student@example.com"))
    }

    @Test fun incorrectEmailIsRejected() {
        assertEquals("Wpisz poprawny adres e-mail.", CatalogValidation.validateEmail("student"))
    }

    @Test fun sixCharacterPasswordIsAccepted() {
        assertNull(CatalogValidation.validatePassword("123456"))
    }

    @Test fun shortPasswordIsRejected() {
        assertEquals("Hasło musi mieć co najmniej 6 znaków.", CatalogValidation.validatePassword("12345"))
    }
}