package org.vaadin.demo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.LocalDate;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;

@Route(value = "expenses", layout = MainLayout.class)
@PageTitle("Ausgaben")
public class ExpensesView extends VerticalLayout {

    private static final int DESCRIPTION_LENGTH = 50;

    private final ExpenseService service;
    private final Grid<Expense> grid = new Grid<>(Expense.class, false);
    private final ExpenseForm form;
    private ExpenseFilter filter = ExpenseFilter.NONE;

    public ExpensesView(ExpenseService service) {
        this.service = service;
        this.form = new ExpenseForm(service, this::closeForm);
        form.setVisible(false);
        setSizeFull();
        add(new ExpenseFilterBar(this::applyFilter), createNewButton(), createSplitLayout());
    }

    private Button createNewButton() {
        return new Button("Neue Ausgabe", VaadinIcon.PLUS.create(), e -> {
            grid.deselectAll();
            openForm(newExpense());
        });
    }

    private SplitLayout createSplitLayout() {
        configureGrid();
        var split = new SplitLayout(grid, form);
        split.setSizeFull();
        split.setSplitterPosition(65);
        return split;
    }

    private void configureGrid() {
        var date = grid.addColumn(Expense::getDate).setHeader("Datum").setSortProperty("date");
        grid.addColumn(Expense::getMerchant).setHeader("Händler").setSortProperty("merchant");
        grid.addColumn(e -> e.getCategory().getLabel()).setHeader("Kategorie").setSortProperty("category");
        grid.addColumn(this::shortDescription).setHeader("Beschreibung");
        grid.addColumn(e -> Formats.money(e.getAmount())).setHeader("Betrag")
                .setTextAlign(ColumnTextAlign.END).setSortProperty("amount");
        grid.addComponentColumn(this::createWarningIcon).setHeader("").setWidth("4rem").setFlexGrow(0);

        // Lazy loading: the Grid asks for one page at a time, straight from the database.
        // No REST endpoint, no DTO mapping.
        grid.setItems(
                query -> service.find(filter, VaadinSpringDataHelpers.toSpringPageRequest(query)).stream(),
                query -> (int) service.count(filter));
        grid.sort(GridSortOrder.desc(date).build());
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                openForm(e.getValue());
            }
        });
    }

    private String shortDescription(Expense expense) {
        String text = expense.getDescription();
        return text.length() > DESCRIPTION_LENGTH ? text.substring(0, DESCRIPTION_LENGTH) + "…" : text;
    }

    private Component createWarningIcon(Expense expense) {
        if (!expense.isComplianceOverridden()) {
            return new Span();
        }
        var icon = VaadinIcon.WARNING.create();
        icon.setColor("var(--aura-orange)");
        icon.setTooltipText(expense.getComplianceNote());
        return icon;
    }

    private Expense newExpense() {
        var expense = new Expense();
        expense.setDate(LocalDate.now());
        return expense;
    }

    private void applyFilter(ExpenseFilter newFilter) {
        this.filter = newFilter;
        grid.getDataProvider().refreshAll();
    }

    private void openForm(Expense expense) {
        form.edit(expense);
        form.setVisible(true);
    }

    private void closeForm() {
        form.setVisible(false);
        grid.deselectAll();
        grid.getDataProvider().refreshAll();
    }
}
