package org.vaadin.demo.compliance;

/** The AI's verdict. {@code feedback} is at most two German sentences naming the violated rule. */
public record ComplianceResult(boolean compliant, String feedback) {
}
