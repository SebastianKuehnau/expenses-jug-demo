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

    /** Saving happens on another thread; wait until the grid has caught up. */
    boolean showsRowWithEventually(String text) {
        for (int i = 0; i < 50 && !showsRowWith(text); i++) {
            window.roundTrip();
            sleep();
        }
        return showsRowWith(text);
    }

    void selectRowWith(String text) {
        showsRowWithEventually(text);
        for (int i = 0; i < grid().size(); i++) {
            if (rowText(i).contains(text)) {
                grid().select(i);
                window.roundTrip();
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
        window.findButton().withText("New expense").click();
    }

    void fillForm(LocalDate date, String merchant, ExpenseCategory category, String amount, String description) {
        window.findDatePicker().withLabel("Date").setValue(date);
        window.findTextField().withLabel("Merchant").setValue(merchant);
        window.findComboBox(ExpenseCategory.class).withLabel("Category").selectItem(category.getLabel());
        enterAmount(amount);
        window.findTextArea().withLabel("Description").setValue(description);
    }

    void enterAmount(String amount) {
        window.findBigDecimalField().withLabel("Amount").setValue(new BigDecimal(amount));
    }

    /** Saving first asks the AI (in the background), so wait until "Checking…" is over. */
    void clickCancel() {
        window.findButton().withText("Cancel").click();
    }

    void clickSave() {
        window.findButton().withText("Save").click();
        for (int i = 0; i < 50 && window.findButton().withText("Checking…").exists(); i++) {
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

    /** Drops a receipt on "Scan receipt". The AI reads it in the background: wait for the result. */
    void scanReceipt(byte[] image) {
        window.findUpload().upload("beleg.png", "image/png", image);
        for (int i = 0; i < 50 && merchant().isEmpty(); i++) {
            window.roundTrip();
            sleep();
        }
        window.roundTrip();
    }

    String merchant() {
        return window.findTextField().withLabel("Merchant").component().getValue();
    }

    String amountText() {
        return String.valueOf(window.findBigDecimalField().withLabel("Amount").component().getValue());
    }

    String category() {
        return window.findComboBox(ExpenseCategory.class).withLabel("Category").component()
                .getValue().getLabel();
    }

    String description() {
        return window.findTextArea().withLabel("Description").component().getValue();
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
        window.findButton().withText("Save anyway").click();
        window.roundTrip();
    }

    void changeDescriptionTo(String description) {
        window.findTextArea().withLabel("Description").setValue(description);
    }

    void clickDelete() {
        window.findButton().withText("Delete").click();
    }

    void confirmDeletion() {
        window.findConfirmDialog().confirm();
    }

    String amountError() {
        return window.findBigDecimalField().withLabel("Amount").component().getErrorMessage();
    }

    String notification() {
        return window.findNotification().getText();
    }

    // --- filter bar ---

    /** The filter bar is collapsed at first. Open it, like the user (or the AI prompt) does. */
    void openFilters() {
        if (!window.findTextField().withLabel("Search").exists()) {
            window.findButton().withId("filter-toggle").click();
        }
    }

    void filterByCategory(ExpenseCategory category) {
        openFilters();
        window.findComboBox(ExpenseCategory.class).withLabel("Category filter").selectItem(category.getLabel());
    }

    void filterByText(String text) {
        openFilters();
        window.findTextField().withLabel("Search").setValue(text);
    }

    void filterByDates(LocalDate from, LocalDate to) {
        openFilters();
        window.findDatePicker().withLabel("Date from").setValue(from);
        window.findDatePicker().withLabel("Date to").setValue(to);
    }

    void filterByMinAmount(String amount) {
        openFilters();
        window.findBigDecimalField().withLabel("Amount from").setValue(new BigDecimal(amount));
    }

    void clickReset() {
        openFilters();
        window.findButton().withText("Reset").click();
    }

    void clickSearch() {
        openFilters();
        window.findButton().withText("Search").click();
    }

    private GridLocator<Expense> grid() {
        return window.findGrid(Expense.class);
    }
}
