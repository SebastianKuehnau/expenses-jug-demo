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
            {2, "ÖBB", TRAVEL, "78.60", "Railjet Vienna – Graz, client meeting Stadtwerke"},
            {3, "Hotel Daniel Graz", ACCOMMODATION, "139.00", "Overnight stay, client meeting Stadtwerke Graz"},
            {4, "The Liffey Bridge Hotel", ACCOMMODATION, "149.00", "2 nights Dublin, Dublin Tech Summit attendance"},
            {5, "Gasthaus Zum Schwarzen Adler", MEALS, "42.80", "Lunch with client M. Huber, project alignment"},
            {6, "JetBrains", SOFTWARE, "289.00", "IntelliJ IDEA Ultimate annual license, project Expenses"},
            {7, "The Liffey Bridge Hotel", ACCOMMODATION, "129.00", "1 night Dublin, workshop with client Northwind"},
            {8, "Deutsche Bahn", TRAVEL, "119.90", "ICE Munich – Vienna, JavaLand conference"},
            {9, "Hotel Bayerischer Hof München", ACCOMMODATION, "165.00", "Overnight stay, conference Munich"},
            {10, "Amazon Business", OFFICE, "34.99", "USB-C docking station for the office"},
            {12, "Café Central", MEALS, "27.50", "Breakfast with colleague J. Novak, sprint planning"},
            {13, "GitHub", SOFTWARE, "21.00", "GitHub Team subscription, project Expenses"},
            {14, "The Liffey Bridge Hotel", ACCOMMODATION, "175.00", "3 nights Dublin, client onsite Northwind"},
            {15, "Wiener Linien", TRAVEL, "38.20", "Weekly pass Vienna, client meetings"},
            {16, "Udemy", TRAINING, "49.99", "Course Spring Security, training for the backend team"},
            {18, "Restaurant Steirereck", MEALS, "186.00", "Dinner"},
            {19, "Libro", OFFICE, "18.40", "Notebooks and pens for workshop"},
            {21, "ÖBB", TRAVEL, "54.30", "Railjet Vienna – Salzburg, workshop client Alpenbau"},
            {22, "Hotel Stein Salzburg", ACCOMMODATION, "212.00", "Overnight stay, workshop Salzburg"},
            {23, "The Liffey Bridge Hotel", ACCOMMODATION, "139.00", "1 night Dublin, customer demo Contoso"},
            {24, "Weinhandlung Kolb", OTHER, "48.00", "Bottle of wine for home"},
            {25, "Atlassian", SOFTWARE, "63.00", "Jira Cloud subscription, project Expenses"},
            {27, "Eurowings", TRAVEL, "189.00", "Flight Vienna – Berlin, client meeting Berlin"},
            {29, "Hotel Adlon Berlin", ACCOMMODATION, "178.00", "Overnight stay, client meeting Berlin"},
            {30, "Berliner Verkehrsbetriebe", TRAVEL, "9.80", "Day ticket Berlin, client meeting"},
            {33, "Vapiano", MEALS, "31.20", "Lunch with client ACME (T. Berger), quote discussion"},
            {35, "IKEA", OFFICE, "129.00", "Desk lamps and shelving, Vienna office"},
            {36, "Pluralsight", TRAINING, "29.00", "Monthly Pluralsight subscription, training Java 25"},
            {38, "ÖBB", TRAVEL, "78.60", "Railjet Graz – Vienna, return from client meeting"},
            {40, "Figma", SOFTWARE, "15.00", "Figma Professional, project Expenses"},
            {41, "The Liffey Bridge Hotel", ACCOMMODATION, "158.00", "2 nights Dublin, partner summit Fabrikam"},
            {42, "Brauhaus Salzburg", MEALS, "96.40", "Dinner with client Alpenbau (H. Moser, K. Leitner), workshop wrap-up"},
            {44, "Staples", OFFICE, "76.50", "Monitor stand and cables"},
            {46, "Bolt", TRAVEL, "22.40", "Taxi Vienna airport, business trip"},
            {48, "Hotel Sacher Wien", ACCOMMODATION, "245.00", "Overnight stay, miscellaneous"},
            {50, "Konferenz W-JAX", TRAINING, "690.00", "Conference ticket W-JAX Munich, training"},
            {52, "Spar", OTHER, "23.60", "Gift for family"},
            {55, "ÖBB", TRAVEL, "49.90", "Railjet Vienna – Linz, client meeting"},
            {58, "Slack", SOFTWARE, "42.00", "Slack Pro subscription, project Expenses"},
            {60, "Gasthaus Pöschl", MEALS, "38.90", "Lunch with colleague J. Novak, retrospective"},
            {63, "Post", OFFICE, "12.90", "Shipping documents to client"},
            {66, "Hotel Wiesler Graz", ACCOMMODATION, "158.00", "Overnight stay, client meeting Graz"},
            {70, "Lufthansa", TRAVEL, "320.00", "Flight Vienna – Munich, conference"},
            {75, "O'Reilly", TRAINING, "39.99", "Technical book Spring AI, training"},
            {80, "Cafe Sperl", MEALS, "19.80", "Coffee with client, scheduling"},
            {85, "ÖBB", TRAVEL, "78.60", "Railjet Vienna – Graz, client meeting Stadtwerke"},
        };
        for (Object[] row : rows) {
            list.add(expense((int) row[0], (String) row[1], (ExpenseCategory) row[2],
                    (String) row[3], (String) row[4]));
        }
        markAsOverridden(list, "Bottle of wine", "Rule 1/6: alcohol and private expense.");
        markAsOverridden(list, "Gift for family", "Rule 6: private expense.");
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
