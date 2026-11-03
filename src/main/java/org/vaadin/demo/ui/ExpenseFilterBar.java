package org.vaadin.demo.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseFilter;

/** The search fields above the grid. "Suchen" hands the typed values over as an {@link ExpenseFilter}. */
public class ExpenseFilterBar extends HorizontalLayout {

    private final TextField text = new TextField("Suche");
    private final ComboBox<ExpenseCategory> category = new ComboBox<>("Kategorie-Filter");
    private final BigDecimalField minAmount = new BigDecimalField("Betrag von");
    private final BigDecimalField maxAmount = new BigDecimalField("Betrag bis");
    private final DatePicker from = new DatePicker("Datum von");
    private final DatePicker to = new DatePicker("Datum bis");
    private final Consumer<ExpenseFilter> onSearch;

    public ExpenseFilterBar(Consumer<ExpenseFilter> onSearch) {
        this.onSearch = onSearch;
        text.setPlaceholder("Händler oder Beschreibung");
        category.setItems(ExpenseCategory.values());
        category.setItemLabelGenerator(ExpenseCategory::getLabel);
        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        setWrap(true);
        add(text, category, minAmount, maxAmount, from, to, createSearchButton(), createResetButton());
    }

    private Button createSearchButton() {
        var button = new Button("Suchen", e -> search());
        button.addThemeVariants(ButtonVariant.PRIMARY);
        return button;
    }

    private Button createResetButton() {
        return new Button("Zurücksetzen", e -> reset());
    }

    private void search() {
        onSearch.accept(new ExpenseFilter(text.getValue(), category.getValue(),
                minAmount.getValue(), maxAmount.getValue(), from.getValue(), to.getValue()));
    }

    private void reset() {
        text.clear();
        category.clear();
        minAmount.clear();
        maxAmount.clear();
        from.clear();
        to.clear();
        search();
    }
}
