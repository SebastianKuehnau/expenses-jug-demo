package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;

/** Use cases of the expenses screen, run without a browser and without a server. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // fresh seed data per test
class ExpensesViewTest {

    @Autowired
    ApplicationContext spring;
    @Autowired
    ExpenseService expenses;

    BrowserlessApplicationContext app;
    ExpensesPage employee;

    @BeforeEach
    void employeeOpensTheExpensesScreen() {
        app = SpringBrowserlessApplicationContext.create(spring, ExpensesView.class);
        employee = new ExpensesPage(app.newUser().newWindow());
        employee.open();
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void grid_shows_seeded_expenses() {
        assertTrue(employee.rowCount() >= 40);
        assertTrue(employee.showsRowWith("ÖBB"));
    }

    @Test
    void create_expense_via_form() {
        int before = (int) expenses.count(ExpenseFilter.NONE);

        employee.clickNewExpense();
        employee.fillForm(LocalDate.now(), "Taxi Wien", ExpenseCategory.TRAVEL, "23.50",
                "Taxi zum Kundentermin");
        employee.clickSave();

        assertEquals("Ausgabe gespeichert", employee.notification());
        assertEquals(before + 1, employee.rowCount());
        assertTrue(employee.showsRowWith("Taxi Wien"));
    }

    @Test
    void edit_expense() {
        employee.selectRowWith("IntelliJ IDEA");

        employee.enterAmount("301.00");
        employee.clickSave();

        assertEquals("Ausgabe gespeichert", employee.notification());
        assertTrue(employee.showsRowWith("€ 301,00"));
    }

    @Test
    void delete_expense() {
        int before = employee.rowCount();
        employee.selectRowWith("Notizbücher");

        employee.clickDelete();
        employee.confirmDeletion();

        assertEquals(before - 1, employee.rowCount());
        assertFalse(employee.showsRowWith("Notizbücher"));
    }

    @Test
    void invalid_input_is_rejected() {
        int before = (int) expenses.count(ExpenseFilter.NONE);

        employee.clickNewExpense();
        employee.fillForm(LocalDate.now(), "Taxi Wien", ExpenseCategory.TRAVEL, "0",
                "Taxi zum Kundentermin");
        employee.clickSave();

        assertNotNull(employee.amountError());
        assertEquals("Der Betrag muss größer als 0 sein", employee.amountError());
        assertEquals(before, expenses.count(ExpenseFilter.NONE));
    }

    @Test
    void filter_by_category_and_amount() {
        employee.filterByCategory(ExpenseCategory.ACCOMMODATION);
        employee.filterByMinAmount("200");
        employee.clickSearch();

        assertEquals(2, employee.rowCount()); // Hotel Stein (212) and Hotel Sacher (245)
        assertTrue(employee.showsRowWith("Hotel Sacher Wien"));
        assertFalse(employee.showsRowWith("ÖBB"));
    }
}
