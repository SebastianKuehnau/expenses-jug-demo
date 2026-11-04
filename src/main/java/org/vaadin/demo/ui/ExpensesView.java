package org.vaadin.demo.ui;

import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.AiGoalBar;
import com.vaadin.draiv.ai.ui.UiAssistant;
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
import org.vaadin.demo.compliance.ComplianceService;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;
import org.vaadin.demo.receipt.ReceiptExtractionService;

@Route(value = "expenses", layout = MainLayout.class)
@PageTitle("Expenses")
public class ExpensesView extends VerticalLayout {

    private static final int DESCRIPTION_LENGTH = 50;

    /**
     * How the AI should operate THIS screen. {@code %s} is replaced by what the user typed.
     * The AI does not write a query. It fills the visible filter fields and clicks "Search",
     * so the user sees what was filtered, can correct it, and normal validation still applies.
     */
    private static final String AI_GOAL = """
            The user describes in their own words which expenses they want to see. Set only the \
            fields in the filter bar that the request implies, clear all other fields, then \
            click "Search".

            Mapping:
            - Merchant or keyword: field "Search"
            - Kind of expense (Travel, Meals, Accommodation, Office, Software, Training): "Category filter"
            - Amounts: "Amount from" and "Amount to" (the number only, no currency)
            - Period: "Date from" and "Date to". Relative terms like "last month" or \
            "this quarter" refer to today's date.

            Do not invent conditions the user did not mention. Request: "%s"
            """;

    private final ExpenseService service;
    private final Grid<Expense> grid = new Grid<>(Expense.class, false);
    private final ExpenseForm form;
    private ExpenseFilter filter = ExpenseFilter.NONE;

    public ExpensesView(ExpenseService service, ComplianceService compliance,
            ReceiptExtractionService receipts, UiAssistant assistant) {
        this.service = service;
        this.form = new ExpenseForm(service, compliance, receipts, this::closeForm);
        form.setVisible(false);
        setSizeFull();
        var filterBar = new ExpenseFilterBar(this::applyFilter);
        add(createAiBar(assistant, filterBar), filterBar, createNewButton(), createSplitLayout());
    }

    private AiGoalBar createAiBar(UiAssistant assistant, ExpenseFilterBar filterBar) {
        var aiBar = new AiGoalBar(assistant, "Filter", AI_GOAL);
        aiBar.setPlaceholder("e.g. meals over €100 in October");
        // The AI may only see and touch the filter bar, never the grid or the form.
        aiBar.setScope(AgentScope.within(filterBar));
        return aiBar;
    }

    private Button createNewButton() {
        return new Button("New expense", VaadinIcon.PLUS.create(), e -> {
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
        var date = grid.addColumn(Expense::getDate).setHeader("Date").setSortProperty("date");
        grid.addColumn(Expense::getMerchant).setHeader("Merchant").setSortProperty("merchant");
        grid.addColumn(e -> e.getCategory().getLabel()).setHeader("Category").setSortProperty("category");
        grid.addColumn(this::shortDescription).setHeader("Description");
        grid.addColumn(e -> Formats.money(e.getAmount())).setHeader("Amount")
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
        icon.setColor("var(--aura-orange-text)");
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
