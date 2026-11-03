package org.vaadin.demo.compliance;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.demo.expense.Expense;

/** Asks the AI whether an expense follows the company rules. */
@Service
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private static final String SYSTEM_PROMPT = """
            Du prüfst Spesenbelege auf die Einhaltung dieser Regeln:

            %s

            Melde nur klare Verstöße. Wenn ein Beleg konform ist, ist compliant true und \
            feedback leer. Sonst ist compliant false und feedback höchstens zwei kurze Sätze \
            auf Deutsch: nenne die verletzte Regel mit Nummer und sage, was in der Beschreibung \
            fehlt oder geändert werden muss.
            """;

    private final ChatClient chatClient;
    private final ComplianceRules rules;
    private final TextPresenterProperties aiProperties;
    private final String model;

    public ComplianceService(OpenAiChatModel chatModel, ComplianceRules rules,
            TextPresenterProperties aiProperties,
            @Value("${openai.compliance.model}") String model) {
        this.chatClient = ChatClient.create(chatModel);
        this.rules = rules;
        this.aiProperties = aiProperties;
        this.model = model;
    }

    /** Blocks for a few seconds: call it from a background thread, never from the UI thread. */
    public ComplianceResult check(Expense expense) {
        if (!aiProperties.isEnabled()) {
            log.info("No OPENAI_API_KEY configured: compliance check skipped");
            return new ComplianceResult(true, null);
        }
        return chatClient.prompt()
                .options(OpenAiChatOptions.builder().model(model))
                .system(SYSTEM_PROMPT.formatted(rules.text()))
                .user(describe(expense))
                .call()
                .entity(ComplianceResult.class);
    }

    private String describe(Expense expense) {
        return """
                Datum: %s
                Händler: %s
                Kategorie: %s
                Betrag: %s %s
                Beschreibung: %s
                """.formatted(expense.getDate(), expense.getMerchant(),
                expense.getCategory().getLabel(), expense.getAmount(), expense.getCurrency(),
                expense.getDescription());
    }
}
