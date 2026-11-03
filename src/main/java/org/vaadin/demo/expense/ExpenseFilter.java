package org.vaadin.demo.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

/** What the user typed into the filter bar. Every field is optional (null = no restriction). */
public record ExpenseFilter(String text, ExpenseCategory category, BigDecimal minAmount,
                            BigDecimal maxAmount, LocalDate from, LocalDate to) {

    public static final ExpenseFilter NONE = new ExpenseFilter(null, null, null, null, null, null);
}
