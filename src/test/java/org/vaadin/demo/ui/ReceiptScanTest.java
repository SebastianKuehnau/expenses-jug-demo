package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import java.math.BigDecimal;
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
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseFilter;
import org.vaadin.demo.expense.ExpenseService;
import org.vaadin.demo.receipt.ReceiptData;
import org.vaadin.demo.receipt.ReceiptExtractionService;

/** "Scan receipt" in the expense form. The AI that reads the receipt is replaced by a fake. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReceiptScanTest {

    private static final byte[] RECEIPT_IMAGE = {1, 2, 3};

    @Autowired
    ApplicationContext spring;
    @Autowired
    ExpenseService expenses;
    @MockitoBean
    ReceiptExtractionService extraction;
    @MockitoBean
    ComplianceService compliance;

    BrowserlessApplicationContext app;
    ExpensesPage employee;
    long expensesBefore;

    @BeforeEach
    void employeeOpensTheFormForANewExpense() {
        when(extraction.isAvailable()).thenReturn(true);
        when(extraction.extract(any(), any())).thenReturn(new ReceiptData(
                LocalDate.now().minusDays(3), "ÖBB", new BigDecimal("78.60"), "EUR",
                ExpenseCategory.TRAVEL, "Railjet Vienna – Graz"));
        when(compliance.check(any())).thenReturn(new ComplianceResult(true, null));
        app = SpringBrowserlessApplicationContext.create(spring, ExpensesView.class);
        employee = new ExpensesPage(app.newUser().newWindow());
        employee.open();
        expensesBefore = expenses.count(ExpenseFilter.NONE);
        employee.clickNewExpense();
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void scannedReceiptFillsTheFormButIsNotSavedYet() {
        employee.scanReceipt(RECEIPT_IMAGE);

        assertEquals("ÖBB", employee.merchant());
        assertEquals("78.60", employee.amountText());
        assertEquals("Travel", employee.category());
        assertEquals("Railjet Vienna – Graz", employee.description());
        assertEquals(expensesBefore, expenses.count(ExpenseFilter.NONE));
    }

    @Test
    void employeeReviewsTheScannedReceiptAndSavesItWithTheComplianceCheck() {
        employee.scanReceipt(RECEIPT_IMAGE);

        employee.clickSave();

        assertEquals(expensesBefore + 1, expenses.count(ExpenseFilter.NONE));
        verify(compliance).check(any());
        assertTrue(employee.showsRowWith("Railjet Vienna"));
    }
}
