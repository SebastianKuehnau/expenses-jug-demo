package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
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

/** A receipt attached in the chat can be stored with the expense that the form creates. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChatReceiptTest {

    private static final byte[] RECEIPT_IMAGE = {1, 2, 3};

    @Autowired
    ApplicationContext spring;
    @MockitoBean
    ComplianceService compliance;
    @MockitoBean
    TextPresenterProperties aiProperties;
    @MockitoBean
    TextPresenterService presenter;

    BrowserlessApplicationContext app;
    ExpensesPage employee;
    ChatPage chat;

    @BeforeEach
    void employeeAttachesAReceiptInTheChat() {
        when(aiProperties.isEnabled()).thenReturn(true);
        when(compliance.check(any())).thenReturn(new ComplianceResult(true, null));
        app = SpringBrowserlessApplicationContext.create(spring, org.vaadin.demo.ui.ExpensesView.class);
        var window = app.newUser().newWindow();
        employee = new ExpensesPage(window);
        chat = new ChatPage(window);
        employee.open();
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void formOffersNoChatReceiptsUntilOneIsAttached() {
        employee.clickNewExpense();

        assertFalse(employee.chatReceiptFieldIsShown());
    }

    @Test
    void receiptFromTheChatIsStoredWithTheExpense() {
        chat.clickChatButton();
        chat.attach("ticket.png", "image/png", RECEIPT_IMAGE);
        employee.clickNewExpense();
        assertTrue(employee.chatReceiptFieldIsShown());

        employee.fillForm(java.time.LocalDate.now(), "Chat Rail", ExpenseCategory.TRAVEL, "12.00",
                "Train ticket from the chat");
        employee.pickChatReceipt("ticket.png");
        assertTrue(employee.receiptPreviewIsShown());
        employee.clickSave();
        assertTrue(employee.showsRowWithEventually("Train ticket from the chat"));

        employee.selectRowWith("Train ticket from the chat");

        assertTrue(employee.receiptPreviewIsShown());
    }
}
