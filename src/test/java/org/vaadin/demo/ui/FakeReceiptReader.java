package org.vaadin.demo.ui;

import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;
import com.vaadin.flow.component.ai.provider.LLMProvider.ToolSpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Plays the LLM of the form filler: it reads the form like the real model would ({@code get_form_state}),
 * then writes values by field label and a source for each ({@code fill_form}).
 */
final class FakeReceiptReader {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FakeReceiptReader() {
    }

    static Flux<String> fill(LLMRequest request, Map<String, Object> valuesByLabel) {
        ToolSpec getFormState = tool(request, "get_form_state");
        ToolSpec fillForm = tool(request, "fill_form");
        JsonNode state = JSON.readTree(getFormState.execute(JSON.readTree("{}")));
        Map<String, Object> values = new HashMap<>();
        Map<String, Object> sources = new HashMap<>();
        for (JsonNode field : state.get("fields")) {
            // The controller describes a bound field as "Label | property".
            String label = field.get("description").asString().split(" \\| ")[0];
            if (valuesByLabel.containsKey(label)) {
                String id = field.get("id").asString();
                values.put(id, valuesByLabel.get(label));
                sources.put(id, Map.of("confidence", "high",
                        "extracts", List.of(Map.of(
                                "text", String.valueOf(valuesByLabel.get(label)),
                                "location", Map.of("type", "page-region", "page", 1,
                                        "rect", List.of(0.1, 0.2, 0.3, 0.05))))));
            }
        }
        fillForm.execute(JSON.valueToTree(Map.of("values", values, "sources", sources)));
        return Flux.just("Filled in the form.");
    }

    private static ToolSpec tool(LLMRequest request, String name) {
        return request.explicitTools().stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("The form filler offers no tool " + name));
    }
}
