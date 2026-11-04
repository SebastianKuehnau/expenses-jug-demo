package org.vaadin.demo.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import java.util.function.Consumer;

/**
 * A yellow warning icon. Click it to read the AI's feedback in a popover and decide:
 * close and fix the input, or "Save anyway".
 */
public class ComplianceWarning extends Composite<Icon> {

    private final Paragraph feedbackText = new Paragraph();
    private final Popover popover = new Popover();
    private String feedback;

    /** Without this the icon has no glyph and stays invisible, even when it is "shown". */
    @Override
    protected Icon initContent() {
        return VaadinIcon.WARNING.create();
    }

    /** @param onSaveAnyway called with the AI's feedback when the user insists on saving */
    public ComplianceWarning(Consumer<String> onSaveAnyway) {
        Icon icon = getContent();
        icon.setVisible(false);
        icon.setColor("var(--aura-orange-text)");
        icon.setTooltipText("Compliance hint: click for details");

        popover.setTarget(icon);
        popover.setOpenOnClick(true);
        popover.add(new VerticalLayout(new H4("Compliance hint"), feedbackText,
                createButtons(onSaveAnyway)));
    }

    public void show(String feedback) {
        this.feedback = feedback;
        feedbackText.setText(feedback);
        getContent().setVisible(true);
    }

    /** The feedback belongs to the text that was checked, so it goes away when that text changes. */
    public void hide() {
        popover.close();
        getContent().setVisible(false);
    }

    private HorizontalLayout createButtons(Consumer<String> onSaveAnyway) {
        var close = new Button("Close", e -> popover.close());
        var saveAnyway = new Button("Save anyway", e -> {
            popover.close();
            onSaveAnyway.accept(feedback);
        });
        saveAnyway.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.TERTIARY);
        return new HorizontalLayout(close, saveAnyway);
    }
}
