package org.vaadin.demo.expense;

import static org.vaadin.demo.expense.ExpenseCategory.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Demo data: ~40 receipts over the last 3 months, a few of them with ignored compliance warnings. */
@Configuration
class SeedData {

    @Bean
    ApplicationRunner seedExpenses(ExpenseRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(createExpenses());
            }
        };
    }

    private List<Expense> createExpenses() {
        var list = new ArrayList<Expense>();
        // daysAgo, merchant, category, amount, description
        Object[][] rows = {
            {2, "ÖBB", TRAVEL, "78.60", "Railjet Wien – Graz, Kundentermin Stadtwerke"},
            {3, "Hotel Daniel Graz", ACCOMMODATION, "139.00", "Übernachtung Kundentermin Stadtwerke Graz"},
            {5, "Gasthaus Zum Schwarzen Adler", MEALS, "42.80", "Mittagessen mit Kunde M. Huber, Projektabstimmung"},
            {6, "JetBrains", SOFTWARE, "289.00", "IntelliJ IDEA Ultimate Jahreslizenz, Projekt Expenses"},
            {8, "Deutsche Bahn", TRAVEL, "119.90", "ICE München – Wien, Konferenz JavaLand"},
            {9, "Hotel Bayerischer Hof München", ACCOMMODATION, "165.00", "Übernachtung Konferenz München"},
            {10, "Amazon Business", OFFICE, "34.99", "USB-C Dockingstation für Büro"},
            {12, "Café Central", MEALS, "27.50", "Frühstück mit Kollegin J. Novak, Sprint-Planung"},
            {13, "GitHub", SOFTWARE, "21.00", "GitHub Team Abo, Projekt Expenses"},
            {15, "Wiener Linien", TRAVEL, "38.20", "Wochenkarte Wien, Kundentermine"},
            {16, "Udemy", TRAINING, "49.99", "Kurs Spring Security, Weiterbildung Backend-Team"},
            {18, "Restaurant Steirereck", MEALS, "186.00", "Abendessen"},
            {19, "Libro", OFFICE, "18.40", "Notizbücher und Stifte für Workshop"},
            {21, "ÖBB", TRAVEL, "54.30", "Railjet Wien – Salzburg, Workshop Kunde Alpenbau"},
            {22, "Hotel Stein Salzburg", ACCOMMODATION, "212.00", "Übernachtung Workshop Salzburg"},
            {24, "Weinhandlung Kolb", OTHER, "48.00", "Flasche Wein für zuhause"},
            {25, "Atlassian", SOFTWARE, "63.00", "Jira Cloud Abo, Projekt Expenses"},
            {27, "Eurowings", TRAVEL, "189.00", "Flug Wien – Berlin, Kundentermin Berlin"},
            {29, "Hotel Adlon Berlin", ACCOMMODATION, "178.00", "Übernachtung Kundentermin Berlin"},
            {30, "Berliner Verkehrsbetriebe", TRAVEL, "9.80", "Tageskarte Berlin, Kundentermin"},
            {33, "Vapiano", MEALS, "31.20", "Mittagessen mit Kunde ACME (T. Berger), Angebotsbesprechung"},
            {35, "IKEA", OFFICE, "129.00", "Schreibtischlampen und Ablage Büro Wien"},
            {36, "Pluralsight", TRAINING, "29.00", "Monatsabo Pluralsight, Weiterbildung Java 25"},
            {38, "ÖBB", TRAVEL, "78.60", "Railjet Graz – Wien, Rückfahrt Kundentermin"},
            {40, "Figma", SOFTWARE, "15.00", "Figma Professional, Projekt Expenses"},
            {42, "Brauhaus Salzburg", MEALS, "96.40", "Abendessen mit Kunde Alpenbau (H. Moser, K. Leitner), Workshop-Abschluss"},
            {44, "Staples", OFFICE, "76.50", "Monitorständer und Kabel"},
            {46, "Bolt", TRAVEL, "22.40", "Taxi Flughafen Wien, Dienstreise"},
            {48, "Hotel Sacher Wien", ACCOMMODATION, "245.00", "Übernachtung diverses"},
            {50, "Konferenz W-JAX", TRAINING, "690.00", "Konferenzticket W-JAX München, Weiterbildung"},
            {52, "Spar", OTHER, "23.60", "Geschenk für Familie"},
            {55, "ÖBB", TRAVEL, "49.90", "Railjet Wien – Linz, Kundentermin"},
            {58, "Slack", SOFTWARE, "42.00", "Slack Pro Abo, Projekt Expenses"},
            {60, "Gasthaus Pöschl", MEALS, "38.90", "Mittagessen mit Kollegin J. Novak, Retro"},
            {63, "Post", OFFICE, "12.90", "Versand Unterlagen an Kunde"},
            {66, "Hotel Wiesler Graz", ACCOMMODATION, "158.00", "Übernachtung Kundentermin Graz"},
            {70, "Lufthansa", TRAVEL, "320.00", "Flug Wien – München, Konferenz"},
            {75, "O'Reilly", TRAINING, "39.99", "Fachbuch Spring AI, Weiterbildung"},
            {80, "Cafe Sperl", MEALS, "19.80", "Kaffee mit Kunde, Terminabstimmung"},
            {85, "ÖBB", TRAVEL, "78.60", "Railjet Wien – Graz, Kundentermin Stadtwerke"},
        };
        for (Object[] row : rows) {
            list.add(expense((int) row[0], (String) row[1], (ExpenseCategory) row[2],
                    (String) row[3], (String) row[4]));
        }
        markAsOverridden(list, "Flasche Wein", "Regel 1/6: Alkohol und private Ausgabe.");
        markAsOverridden(list, "Geschenk für Familie", "Regel 6: Private Ausgabe.");
        return list;
    }

    private Expense expense(int daysAgo, String merchant, ExpenseCategory category,
                            String amount, String description) {
        var expense = new Expense();
        expense.setDate(LocalDate.now().minusDays(daysAgo));
        expense.setMerchant(merchant);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setDescription(description);
        return expense;
    }

    private void markAsOverridden(List<Expense> list, String text, String note) {
        list.stream().filter(e -> e.getDescription().contains(text)).forEach(e -> {
            e.setComplianceOverridden(true);
            e.setComplianceNote(note);
        });
    }
}
