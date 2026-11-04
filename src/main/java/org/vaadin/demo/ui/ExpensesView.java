package org.vaadin.demo.ui;

import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.AiGoalBar;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.LocalDate;
import org.vaadin.demo.compliance.ComplianceService;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;
import org.vaadin.demo.receipt.ReceiptFormAi;

@Route(value = "expenses", layout = MainLayout.class)
@PageTitle("Expenses")
public class ExpensesView extends VerticalLayout {

    private static final int DESCRIPTION_LENGTH = 50;

    private static final String AI_BUTTON_TEXT = "Filter";

    /**
     * How the AI should operate THIS screen. {@code %s} is replaced by what the user typed.
     * The AI does not write a query. It fills the visible filter fields and clicks "Search",
     * so the user sees what was filtered, can correct it, and normal validation still applies.
     */
    private static final String AI_GOAL = """
            The user describes in their own words which expenses they want to see. Set only the \
            fields in the filter bar that the request implies, clear all other fields, then \
            click "Search". Set all fields at once: call all the tools you need in a single \
            step, in parallel, not one after another.

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
    private final Dialog formDialog = new Dialog();
    private final ExpenseFilterBar filterBar = new ExpenseFilterBar(this::applyFilter);
    private final Button filterToggle = new Button(VaadinIcon.ANGLE_DOWN.create());
    private final Badge activeFilters = new Badge();
    /** Frees the AI bar again, see {@link #createAiBar}. */
    private Runnable releaseAiBar = () -> { };
    private ExpenseFilter filter = ExpenseFilter.NONE;

    public ExpensesView(ExpenseService service, ComplianceService compliance,
            ReceiptFormAi receiptAi, UiAssistant assistant) {
        this.service = service;
        this.form = new ExpenseForm(service, compliance, receiptAi, this::closeForm);
        setSizeFull();
        configureFormDialog();
        filterBar.setVisible(false);
        add(createSearchRow(assistant), filterBar, createGrid(), createNewButton());
    }

    private HorizontalLayout createSearchRow(UiAssistant assistant) {
        var row = new HorizontalLayout(createAiBar(assistant, filterBar), createFilterToggle());
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        row.setWidthFull();
        return row;
    }

    private Button createFilterToggle() {
        activeFilters.addThemeVariants(BadgeVariant.NUMBER_ONLY, BadgeVariant.SMALL);
        activeFilters.setVisible(false);
        filterToggle.setSuffixComponent(activeFilters);
        filterToggle.addThemeVariants(ButtonVariant.TERTIARY);
        filterToggle.setId("filter-toggle");
        filterToggle.setTooltipText("Show/hide filters");
        filterToggle.addClickListener(e -> showFilterBar(!filterBar.isVisible()));
        return filterToggle;
    }

    private void showFilterBar(boolean visible) {
        filterBar.setVisible(visible);
        filterToggle.setIcon((visible ? VaadinIcon.ANGLE_UP : VaadinIcon.ANGLE_DOWN).create());
    }

    private AiGoalBar createAiBar(UiAssistant assistant, ExpenseFilterBar filterBar) {
        var aiBar = new AiGoalBar(assistant, AI_BUTTON_TEXT, AI_GOAL) {
//            {
//                // The AI keeps talking for a while after it clicked "Search". The user does not
//                // have to wait for that: as soon as the filter is applied, the bar is free again.
//                releaseAiBar = () -> {
//                    getPromptField().setEnabled(true);
//                    button().setEnabled(true);
//                    setButtonText(AI_BUTTON_TEXT);
//                };
//            }

            @Override
            protected String resolveGoal() {
                // Every prompt starts from scratch: no values left over from the last one.
                filterBar.clearFields();
                getPromptField().setEnabled(false);
                return super.resolveGoal();
            }

            @Override
            protected void onSuccess() {
                // No super call: it would clear the prompt. The prompt stays, so the user sees what was asked.
                getPromptField().setEnabled(true);
            }
        };
        aiBar.setPlaceholder("e.g. meals over €100 in October");
        // The clear button of the prompt field also resets the filter.
        aiBar.getPromptField().addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue().isEmpty()) {
                filterBar.reset();
            }
        });
        // The AI may only see and touch the filter bar, never the grid or the form.
        aiBar.setScope(AgentScope.within(filterBar));

        return aiBar;
    }

    private Button createNewButton() {
        var newExpenseButton = new Button("New expense", VaadinIcon.PLUS.create(), e -> {
            grid.deselectAll();
            openForm(newExpense());
        });
        newExpenseButton.addThemeVariants(ButtonVariant.LARGE, ButtonVariant.PRIMARY);
        setAlignSelf(Alignment.END, newExpenseButton);
        return newExpenseButton;
    }

    private Grid<Expense> createGrid() {
        configureGrid();
        return grid;
    }

    /** The form lives in a dialog. Closing it in any way (Esc, click outside) unselects the row. */
    private void configureFormDialog() {
        formDialog.add(form);
        formDialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                grid.deselectAll();
            }
        });
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
        //releaseAiBar.run();
        activeFilters.setNumber(newFilter.activeCriteria());
        activeFilters.setVisible(newFilter.activeCriteria() > 0);
        grid.getDataProvider().refreshAll();
    }

    private void openForm(Expense expense) {
        form.edit(expense);
        formDialog.setHeaderTitle(expense.getId() == null ? "New expense" : "Edit expense");
        formDialog.open();
    }

    private void closeForm() {
        formDialog.close();
        grid.getDataProvider().refreshAll();
    }
}
