package org.vaadin.demo.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Amount formatting for the UI: €1,234.50 */
final class Formats {

    private static final Locale IRELAND = Locale.of("en", "IE");

    private Formats() {
    }

    static String money(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(IRELAND).format(amount);
    }
}
