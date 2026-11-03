package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {"spring.ai.openai.api-key=not-configured"})
class ChatWithoutApiKeyTest {

    @Autowired
    ApplicationContext spring;

    @Test
    void appStartsAndChatButtonIsDisabledWithoutApiKey() {
        try (BrowserlessApplicationContext app =
                SpringBrowserlessApplicationContext.create(spring, DashboardView.class)) {
            var window = app.newUser().newWindow();
            window.navigate(DashboardView.class);

            assertFalse(new ChatPage(window).chatButtonIsEnabled());
        }
    }
}
