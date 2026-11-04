package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnResult;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** The assistant window, with a fake AI behind it (no network, no API key needed). */
@SpringBootTest
class ChatTest {

    @Autowired
    ApplicationContext spring;
    @MockitoBean
    TextPresenterProperties aiProperties;
    @MockitoBean
    TextPresenterService presenter;

    BrowserlessApplicationContext app;
    ChatPage employee;

    @BeforeEach
    void anAiIsConfigured() {
        when(aiProperties.isEnabled()).thenReturn(true);
        when(presenter.respond(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new TurnResult("You can find the rules under **Compliance rules**.",
                        List.of(), List.of(), true, "completed", null));
        app = SpringBrowserlessApplicationContext.create(spring, DashboardView.class);
        var window = app.newUser().newWindow();
        window.navigate(DashboardView.class);
        employee = new ChatPage(window);
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void employeeOpensTheChatAndIsGreeted() {
        employee.clickChatButton();

        assertTrue(employee.showsMessage("Tell me what I can do for you in the expense app"));
    }

    @Test
    void employeeAsksAQuestionAndGetsAnAnswer() throws InterruptedException {
        employee.clickChatButton();

        employee.send("Where can I see the compliance rules?");

        assertTrue(employee.showsMessageEventually("You can find the rules under"));
    }

    @Test
    void chatIsClosedUntilTheEmployeeOpensIt() {
        assertFalse(employee.chatIsOpen());
    }
}
