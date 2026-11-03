package org.vaadin.demo.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.concurrent.CompletableFuture;
import org.vaadin.demo.compliance.ComplianceResult;
import org.vaadin.demo.compliance.ComplianceService;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseService;

/** Edit form for one expense. Tells its parent via {@code onDone} when the user is finished. */
public class ExpenseForm extends VerticalLayout {

    private final DatePicker date = new DatePicker("Datum");
    private final TextField merchant = new TextField("Händler");
    private final ComboBox<ExpenseCategory> category = new ComboBox<>("Kategorie");
    private final BigDecimalField amount = new BigDecimalField("Betrag");
    private final TextField currency = new TextField("Währung");
    private final TextArea description = new TextArea("Beschreibung");
    private final FormLayout fields = new FormLayout();
    private final Button save = new Button("Speichern", e -> checkAndSave());
    private final Button delete = new Button("Löschen", e -> confirmDelete());
    private final ComplianceWarning warning = new ComplianceWarning(this::saveAnyway);

    /**
     * The Binder connects the fields to the bean. BeanValidationBinder reads the
     * validation annotations on Expense, so we don't repeat the rules here.
     */
    private final BeanValidationBinder<Expense> binder = new BeanValidationBinder<>(Expense.class);

    private final ExpenseService service;
    private final ComplianceService compliance;
    private final Runnable onDone;
    private Expense expense;

    public ExpenseForm(ExpenseService service, ComplianceService compliance, Runnable onDone) {
        this.service = service;
        this.compliance = compliance;
        this.onDone = onDone;
        category.setItems(ExpenseCategory.values());
        category.setItemLabelGenerator(ExpenseCategory::getLabel);
        description.setMaxLength(500);
        description.setSuffixComponent(warning);
        // The AI's feedback was about the old text: once the text changes, it no longer applies.
        description.addValueChangeListener(e -> warning.hide());
        binder.bindInstanceFields(this);
        add(createFields(), createButtons());
    }

    /** Shows the given expense; a new, unsaved one means "create". */
    public void edit(Expense expense) {
        this.expense = expense;
        binder.readBean(expense);
        warning.hide();
        delete.setVisible(expense.getId() != null);
    }

    private FormLayout createFields() {
        fields.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        fields.add(date, merchant, category, amount, currency, description);
        return fields;
    }

    private HorizontalLayout createButtons() {
        save.addThemeVariants(ButtonVariant.PRIMARY);
        delete.addThemeVariants(ButtonVariant.ERROR);
        return new HorizontalLayout(save, delete, new Button("Abbrechen", e -> onDone.run()));
    }

    /** Validate, then let the AI check the rules. Only a compliant expense is saved right away. */
    private void checkAndSave() {
        var candidate = new Expense();
        try {
            binder.writeBean(candidate); // the binder shows field errors; nothing is saved yet
        } catch (ValidationException e) {
            return;
        }
        setChecking(true);
        // The AI call takes seconds, so it runs in the background. UI.access brings the result
        // back to the UI thread (and @Push sends it to the browser).
        UI ui = UI.getCurrent();
        CompletableFuture.supplyAsync(() -> compliance.check(candidate))
                .whenComplete((result, error) -> ui.access(() -> afterCheck(result, error)));
    }

    private void afterCheck(ComplianceResult result, Throwable error) {
        setChecking(false);
        if (error != null) {
            Notification.show("Die Compliance-Prüfung ist fehlgeschlagen. Bitte noch einmal versuchen.");
        } else if (result.compliant()) {
            saveExpense(false, null);
        } else {
            warning.show(result.feedback());
        }
    }

    /** "Trotzdem speichern": no new check, but we remember that the warning was ignored. */
    private void saveAnyway(String feedback) {
        saveExpense(true, feedback);
    }

    private void saveExpense(boolean overridden, String note) {
        try {
            binder.writeBean(expense);
        } catch (ValidationException e) {
            return;
        }
        expense.setComplianceOverridden(overridden);
        expense.setComplianceNote(note);
        service.save(expense);
        Notification.show("Ausgabe gespeichert");
        onDone.run();
    }

    private void setChecking(boolean checking) {
        fields.setEnabled(!checking);
        save.setEnabled(!checking);
        save.setText(checking ? "Prüfe…" : "Speichern");
    }

    private void confirmDelete() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Ausgabe löschen?");
        dialog.setText("Diese Ausgabe wird endgültig gelöscht.");
        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");
        dialog.setConfirmText("Löschen");
        dialog.addConfirmListener(e -> {
            service.delete(expense);
            Notification.show("Ausgabe gelöscht");
            onDone.run();
        });
        dialog.open();
    }
}
