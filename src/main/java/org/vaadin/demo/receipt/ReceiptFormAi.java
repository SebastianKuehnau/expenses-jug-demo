package org.vaadin.demo.receipt;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/** Connects Vaadin's {@link FormAIController} to the LLM that reads receipts. */
@Service
public class ReceiptFormAi {

    private static final String INSTRUCTIONS = """
            You fill in an expense form from the attached receipt (an image or a PDF).
            - Date: the invoice or receipt date.
            - Merchant: the name of the merchant or supplier.
            - Category: the best fit from the options of the field.
            - Amount: the total (gross), number only. Currency: the ISO code, e.g. EUR.
            - Description: one short English sentence about what was bought. Do not invent a \
            business purpose that is not on the receipt.
            Leave fields empty that cannot be read from the receipt.
            For every value you read from the receipt, report the source: your confidence (high, \
            medium or low) and the snippet you read, copied verbatim, with its location on the image \
            (page 1) as a rectangle in fractions of the image width and height, measured from the \
            top-left corner.
            """;

    private final ObjectProvider<LLMProvider> providers;
    private final TextPresenterProperties aiProperties;

    public ReceiptFormAi(ObjectProvider<LLMProvider> receiptFormProvider, TextPresenterProperties aiProperties) {
        this.providers = receiptFormProvider;
        this.aiProperties = aiProperties;
    }

    public boolean isAvailable() {
        return aiProperties.isEnabled();
    }

    /** One orchestrator per form. {@code onDone} is called when a fill has finished or failed. */
    public AIOrchestrator createOrchestrator(FormAIController controller, ResponseListener onDone) {
        return AIOrchestrator.builder(providers.getObject(), INSTRUCTIONS)
                .withController(controller)
                .withResponseListener(onDone)
                .build();
    }
}
