package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.vaadin.demo.compliance.ComplianceResult;
import org.vaadin.demo.compliance.ComplianceService;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;

/** What happens when the AI objects to an expense. The AI is replaced by a fake. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ComplianceCheckTest {

    private static final String FEEDBACK = "Rule 2: occasion and attendees are missing.";

    @Autowired
    ApplicationContext spring;
    @Autowired
    ExpenseService expenses;
    @MockitoBean
    ComplianceService compliance;

    BrowserlessApplicationContext app;
    ExpensesPage employee;
    long expensesBefore;

    @BeforeEach
    void employeeIsAboutToEnterAVagueDinner() {
        when(compliance.check(any())).thenReturn(new ComplianceResult(false, FEEDBACK));
        app = SpringBrowserlessApplicationContext.create(spring, ExpensesView.class);
        employee = new ExpensesPage(app.newUser().newWindow());
        employee.open();
        expensesBefore = expenses.count(ExpenseFilter.NONE);
        employee.clickNewExpense();
        employee.fillForm(LocalDate.now(), "Restaurant Mayer", ExpenseCategory.MEALS, "180", "Abendessen");
        employee.clickSave();
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void expenseThatBreaksARuleIsNotSavedAndShowsAWarning() {
        assertTrue(employee.warningIconIsShown());
        assertEquals(expensesBefore, expenses.count(ExpenseFilter.NONE));
    }

    @Test
    void warningExplainsWhichRuleWasBroken() {
        employee.clickWarningIcon();

        assertTrue(employee.popoverShows(FEEDBACK));
    }

    @Test
    void employeeCanSaveAnywayAndTheWarningIsRemembered() {
        employee.clickWarningIcon();

        employee.clickSaveAnyway();

        assertEquals(expensesBefore + 1, expenses.count(ExpenseFilter.NONE));
        Expense saved = expenses.findAll().stream()
                .filter(e -> "Restaurant Mayer".equals(e.getMerchant())).findFirst().orElseThrow();
        assertTrue(saved.isComplianceOverridden());
        assertEquals(FEEDBACK, saved.getComplianceNote());
    }

    @Test
    void changingTheDescriptionRemovesTheOldWarning() {
        employee.changeDescriptionTo("Abendessen mit Kunde ACME (M. Huber), Projekt-Kickoff");

        assertFalse(employee.warningIconIsShown());
    }

    @Test
    void afterFixingTheDescriptionTheExpenseIsCheckedAgainAndSaved() {
        when(compliance.check(any())).thenReturn(new ComplianceResult(true, null));
        employee.changeDescriptionTo("Abendessen mit Kunde ACME (M. Huber), Projekt-Kickoff");

        employee.clickSave();

        assertEquals(expensesBefore + 1, expenses.count(ExpenseFilter.NONE));
        assertFalse(employee.warningIconIsShown());
    }
}
