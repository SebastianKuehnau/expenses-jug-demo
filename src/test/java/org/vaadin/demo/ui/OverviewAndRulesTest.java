package org.vaadin.demo.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class OverviewAndRulesTest {

    @Autowired
    ApplicationContext spring;

    BrowserlessApplicationContext app;

    @BeforeEach
    void openTheApp() {
        app = SpringBrowserlessApplicationContext.create(spring, DashboardView.class);
    }

    @AfterEach
    void closeTheApp() {
        app.close();
    }

    @Test
    void overview_shows_number_of_receipts_and_sums_per_category() {
        var employee = app.newUser().newWindow();

        employee.navigate(DashboardView.class);

        assertTrue(employee.findCard().withCondition(c -> "Anzahl Belege".equals(c.getTitleAsText())).exists());
        assertTrue(employee.findCard().withCondition(c -> "Reise".equals(c.getTitleAsText())).exists());
    }

    @Test
    void rules_screen_lists_the_compliance_rules() {
        var employee = app.newUser().newWindow();

        employee.navigate(RulesView.class);

        assertTrue(employee.findMarkdown().component().getContent().contains("Alkohol"));
    }
}
