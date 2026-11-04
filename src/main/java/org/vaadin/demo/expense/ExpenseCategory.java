package org.vaadin.demo.expense;

public enum ExpenseCategory {
    TRAVEL("Travel"),
    MEALS("Meals"),
    ACCOMMODATION("Accommodation"),
    OFFICE("Office"),
    SOFTWARE("Software"),
    TRAINING("Training"),
    OTHER("Other");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
