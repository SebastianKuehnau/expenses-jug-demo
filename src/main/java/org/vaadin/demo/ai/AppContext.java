package org.vaadin.demo.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

/** What the AI needs to know about this app. It is sent as the first message of every turn. */
public final class AppContext {

    private static final String TEXT = """
            You are the assistant of an expense app in which employees record expense receipts.

            The app has three pages:
            - "/" Overview: total of the month, number of receipts, totals per category
            - "/expenses" Expenses: filter bar, table of receipts, form to record a new one
            - "/rules" Compliance rules: the rules for expenses, read-only

            Filtering on "/expenses": set only the necessary fields of the filter bar ("Search", \
            "Category filter", "Amount from", "Amount to", "Date from", "Date to"), clear the \
            others and click "Search". Relative dates refer to today.

            Recording receipts: if the user attaches receipts (images or PDFs), read them yourself. \
            For each receipt: navigate to "/expenses", click "New expense", fill in the form \
            (Date, Merchant, Category, Amount, Currency, Description) and click "Save". Do not use \
            the "Scan receipt" upload in the form. On save the app checks the compliance rules. \
            If a warning icon appears next to the description, the receipt was not saved: tell the \
            user the feedback in the chat and ask how to proceed. Never click "Save anyway" \
            without the user's explicit consent.

            How you work:
            - Operate the UI like a human: navigate, fill in fields, click buttons.
            - Use inspect to see what is on the screen before you act.
            - Save only if the user asks for it or the task clearly implies it.
            - Answer briefly and in English.
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
