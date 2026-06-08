# Item Catalog

Aplikacja mobilna na system Android do zarządzania osobistym katalogiem przedmiotów. Pozwala zalogowanemu użytkownikowi dodawać, przeglądać, edytować i usuwać przedmioty — każdy z nazwą, kategorią, opisem oraz opcjonalnym zdjęciem wykonanym aparatem urządzenia.

## Spis treści

- [Opis projektu](#opis-projektu)
- [Instrukcja uruchomienia](#instrukcja-uruchomienia)
- [Zrzuty ekranu](#zrzuty-ekranu)
- [Diagram architektury](#diagram-architektury)
- [Opis API](#opis-api)
- [Instrukcja testowania](#instrukcja-testowania)
- [Wkład własny](#wkład-własny)

---

## Opis projektu

Item Catalog to aplikacja napisana w Kotlinie z wykorzystaniem Jetpack Compose i Material 3. Każdy użytkownik posiada własny, odizolowany katalog przedmiotów przechowywany w Cloud Firestore, a logowanie i rejestracja obsługiwane są przez Firebase Authentication (e-mail/hasło).

Najważniejsze funkcje:
- rejestracja i logowanie użytkownika,
- lista przedmiotów aktualizowana w czasie rzeczywistym (nasłuchiwanie zmian w Firestore),
- wyszukiwanie/filtrowanie przedmiotów po nazwie, kategorii i opisie,
- dodawanie i edycja przedmiotu wraz z wykonaniem zdjęcia aparatem,
- podgląd szczegółów przedmiotu oraz jego usuwanie (z potwierdzeniem),
- walidacja danych formularza.

Aplikacja zbudowana jest w architekturze MVVM (Model–View–ViewModel) z osobnym ViewModelem dla każdego ekranu oraz warstwą repozytoriów odseparowującą logikę biznesową od wywołań Firebase.

### Stos technologiczny

| Warstwa | Technologia |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Nawigacja | Navigation Compose (zagnieżdżone grafy) |
| Zarządzanie stanem | `StateFlow` / `MutableStateFlow` |
| Architektura | MVVM, ViewModel na ekran |
| Uwierzytelnianie | Firebase Authentication (e-mail/hasło) |
| Baza danych | Cloud Firestore |
| Obsługa zdjęć | Aparat (`ActivityResultContracts.TakePicture`), kodowanie Base64 |
| Wstrzykiwanie zależności | Ręczne — `ViewModelProvider.Factory` |
| Testy | JUnit 4 (testy jednostkowe)|

---

## Instrukcja uruchomienia

### Wymagania

- Android Studio (najnowsza stabilna wersja)
- JDK 17
- Android SDK z `compileSdk 36`
- Urządzenie fizyczne lub emulator z obsługą aparatu (do testowania funkcji robienia zdjęć) i minimalnym poziomem API zgodnym z `minSdk` projektu
- Konto Firebase z utworzonym projektem (Authentication + Firestore)

### Kroki

1. **Sklonuj repozytorium**
   ```
   git clone <adres-repozytorium>
   cd item-catalog
   ```

2. **Skonfiguruj Firebase**
    - Utwórz projekt w [Firebase Console](https://console.firebase.google.com/), włącz **Authentication** (logowanie e-mail/hasło) oraz **Cloud Firestore**.
    - Pobierz plik `google-services.json` dla aplikacji o pakiecie `pl.edu.itemcatalog` i umieść go w katalogu `app/`.
    - *Uwaga:* plik `google-services.json` znajdujący się w repozytorium może być wersją demonstracyjną używaną wyłącznie w CI — do uruchomienia aplikacji z prawdziwym backendem należy podmienić go na własny.

3. **Wdróż reguły bezpieczeństwa Firestore** (opcjonalnie, jeśli korzystasz z własnego projektu Firebase)
   ```
   firebase deploy --only firestore:rules,firestore:indexes
   ```
   Reguły znajdują się w pliku `firestore.rules`, a konfiguracja indeksów w `firestore.indexes.json`.

4. **Otwórz projekt w Android Studio**
    - Poczekaj na zsynchronizowanie zależności Gradle.

5. **Uruchom aplikację**
    - Wybierz urządzenie/emulator i uruchom konfigurację `app` (Run ▶).
    - Przy pierwszym użyciu aparatu aplikacja poprosi o uprawnienie `CAMERA`.

### Budowanie i testowanie z linii poleceń

```
./gradlew assembleDebug          # zbudowanie aplikacji w wersji debug
./gradlew testDebugUnitTest      # uruchomienie testów jednostkowych
```

---


## Diagram architektury

```
┌─────────────────────────────────────────────────────────────────┐
│                         WARSTWA UI                               │
│   Ekrany Compose: LoginScreen, RegisterScreen, CatalogScreen,    │
│   ItemDetailScreen, ItemFormScreen, komponent Base64Image        │
└───────────────────────────────┬─────────────────────────────────┘
                                 │ StateFlow / zdarzenia UI
┌───────────────────────────────▼─────────────────────────────────┐
│                      WARSTWA VIEWMODEL                           │
│   LoginViewModel · RegisterViewModel · CatalogViewModel          │
│   (współdzielony w obrębie grafu nawigacji „catalog_graph”)      │
│   ItemDetailViewModel · ItemFormViewModel                        │
│   + obiekty pomocnicze: CatalogValidation, SearchMatcher,        │
│     ImageUtils                                                   │
└───────────────────────────────┬─────────────────────────────────┘
                                 │ wywołania repozytoriów
┌───────────────────────────────▼─────────────────────────────────┐
│                      WARSTWA REPOZYTORIÓW                        │
│   AuthRepository (opakowuje FirebaseAuth)                        │
│   ItemRepository (opakowuje Cloud Firestore, nasłuchiwanie       │
│   w czasie rzeczywistym)                                         │
└───────────────────────────────┬─────────────────────────────────┘
                                 │ SDK Firebase
┌───────────────────────────────▼─────────────────────────────────┐
│                          FIREBASE                                │
│   Firebase Authentication  ·  Cloud Firestore                    │
│   ścieżka dokumentów: users/{userId}/items/{itemId}              │
│   reguły bezpieczeństwa: signedIn() · owns() · validItem()       │
└─────────────────────────────────────────────────────────────────┘
```

Przepływ nawigacji między ekranami katalogu (graf `catalog_graph`):

```
LoginScreen ──(rejestracja)──▶ RegisterScreen
     │                               │
     └──────────(zalogowano)─────────┘
                     │
                     ▼
            ┌─────────────────────────────────────────┐
            │        catalog_graph (CatalogViewModel)  │
            │                                           │
            │   CatalogScreen ──▶ ItemDetailScreen      │
            │        │                  │               │
            │        └──────▶ ItemFormScreen ◀──────────┘
            └─────────────────────────────────────────┘
```

`CatalogViewModel` jest powiązany z całym grafem nawigacji (a nie pojedynczym ekranem), dzięki czemu nasłuchiwanie zmian w Firestore pozostaje aktywne podczas przechodzenia między ekranami katalogu, szczegółów i formularza — bez ponownego ładowania danych.

---

## Opis API

Aplikacja nie korzysta z własnego serwera ani REST API — cała logika backendowa oparta jest na usługach **Firebase**, z którymi komunikuje się wyłącznie warstwa repozytoriów.

### `AuthRepository` (opakowuje `FirebaseAuth`)

| Metoda | Opis |
|---|---|
| `isSignedIn()` | sprawdza, czy istnieje aktywna sesja użytkownika |
| `currentUserId` | identyfikator (UID) zalogowanego użytkownika — używany jako segment ścieżki w Firestore |
| `register(email, password, callback)` | tworzy nowe konto Firebase |
| `login(email, password, callback)` | loguje na istniejące konto |
| `logout()` | wylogowuje użytkownika |

### `ItemRepository` (opakowuje Cloud Firestore)

Wszystkie przedmioty przechowywane są pod ścieżką `users/{userId}/items/{itemId}`, dzięki czemu dane każdego użytkownika są odizolowane.

| Metoda | Opis |
|---|---|
| `listen(userId, onItemsChanged, onError)` | dołącza nasłuchiwacz `addSnapshotListener` w czasie rzeczywistym, posortowany malejąco po `updatedAt`; zwraca `ListenerRegistration` umożliwiający odłączenie |
| `create(userId, item, callback)` | zapisuje nowy dokument |
| `update(userId, item, callback)` | nadpisuje istniejący dokument |
| `delete(userId, itemId, callback)` | usuwa dokument |
| `newDocumentId(userId)` | generuje identyfikator nowego dokumentu bez zapisu |

### Reguły bezpieczeństwa Firestore (`firestore.rules`)

Reguły wymuszają własność danych i ich integralność po stronie serwera — klient nie może ich obejść nawet zmodyfikowaną wersją aplikacji.

| Funkcja / reguła | Opis |
|---|---|
| `signedIn()` | wymaga aktywnego tokenu uwierzytelniającego |
| `owns(userId)` | UID z tokenu musi odpowiadać segmentowi `{userId}` w ścieżce |
| `validItem(data, itemId)` | wymusza dokładny zestaw pól (bez dodatkowych), poprawne typy, limity długości pól zgodne z `CatalogValidation` oraz zgodność `data.id` z identyfikatorem dokumentu |
| odczyt / usuwanie | wymagają wyłącznie `owns()` |
| tworzenie / aktualizacja | dodatkowo wymagają spełnienia `validItem()` |

---

## Instrukcja testowania

### Testy jednostkowe (JUnit 4)

```
./gradlew testDebugUnitTest
```

- `CatalogValidationTest` — 10 przypadków: poprawny przedmiot, naruszenia granic długości dla wszystkich pól, poprawny/niepoprawny e-mail, hasło o długości 6 znaków i krótsze
- `SearchMatcherTest` — 5 przypadków: puste zapytanie, dopasowanie bez uwzględniania wielkości liter dla nazwy/opisu/kategorii, brak dopasowania dla niepowiązanego zapytania

### Ręczne testowanie kluczowych funkcji (scenariusz „od początku do końca”)

1. Zarejestruj nowe konto, a następnie wyloguj się i zaloguj ponownie.
2. Dodaj nowy przedmiot — uzupełnij nazwę, kategorię, opis i zrób zdjęcie aparatem.
3. Sprawdź walidację formularza — spróbuj zapisać przedmiot z pustą nazwą, zbyt długim opisem itp. i zweryfikuj komunikaty błędów przy poszczególnych polach.
4. Wyszukaj dodany przedmiot po fragmencie nazwy, kategorii i opisu (sprawdź też brak rozróżniania wielkości liter).
5. Otwórz szczegóły przedmiotu, zmień jego dane (edycja) i potwierdź, że zmiana jest od razu widoczna na liście (aktualizacja w czasie rzeczywistym).
6. Usuń przedmiot — potwierdź działanie okna dialogowego z potwierdzeniem usunięcia.
7. Wyloguj się i zaloguj na inne konto — zweryfikuj, że katalog innego użytkownika jest pusty/odizolowany.

## Wkład własny

*Sekcja do uzupełnienia przez autora/autorów projektu — poniżej znajduje się zarys obszarów funkcjonalnych zaimplementowanych w ramach projektu, które można rozwinąć o opis własnego wkładu:*

- zaprojektowanie modeli danych (`CatalogItem`, `ItemDraft`) oraz struktury przechowywania danych w Firestore - Marcel Snopkowski,
- implementacja warstwy repozytoriów (`AuthRepository`, `ItemRepository`) izolującej resztę aplikacji od zależności na Firebase - Marcel Snopkowski,
- napisanie reguł walidacji (`CatalogValidation`) - Marcel Snopkowski,
- mechanizm wyszukiwania (`SearchMatcher`) - Agata Mróz,
- implementacja warstwy ViewModel (osobny ViewModel dla każdego ekranu) wraz z zarządzaniem stanem przez `StateFlow` - Marcel Snopkowski,
- zaprojektowanie i zaimplementowanie wszystkich ekranów w Jetpack Compose, w tym nawigacji (zagnieżdżony graf `catalog_graph`) - Agata Mróz,
- integracja aparatu (`ActivityResultContracts.TakePicture`, `FileProvider`) oraz potoku przetwarzania zdjęć (`ImageUtils` — przeskalowanie, kompresja, kodowanie Base64) - Agata Mróz,
- wzmocnienie reguł bezpieczeństwa Cloud Firestore oraz napisanie testów jednostkowych i integracyjnych - Marcel Snopkowski,


## 👥 Zespół projektowy

* Marcel Snopkowski (177159)
* Agata Mróz (167821)
* Bartosz Zając (177190)
