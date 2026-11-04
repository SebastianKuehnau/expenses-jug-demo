package org.vaadin.demo.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.demo.compliance.ComplianceRules;

@Route(value = "rules", layout = MainLayout.class)
@PageTitle("Compliance rules")
public class RulesView extends VerticalLayout {

    public RulesView(ComplianceRules rules) {
        add(new H2("Compliance rules"), new Markdown(rules.text()));
    }
}
