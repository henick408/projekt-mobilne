package pl.edu.przedmioty.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.edu.przedmioty.model.CatalogItem

class SearchMatcherTest {
    private val item = CatalogItem(
        id = "1",
        name = "Laptop Lenovo",
        description = "Komputer do nauki",
        category = "Elektronika",
    )

    @Test fun blankQueryMatchesEverything() {
        assertTrue(SearchMatcher.matches(item, ""))
    }

    @Test fun matchesNameIgnoringCase() {
        assertTrue(SearchMatcher.matches(item, "lenovo"))
    }

    @Test fun matchesDescription() {
        assertTrue(SearchMatcher.matches(item, "nauki"))
    }

    @Test fun matchesCategory() {
        assertTrue(SearchMatcher.matches(item, "ELEKTRONIKA"))
    }

    @Test fun unrelatedQueryDoesNotMatch() {
        assertFalse(SearchMatcher.matches(item, "rower"))
    }
}