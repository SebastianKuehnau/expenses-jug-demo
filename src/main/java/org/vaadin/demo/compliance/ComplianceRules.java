package org.vaadin.demo.compliance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** The company's expense rules. They live in {@code compliance-rules.md}, shown on /rules and sent to the AI. */
@Component
public class ComplianceRules {

    private final String text;

    public ComplianceRules() {
        try {
            this.text = new ClassPathResource("compliance-rules.md").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String text() {
        return text;
    }
}
