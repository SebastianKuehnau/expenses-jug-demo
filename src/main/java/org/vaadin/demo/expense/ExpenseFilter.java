package org.vaadin.demo.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

/** What the user typed into the filter bar. Every field is optional (null = no restriction). */
public record ExpenseFilter(String text, ExpenseCategory category, BigDecimal minAmount,
                            BigDecimal maxAmount, LocalDate from, LocalDate to) {

    public static final ExpenseFilter NONE = new ExpenseFilter(null, null, null, null, null, null);

    /** How many fields restrict the result. Shown as a badge on the filter toggle. */
    public int activeCriteria() {
        int count = text != null && !text.isBlank() ? 1 : 0;
        for (Object value : new Object[] {category, minAmount, maxAmount, from, to}) {
            count += value != null ? 1 : 0;
        }
        return count;
    }
}
