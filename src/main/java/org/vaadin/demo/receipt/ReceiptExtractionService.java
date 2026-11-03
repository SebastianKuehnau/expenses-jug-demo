package org.vaadin.demo.receipt;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

/** Reads a receipt (photo, scan or PDF) with a vision-capable model. */
@Service
public class ReceiptExtractionService {

    private static final String INSTRUCTIONS = """
            Lies diesen Beleg und gib seine Daten zurück.
            - date: Rechnungs- oder Belegdatum
            - merchant: Name des Händlers oder Lieferanten
            - amount: Gesamtbetrag (brutto), nur die Zahl
            - currency: ISO-Code, z. B. EUR
            - category: genau eine von TRAVEL, MEALS, ACCOMMODATION, OFFICE, SOFTWARE, TRAINING, OTHER
            - description: ein kurzer deutscher Satz, was gekauft wurde. Erfinde keinen \
            Geschäftszweck, der nicht auf dem Beleg steht.
            Felder, die auf dem Beleg nicht erkennbar sind, bleiben null.
            """;

    private final ChatClient chatClient;
    private final TextPresenterProperties aiProperties;
    private final String model;

    public ReceiptExtractionService(OpenAiChatModel chatModel, TextPresenterProperties aiProperties,
            @Value("${openai.receipt.model}") String model) {
        this.chatClient = ChatClient.create(chatModel);
        this.aiProperties = aiProperties;
        this.model = model;
    }

    public boolean isAvailable() {
        return aiProperties.isEnabled();
    }

    /** Blocks for a few seconds: call it from a background thread, never from the UI thread. */
    public ReceiptData extract(byte[] data, String mimeType) {
        return chatClient.prompt()
                .options(OpenAiChatOptions.builder().model(model))
                .user(user -> user.text(INSTRUCTIONS)
                        .media(MimeType.valueOf(mimeType), new ByteArrayResource(data)))
                .call()
                .entity(ReceiptData.class);
    }
}
