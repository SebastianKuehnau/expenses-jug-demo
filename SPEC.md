# Expense Demo — Spezifikation (JUG Vienna, 60 min)

Demo-App für einen Vortrag: zuerst Vaadin in 15 Minuten (Grid, Formular, Browserless-Test),
dann „die AI bedient dieselbe UI wie der Mensch“ (Chat, Filter, Compliance, Belegscan).

Umsetzung mit Claude Code in **Phasen**. Jede Phase endet mit grünen Tests, einem Commit und
einem Git-Tag (`phase-1` … `phase-5`), damit man im Vortrag zwischen Ständen springen kann.

---

## 0. Leitlinien (gelten für jede Phase)

**Zielgruppe des Codes:** Java-Entwickler, die Vaadin noch nie gesehen haben. Der Code wird auf
dem Beamer gezeigt.

- Eine View = eine Klasse, von oben nach unten lesbar: Felder → Konstruktor → kleine,
  sprechend benannte private Methoden (`createGrid()`, `createForm()`, `save()`).
- Methoden höchstens ~25 Zeilen, Klassen möglichst unter ~200 Zeilen. Lieber eine Klasse mehr.
- Nur offizielle Vaadin-API. **Kein** `executeJs`, kein `getElement()`-Gefrickel, kein Inline-CSS
  über `getStyle()` außer wenn es nicht anders geht (dann mit Kommentar). Layout über
  `VerticalLayout`/`HorizontalLayout`/`FormLayout`, Optik über Theme-Varianten.
- Kurze Javadoc-Kommentare dort, wo ein Vaadin-Konzept zum ersten Mal auftaucht (Binder,
  DataProvider, Push, `UI.access`) — ein Satz, der *warum* erklärt.
- Keine Abstraktionsschichten „für später“. Kein generisches CRUD-Framework, keine Basisklassen.
- Java-Records für DTOs, Spring Data JPA für Persistenz, H2 in-memory mit Seed-Daten.
- Code-Bezeichner Englisch, **UI-Texte Deutsch**.
- Vor dem Schreiben von Vaadin-Code: Vaadin-MCP (`get_vaadin_primer`, `get_java_symbol`) bzw.
  die Vaadin-Skills nutzen, nicht aus dem Gedächtnis raten.

**Stack:** Java 25, Spring Boot 4.1, Vaadin 25.3 (Flow), Spring AI 2.0 (OpenAI),
`com.vaadin:draiv-ai:0.0.1-alpha1-SNAPSHOT` (lokal installiert, siehe README),
`browserless-test-spring` für Tests.

---

## 1. Domäne

```java
@Entity Expense {
    Long id;
    LocalDate date;            // Pflicht, nicht in der Zukunft
    String merchant;           // Pflicht, Händler/Lieferant
    String description;        // Pflicht, max. 500 Zeichen
    ExpenseCategory category;  // Pflicht
    BigDecimal amount;         // Pflicht, > 0
    String currency;           // Default "EUR"
    boolean complianceOverridden; // true = Warnung ignoriert („Trotzdem speichern“)
    String complianceNote;        // AI-Rückmeldung, falls ignoriert
}
enum ExpenseCategory { TRAVEL, MEALS, ACCOMMODATION, OFFICE, SOFTWARE, TRAINING, OTHER }
```

- `ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense>`
- `ExpenseService` mit `findAll(ExpenseFilter)`, `save(Expense)`, `delete(Expense)`.
- `record ExpenseFilter(String text, ExpenseCategory category, BigDecimal minAmount,
  BigDecimal maxAmount, LocalDate from, LocalDate to)` → JPA-Specification im Service.
- Seed: ~40 realistische Belege (Wien/Graz/München, Bahn, Hotel, Bewirtung, Software-Abos),
  über die letzten 3 Monate verteilt, inkl. 2–3 die gegen die Compliance-Regeln verstoßen.

---

## 2. Phase 1 — Vaadin-Grundlagen (Vortragsteil „Vaadin Intro“)

### 2.1 App-Shell
- `Application` mit `@Push` und `AppShellConfigurator` (Push wird später für die AI gebraucht —
  im Code einen Satz dazu).
- `MainLayout extends AppLayout`: Titel „Expenses“, `SideNav` mit:
  - **Übersicht** (`/`) — Summe des laufenden Monats, Anzahl Belege, Summe je Kategorie
    (einfache `Card`s, kein Chart nötig).
  - **Ausgaben** (`/expenses`) — Hauptview.
  - **Compliance-Regeln** (`/rules`) — zeigt die Regeln aus Abschnitt 4 read-only an.
  - Mehrere Views sind Absicht: daran zeigt man später „finde die View per Chat“.

### 2.2 `ExpensesView` (`/expenses`)
Aufbau von oben nach unten:
1. **Filterleiste** (`HorizontalLayout` bzw. kompaktes `FormLayout`): Textsuche (Händler +
   Beschreibung), Kategorie (`ComboBox`), Betrag von/bis (`BigDecimalField`), Datum von/bis
   (`DatePicker`), Buttons „Suchen“ und „Zurücksetzen“. „Suchen“ setzt den `ExpenseFilter`
   und lädt das Grid neu.
   *Wichtig für Phase 3:* Die AI bedient genau diese Felder. Jedes Feld braucht ein klares
   deutsches Label.
2. **Grid** `Grid<Expense>`: Spalten Datum, Händler, Kategorie, Beschreibung (gekürzt),
   Betrag (rechtsbündig, formatiert `€ 1.234,50`), Spalte mit gelbem Warn-Icon, wenn
   `complianceOverridden`. Sortierbar, Lazy Loading über `setItems(query -> …)` mit
   Spring-Data-`PageRequest` (das ist eine Folie wert: „kein REST, kein DTO-Mapping“).
3. **Button „Neue Ausgabe“** über dem Grid.
4. **Formular** rechts neben dem Grid (`SplitLayout`), sichtbar bei Auswahl oder „Neu“:
   `FormLayout` mit Datum, Händler, Kategorie, Betrag, Währung, Beschreibung (`TextArea`),
   Buttons **Speichern**, **Löschen** (mit `ConfirmDialog`), **Abbrechen**.
   - `BeanValidationBinder<Expense>` + Bean-Validation-Annotationen auf der Entity.
   - Nach Speichern/Löschen: Grid aktualisieren, `Notification` („Ausgabe gespeichert“).

### 2.3 Tests (`src/test`, Browserless — kein Browser, kein Server)
Mit `SpringBrowserlessTest` (Skill `vaadin-browserless-testing` verwenden). Je Test ein klarer
Use Case, als Geschichte lesbar:
- `grid_shows_seeded_expenses()`
- `create_expense_via_form()` — „Neu“, Felder füllen, Speichern → Zeile im Grid.
- `edit_expense()` — Zeile wählen, Betrag ändern, Speichern → Grid zeigt neuen Betrag.
- `delete_expense()` — mit Bestätigung.
- `invalid_input_is_rejected()` — Betrag 0 → Fehlermeldung, nichts gespeichert.
- `filter_by_category_and_amount()`.

**Fertig, wenn:** App startet, CRUD und Filter funktionieren, `./mvnw test` ist grün. Tag `phase-1`.

---

## 3. Phase 2 — Chat-Client (wie Parempi, ohne Sprache)

Vorbild: `draiv-experiment/parempi/src/main/java/com/porotype/parempi/ai/ui/ActivityOverlay.java`
(nur den Chat-Teil übernehmen, stark vereinfacht). Backend: `TextPresenterService` aus `draiv-ai`.

### 3.1 Verdrahtung
- `application.properties`:
  ```properties
  spring.ai.openai.api-key=${OPENAI_API_KEY:not-configured}
  openai.text.model=${OPENAI_TEXT_MODEL:gpt-5}
  openai.text.reasoning-effort=minimal
  draiv.session.demo-identity=true
  ```
- `AiConfiguration` mit genau einer Bean `chatToolCatalog` (Vorlage:
  `vaadin-grid-search-ui/.../ai/AiSearchConfiguration.java`):
  ```java
  @Bean(name = "chatToolCatalog")
  PresenterToolCatalog chatToolCatalog(CapabilityMcpTools capabilityTools) {
      return new PresenterToolCatalog(MethodToolCallbackProvider.builder()
              .toolObjects(capabilityTools).build());
  }
  ```
  Damit bekommt die AI `inspect` / `list_capabilities` / `apply` / `navigate` auf die
  laufende UI. Ohne API-Key startet die App trotzdem; der Chat-Button ist dann deaktiviert
  mit Tooltip „Kein OPENAI_API_KEY gesetzt“.

### 3.2 UI-Verhalten (wie Parempi)
- Button **„Chat“** (Icon `COMMENT_ELLIPSIS`) rechts in der Navbar des `MainLayout`.
  Klick öffnet/schließt das Chat-Fenster.
- Chat-Fenster = `Dialog`, **nicht modal**, verschiebbar, größenveränderbar, 420×560 px,
  startet unten rechts. Header „Assistent“ mit Buttons „Verlauf löschen“ (`ERASER`) und
  „Schließen“ (`CLOSE`). Bleibt beim Navigieren zwischen Views offen und behält den Verlauf
  (der Dialog gehört zum `MainLayout`, nicht zur View).
- Inhalt von oben nach unten:
  1. **Nachrichtenverlauf** (scrollbar): Sprechblasen für Nutzer (rechts), Assistent (links),
     Fehler (rot). Begrüßung: „Hallo! Sag mir, was ich in der Expense-App für dich tun soll.“
     Antworten des Assistenten als Markdown rendern (Vaadin `Markdown`-Komponente).
     Nutzerblasen zeigen angehängte Dateinamen als Chips.
  2. **Upload** (Vaadin 25 `UploadHandler.inMemory`): Drag & Drop, `image/*` und
     `application/pdf`, max. 5 Dateien, max. 20 MB. Dateien werden als
     `TextPresenterService.Attachment(filename, mimeType, base64)` gesammelt und mit der
     nächsten Nachricht geschickt; Entfernen eines Chips entfernt auch das Attachment.
  3. **`MessageInput`**.
- **Ein Turn:**
  1. Nutzerblase anzeigen, Blase „Denke nach…“ einfügen, Eingabe + Upload deaktivieren,
     Button **„Stopp“** einblenden.
  2. Auf einem Executor-Thread: `presenter.respond(principal, history, text, attachments,
     event -> …, null, null, cancelSignal)`.
  3. `TurnEvent`s live in die „Denke nach…“-Blase schreiben (`UI.access`), übersetzt in
     kurze deutsche Statuszeilen: `navigate` → „Öffne …“, `inspect` → „Schaue auf den
     Bildschirm…“, `apply` → „Fülle aus / klicke …“. (Vorbild: Parempi `TurnProgress`.)
  4. Ergebnis: Statusblase entfernen, Antwort anzeigen, `history` durch
     `result.conversationItems()` ersetzen, Eingabe wieder aktivieren.
  5. Fehler → rote Blase mit Meldung. „Stopp“ → `CancelSignal` setzen.
- Die Änderungen der AI sind im Formular/Grid **sichtbar**, weil sie über Push in die UI
  kommen. Kommentar im Code: *„Die AI läuft im Hintergrund-Thread; `UI.access` + `@Push`
  bringen ihre Änderungen sofort in den Browser.“*

### 3.3 Bewusst NICHT übernehmen (gegenüber Parempi)
Sprache/Talk-Toggle, Realtime, Walkthroughs, Evidence/Provenance, Hintergrund-UIs,
`run_code`/Skripte, Makros, Modell-Auswahl-Dialog, Browser-Benachrichtigungen, alle
`executeJs`-Tricks (Enter-Blockade → stattdessen Eingabe einfach deaktivieren).

### 3.4 Systemprompt
Kurzer App-Kontext als erste Nachricht im Verlauf (eigene Klasse `AppContext`, als Text-Block
lesbar): was die App ist, welche Views es gibt (`/`, `/expenses`, `/rules`), „bediene die UI wie
ein Mensch, lies vor dem Handeln mit `inspect`, speichere nur, wenn der Nutzer es verlangt
oder es aus der Aufgabe eindeutig hervorgeht“, antworte kurz und auf Deutsch.

**Demo-Prompts, die funktionieren müssen:**
- „Wo sehe ich die Compliance-Regeln?“ → navigiert nach `/rules`.
- „Zeig mir alle Ausgaben“ → navigiert nach `/expenses`.

**Fertig, wenn:** beide Prompts klappen, Tests weiterhin grün. Tag `phase-2`.

---

## 4. Phase 3 — Filtern mit natürlicher Sprache

- In `ExpensesView` über der Filterleiste: `AiGoalBar` aus `draiv-ai` (Vorlage:
  `vaadin-grid-search-ui/README.md`, Schritt 5), Platzhalter
  „z. B. Bewirtung über 100 € im Oktober“.
- Goal-Template als Konstante in der View (Text-Block): setze nur die Filterfelder, die die
  Anfrage impliziert, leere die anderen, klicke dann „Suchen“; relative Daten bezogen auf heute.
- **Keine** generierte JPA-Query. Die AI füllt das sichtbare Formular, der Nutzer kann
  korrigieren. (Folienbotschaft: kein Injection-Risiko, Security/Validierung greifen wie immer.)
- Dasselbe geht auch im Chat („Filtere nach Hotels im September“).

**Demo-Prompts:** „Bewirtungen über 100 € im letzten Monat“, „alles von der ÖBB“,
„Software-Ausgaben dieses Quartal, teuerste zuerst“ (Sortierung optional).

Test (ohne AI): Filterleiste programmatisch füllen wie die AI es täte → Grid gefiltert.
Tag `phase-3`.

---

## 5. Phase 4 — Compliance-Prüfung mit AI (neue Funktion)

### 5.1 Regeln
`ComplianceRules` (Spring-`@ConfigurationProperties` oder `src/main/resources/compliance-rules.md`),
angezeigt in `/rules`. Startinhalt:
1. Keine Ausgaben für Alkohol, außer bei Bewirtung von Kunden.
2. Bewirtung (MEALS) über 50 € braucht Anlass und Teilnehmer in der Beschreibung.
3. Hotel max. 180 € pro Nacht (Wien 200 €).
4. Flüge unter 600 km nur mit Begründung; Bahn ist Standard.
5. Software-Abos nur mit Angabe des Projekts.
6. Keine privaten Ausgaben (Geschenke an sich selbst, Freizeit, Familie).
7. Beschreibung muss den geschäftlichen Zweck erkennen lassen („diverses“ reicht nicht).

### 5.2 Service
```java
record ComplianceResult(boolean compliant, String feedback) {}  // feedback ≤ 2 Sätze, Deutsch

@Service class ComplianceService {
    ComplianceResult check(Expense expense);   // Spring AI ChatClient, .entity(ComplianceResult.class)
}
```
Prompt: Regeln + Beleg (Datum, Händler, Kategorie, Betrag, Beschreibung). Antwort knapp,
nennt die verletzte Regel mit Nummer, sagt, was fehlt. Ist kein Key konfiguriert →
`compliant=true` (Prüfung übersprungen, Log-Hinweis).

### 5.3 Ablauf beim Speichern
1. Binder-Validierung (Pflichtfelder) wie bisher. Fehler → abbrechen.
2. Speichern-Button deaktivieren, Text „Prüfe…“; `ComplianceService.check` asynchron
   (`CompletableFuture` + `UI.access`), damit die UI nicht einfriert.
3. **Konform** → speichern, Notification, Warn-Icon ausblenden.
4. **Nicht konform** → **nicht** speichern. Neben dem Feld *Beschreibung* erscheint ein
   **gelbes Warn-Icon** (`VaadinIcon.WARNING`, Farbe über Lumo-/Aura-Warning-Token,
   Tooltip „Compliance-Hinweis — klicken für Details“).
5. **Klick auf das Icon** → `Popover` am Icon mit:
   - Überschrift „Compliance-Hinweis“
   - der kurzen AI-Rückmeldung
   - Buttons **„Schließen“** und **„Trotzdem speichern“** (Variante `LUMO_ERROR`/tertiär).
6. **Schließen** → Popover zu, Nutzer ändert die Eingabe; erneutes **Speichern** prüft neu.
   Sobald die Beschreibung geändert wird, verschwindet das Icon (die alte Rückmeldung gilt
   nicht mehr).
7. **Trotzdem speichern** → ohne erneute Prüfung speichern mit
   `complianceOverridden = true`, `complianceNote = feedback`. Im Grid erscheint das gelbe
   Icon in der Zeile (Tooltip = Note).

Komponente kapseln: `ComplianceWarning extends Composite<Icon>` (Icon + Popover + zwei
Callbacks) — klein, auf einer Folie zeigbar.

**Demo-Eingaben:**
- „Abendessen“ / MEALS / 180 € → Hinweis Regel 2.
- „Abendessen mit Kunde ACME (M. Huber, J. Novak), Projekt-Kickoff“ → konform.
- „Flasche Wein für zuhause“ → Hinweis Regel 1/6 → „Trotzdem speichern“ zeigen.

**Tests:** `ComplianceService` im Test durch Fake ersetzen (`@MockitoBean` / `@TestConfiguration`);
testen: Warn-Icon erscheint & nichts gespeichert; Popover → „Trotzdem speichern“ speichert mit
Flag; Beschreibung ändern blendet Icon aus. Tag `phase-4`.

---

## 6. Phase 5 — Beleg scannen

### 6.1 Einzelner Beleg im Formular
- Im Formular oben ein `Upload` „Beleg scannen“ (Bild/PDF, 1 Datei).
- `ReceiptExtractionService.extract(byte[] data, String mimeType) → ReceiptData`
  (Spring AI `ChatClient`, `UserMessage` mit `Media`, `.entity(ReceiptData.class)`).
  ```java
  record ReceiptData(LocalDate date, String merchant, BigDecimal amount, String currency,
                     ExpenseCategory category, String description) {}
  ```
- Ergebnis füllt die Formularfelder (über den Binder, **nicht** speichern). Ausgefüllte Felder
  kurz hervorheben ist nett, aber optional. Der Nutzer prüft und klickt „Speichern“ → die
  Compliance-Prüfung aus Phase 4 greift automatisch.
- Während der Extraktion: Upload deaktiviert, `ProgressBar` indeterminate.

### 6.2 Mehrere Belege per Chat
Keine neue Logik: Belege im Chat-Fenster anhängen, z. B.
„Erfasse diese drei Belege als Ausgaben.“ Die AI liest die Bilder (Attachments aus Phase 2),
navigiert nach `/expenses` und füllt für jeden Beleg das Formular und klickt „Speichern“ —
inklusive Compliance-Prüfung; bei einer Warnung meldet sie das im Chat statt sie zu ignorieren
(im Systemprompt festhalten: „Trotzdem speichern“ nie ohne ausdrückliche Zustimmung).

**Demo-Material:** `demo-receipts/` mit 4–5 realistischen, selbst erstellten Belegen als PNG
(ÖBB-Ticket, Hotelrechnung Graz, Restaurant mit Wein, Software-Abo), einer davon absichtlich
compliance-kritisch. (Generierbar als HTML → Screenshot; keine echten Firmenlogos.)

Tag `phase-5`.

---

## 7. Demo-Ablauf (Abgleich mit dem Vortrag)

| Min | Teil | Stand |
|---|---|---|
| 0–5 | Einstieg | — |
| 5–20 | Grid, Formular, Browserless-Test | `phase-1` (Code zeigen, 1 Feld live ergänzen, Test laufen lassen) |
| 20–25 | Chat: View finden | `phase-2` |
| 25–32 | Filter in natürlicher Sprache | `phase-3` |
| 32–40 | Compliance-Warnung | `phase-4` |
| 40–50 | Beleg scannen, dann 3 Belege per Chat | `phase-5` |
| 50–60 | Fazit, Q&A | — |

Fallback: Für jede AI-Demo ein kurzes Bildschirmvideo aufnehmen.
