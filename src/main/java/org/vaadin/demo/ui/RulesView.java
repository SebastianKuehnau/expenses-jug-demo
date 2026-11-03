package org.vaadin.demo.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

@Route(value = "rules", layout = MainLayout.class)
@PageTitle("Compliance-Regeln")
public class RulesView extends VerticalLayout {

    public RulesView() {
        add(new H2("Compliance-Regeln"), new Markdown(readRules()));
    }

    private String readRules() {
        try {
            return new ClassPathResource("compliance-rules.md")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
