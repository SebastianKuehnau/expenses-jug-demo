package org.vaadin.demo.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.vaadin.demo.expense.ExpenseCategory;

/** What the AI read from a receipt. Any field may be null if it is not on the receipt. */
public record ReceiptData(LocalDate date, String merchant, BigDecimal amount, String currency,
                          ExpenseCategory category, String description) {
}
