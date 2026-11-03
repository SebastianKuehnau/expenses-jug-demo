package org.vaadin.demo.ai;

import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.presenter.PresenterToolCatalog;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
