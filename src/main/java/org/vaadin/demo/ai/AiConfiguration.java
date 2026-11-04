package org.vaadin.demo.ai;

import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.presenter.PresenterToolCatalog;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * The one piece of draiv-ai wiring the app has to supply: the tools the AI may use.
 * <p>
 * {@code CapabilityMcpTools} gives it {@code inspect}, {@code list_capabilities}, {@code apply}
 * and {@code navigate} on the running UI, the same fields and buttons a human sees.
 * Everything else (chat loop, provider selection) is auto-configured by draiv-ai.
 */
@Configuration
public class AiConfiguration {

    @Bean(name = "chatToolCatalog")
    PresenterToolCatalog chatToolCatalog(CapabilityMcpTools capabilityTools) {
        return new PresenterToolCatalog(MethodToolCallbackProvider.builder()
                .toolObjects(capabilityTools)
                .build());
    }

    /**
     * The LLM that reads receipts for the form filler. It needs a model that can see images.
     * Prototype: each AIOrchestrator claims its provider exclusively, so every form gets its own.
     */
    @Bean
    @Scope("prototype")
    LLMProvider receiptFormProvider(OpenAiChatModel chatModel,
            @Value("${openai.receipt.model}") String model) {
        var client = ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder().model(model))
                .build();
        var provider = new SpringAILLMProvider(client);
        provider.setBackgroundExecution(true); // the call takes seconds: not on the UI thread
        return provider;
    }
}
