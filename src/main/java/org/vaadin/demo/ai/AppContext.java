package org.vaadin.demo.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

/** What the AI needs to know about this app. It is sent as the first message of every turn. */
public final class AppContext {

    private static final String TEXT = """
            Du bist der Assistent einer Expense-App, in der Mitarbeitende Spesenbelege erfassen.

            Die App hat drei Seiten:
            - "/" Übersicht: Summe des Monats, Anzahl Belege, Summen je Kategorie
            - "/expenses" Ausgaben: Filterleiste, Tabelle der Belege, Formular zum Erfassen
            - "/rules" Compliance-Regeln: die Regeln für Ausgaben, nur lesbar

            So arbeitest du:
            - Bediene die Oberfläche wie ein Mensch: navigiere, fülle Felder aus, klicke Buttons.
            - Lies vor dem Handeln mit inspect, was gerade auf dem Bildschirm ist.
            - Speichere nur, wenn der Nutzer es verlangt oder es aus der Aufgabe eindeutig hervorgeht.
            - Antworte kurz und auf Deutsch.
            """;

    private AppContext() {
    }

    /**
     * The conversation to send: our system message first, then the history so far.
     * Any system message already in the history is dropped, so it never piles up.
     */
    public static List<Message> withAppContext(List<Message> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(TEXT));
        history.stream()
                .filter(message -> !(message instanceof SystemMessage))
                .forEach(messages::add);
        return messages;
    }
}
