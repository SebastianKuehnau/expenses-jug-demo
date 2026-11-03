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
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;

@Route(value = "expenses", layout = MainLayout.class)
@PageTitle("Ausgaben")
public class ExpensesView extends VerticalLayout {

    private static final int DESCRIPTION_LENGTH = 50;

    /**
     * How the AI should operate THIS screen. {@code %s} is replaced by what the user typed.
     * The AI does not write a query. It fills the visible filter fields and clicks "Suchen",
     * so the user sees what was filtered, can correct it, and normal validation still applies.
     */
    private static final String AI_GOAL = """
            Der Nutzer beschreibt in eigenen Worten, welche Ausgaben er sehen will. Setze in der \
            Filterleiste nur die Felder, die die Anfrage nahelegt, leere alle anderen Felder und \
            klicke dann auf "Suchen".

            Zuordnung:
            - Händler oder Stichwort: Feld "Suche"
            - Art der Ausgabe (Reise, Bewirtung, Unterkunft, Büro, Software, Weiterbildung): "Kategorie-Filter"
            - Beträge: "Betrag von" und "Betrag bis" (nur die Zahl, ohne Währung)
            - Zeitraum: "Datum von" und "Datum bis". Relative Angaben wie "letzter Monat" oder \
            "dieses Quartal" beziehen sich auf das heutige Datum.

            Erfinde keine Bedingungen, die der Nutzer nicht genannt hat. Anfrage: "%s"
            """;

    private final ExpenseService service;
    private final Grid<Expense> grid = new Grid<>(Expense.class, false);
    private final ExpenseForm form;
    private ExpenseFilter filter = ExpenseFilter.NONE;

    public ExpensesView(ExpenseService service, UiAssistant assistant) {
        this.service = service;
        this.form = new ExpenseForm(service, this::closeForm);
        form.setVisible(false);
        setSizeFull();
        var filterBar = new ExpenseFilterBar(this::applyFilter);
        add(createAiBar(assistant, filterBar), filterBar, createNewButton(), createSplitLayout());
    }

    private AiGoalBar createAiBar(UiAssistant assistant, ExpenseFilterBar filterBar) {
        var aiBar = new AiGoalBar(assistant, "Filtern", AI_GOAL);
        aiBar.setPlaceholder("z. B. Bewirtung über 100 € im Oktober");
        // The AI may only see and touch the filter bar, never the grid or the form.
        aiBar.setScope(AgentScope.within(filterBar));
        return aiBar;
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
