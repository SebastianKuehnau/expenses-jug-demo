package org.vaadin.demo.expense;

public enum ExpenseCategory {
    TRAVEL("Reise"),
    MEALS("Bewirtung"),
    ACCOMMODATION("Unterkunft"),
    OFFICE("Büro"),
    SOFTWARE("Software"),
    TRAINING("Weiterbildung"),
    OTHER("Sonstiges");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
