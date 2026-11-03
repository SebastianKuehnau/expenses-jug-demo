package org.vaadin.demo.ui;

import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.flow.component.grid.GridLocator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseCategory;

/** Where things are on {@link ExpensesView}. The only place that knows labels and button texts. */
class ExpensesPage {

    private final BrowserlessUIContext window;

    ExpensesPage(BrowserlessUIContext window) {
        this.window = window;
    }

    void open() {
        window.navigate(ExpensesView.class);
    }

    // --- grid ---

    int rowCount() {
        return grid().size();
    }

    boolean showsRowWith(String text) {
        return rowTexts().stream().anyMatch(row -> row.contains(text));
    }

    void selectRowWith(String text) {
        for (int i = 0; i < grid().size(); i++) {
            if (rowText(i).contains(text)) {
                grid().select(i);
                return;
            }
        }
        throw new AssertionError("No row contains '" + text + "': " + rowTexts());
    }

    private List<String> rowTexts() {
        return IntStream.range(0, grid().size()).mapToObj(this::rowText).toList();
    }

    /** All visible cells of a row as one text: date, merchant, category, description, amount. */
    private String rowText(int row) {
        return IntStream.range(0, 5).mapToObj(column -> grid().getCellText(row, column))
                .collect(Collectors.joining(" | ")).replace('\u00a0', ' '); // the currency format uses a non-breaking space
    }

    // --- form ---

    void clickNewExpense() {
        window.findButton().withText("Neue Ausgabe").click();
    }

    void fillForm(LocalDate date, String merchant, ExpenseCategory category, String amount, String description) {
        window.findDatePicker().withLabel("Datum").setValue(date);
        window.findTextField().withLabel("Händler").setValue(merchant);
        window.findComboBox(ExpenseCategory.class).withLabel("Kategorie").selectItem(category.getLabel());
        enterAmount(amount);
        window.findTextArea().withLabel("Beschreibung").setValue(description);
    }

    void enterAmount(String amount) {
        window.findBigDecimalField().withLabel("Betrag").setValue(new BigDecimal(amount));
    }

    void clickSave() {
        window.findButton().withText("Speichern").click();
    }

    void clickDelete() {
        window.findButton().withText("Löschen").click();
    }

    void confirmDeletion() {
        window.findConfirmDialog().confirm();
    }

    String amountError() {
        return window.findBigDecimalField().withLabel("Betrag").component().getErrorMessage();
    }

    String notification() {
        return window.findNotification().getText();
    }

    // --- filter bar ---

    void filterByCategory(ExpenseCategory category) {
        window.findComboBox(ExpenseCategory.class).withLabel("Kategorie-Filter").selectItem(category.getLabel());
    }

    void filterByText(String text) {
        window.findTextField().withLabel("Suche").setValue(text);
    }

    void filterByDates(LocalDate from, LocalDate to) {
        window.findDatePicker().withLabel("Datum von").setValue(from);
        window.findDatePicker().withLabel("Datum bis").setValue(to);
    }

    void filterByMinAmount(String amount) {
        window.findBigDecimalField().withLabel("Betrag von").setValue(new BigDecimal(amount));
    }

    void clickReset() {
        window.findButton().withText("Zurücksetzen").click();
    }

    void clickSearch() {
        window.findButton().withText("Suchen").click();
    }

    private GridLocator<Expense> grid() {
        return window.findGrid(Expense.class);
    }
}
