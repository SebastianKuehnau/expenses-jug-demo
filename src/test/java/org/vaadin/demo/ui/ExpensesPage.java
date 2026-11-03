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

    /** Saving first asks the AI (in the background), so wait until "Prüfe…" is over. */
    void clickSave() {
        window.findButton().withText("Speichern").click();
        for (int i = 0; i < 50 && window.findButton().withText("Prüfe…").exists(); i++) {
            window.roundTrip();
            sleep();
        }
        window.roundTrip();
    }

    private void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- receipt scan ---

    /** Drops a receipt on "Beleg scannen". The AI reads it in the background: wait for the result. */
    void scanReceipt(byte[] image) {
        window.findUpload().upload("beleg.png", "image/png", image);
        for (int i = 0; i < 50 && merchant().isEmpty(); i++) {
            window.roundTrip();
            sleep();
        }
        window.roundTrip();
    }

    String merchant() {
        return window.findTextField().withLabel("Händler").component().getValue();
    }

    String amountText() {
        return String.valueOf(window.findBigDecimalField().withLabel("Betrag").component().getValue());
    }

    String category() {
        return window.findComboBox(ExpenseCategory.class).withLabel("Kategorie").component()
                .getValue().getLabel();
    }

    String description() {
        return window.findTextArea().withLabel("Beschreibung").component().getValue();
    }

    // --- compliance warning ---

    boolean warningIconIsShown() {
        return window.find(ComplianceWarning.class).exists();
    }

    void clickWarningIcon() {
        // The popover is attached to the icon inside the ComplianceWarning composite.
        var icon = window.find(ComplianceWarning.class).single().getChildren().findFirst().orElseThrow();
        window.popoverFor(icon).clickTarget();
    }

    boolean popoverShows(String text) {
        return window.findPopover().exists()
                && window.findParagraph().withCondition(p -> p.getText().contains(text)).exists();
    }

    void clickSaveAnyway() {
        window.findButton().withText("Trotzdem speichern").click();
        window.roundTrip();
    }

    void changeDescriptionTo(String description) {
        window.findTextArea().withLabel("Beschreibung").setValue(description);
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
