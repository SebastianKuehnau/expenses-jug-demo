# Expense Demo — Arbeitsnotizen für Claude

Demo-App für einen 60-Minuten-Vortrag (JUG Vienna). **Die Spezifikation steht in `SPEC.md` —
vor jeder Aufgabe lesen und genau eine Phase umsetzen.**

- Stack: Java 25, Spring Boot 4.1, Vaadin 25.3 (Flow, Java-Views), Spring AI 2.0 (OpenAI),
  `com.vaadin:draiv-ai:0.0.1-alpha1-SNAPSHOT` aus `~/.m2` (lokal installiert aus
  `../draiv-experiment`).
- Referenzcode (nur lesen, nicht kopieren ohne Vereinfachung):
  - Chat-Client: `../draiv-experiment/parempi/src/main/java/com/porotype/parempi/ai/ui/ActivityOverlay.java`
  - AI-Verdrahtung + `AiGoalBar`: `../draiv-experiment/vaadin-grid-search-ui/` (README + `ai/AiSearchConfiguration.java`)
  - Browserless-Tests: `../draiv-experiment/vaadin-grid-search-ui/src/test/`
- Code wird im Vortrag gezeigt und von Vaadin-Neulingen gelesen: Leitlinien in `SPEC.md` §0
  sind verbindlich (kurze Methoden, nur offizielle API, kein `executeJs`, keine Abstraktionen
  auf Vorrat).
- Vaadin-API nicht raten: Vaadin-MCP (`get_vaadin_primer`, `get_java_symbol`,
  `search_vaadin_docs`, Version 25.3) und Skills `vaadin-browserless-testing`,
  `vaadin-form-layout`, `vaadin-usage-review` benutzen.
- Starten: `OPENAI_API_KEY=… ./mvnw spring-boot:run` → http://localhost:8080
- Eine Phase ist fertig, wenn `./mvnw test` grün ist, die Demo-Prompts der Phase im laufenden
  Browser funktionieren, `vaadin-usage-review` keine Befunde hat, committed und mit `phase-N`
  getaggt ist.
