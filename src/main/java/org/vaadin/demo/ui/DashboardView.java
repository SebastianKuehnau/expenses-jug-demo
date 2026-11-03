package org.vaadin.demo.ui;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseService;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Übersicht")
public class DashboardView extends VerticalLayout {

    public DashboardView(ExpenseService service) {
        List<Expense> all = service.findAll();
        add(new H2("Übersicht"), createCards(all));
    }

    private HorizontalLayout createCards(List<Expense> all) {
        var cards = new HorizontalLayout();
        cards.setWrap(true);
        cards.add(card("Summe laufender Monat", Formats.money(sumThisMonth(all))));
        cards.add(card("Anzahl Belege", String.valueOf(all.size())));
        sumsByCategory(all).forEach((category, sum) ->
                cards.add(card(category.getLabel(), Formats.money(sum))));
        return cards;
    }

    private Card card(String title, String value) {
        var card = new Card();
        card.setTitle(title);
        card.add(new Span(value));
        card.setWidth("14rem");
        return card;
    }

    private BigDecimal sumThisMonth(List<Expense> all) {
        YearMonth now = YearMonth.from(LocalDate.now());
        return all.stream().filter(e -> YearMonth.from(e.getDate()).equals(now))
                .map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<ExpenseCategory, BigDecimal> sumsByCategory(List<Expense> all) {
        return all.stream().collect(Collectors.groupingBy(Expense::getCategory,
                () -> new EnumMap<>(ExpenseCategory.class),
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
    }
}
