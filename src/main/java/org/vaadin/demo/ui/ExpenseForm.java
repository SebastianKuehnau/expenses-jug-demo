package org.vaadin.demo.ui;

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
    private final Button delete = new Button("Löschen", e -> confirmDelete());

    /**
     * The Binder connects the fields to the bean. BeanValidationBinder reads the
     * validation annotations on Expense, so we don't repeat the rules here.
     */
    private final BeanValidationBinder<Expense> binder = new BeanValidationBinder<>(Expense.class);

    private final ExpenseService service;
    private final Runnable onDone;
    private Expense expense;

    public ExpenseForm(ExpenseService service, Runnable onDone) {
        this.service = service;
        this.onDone = onDone;
        category.setItems(ExpenseCategory.values());
        category.setItemLabelGenerator(ExpenseCategory::getLabel);
        binder.bindInstanceFields(this);
        add(createFields(), createButtons());
    }

    /** Shows the given expense; a new, unsaved one means "create". */
    public void edit(Expense expense) {
        this.expense = expense;
        binder.readBean(expense);
        delete.setVisible(expense.getId() != null);
    }

    private FormLayout createFields() {
        var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        description.setMaxLength(500);
        form.add(date, merchant, category, amount, currency, description);
        return form;
    }

    private HorizontalLayout createButtons() {
        var save = new Button("Speichern", e -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);
        delete.addThemeVariants(ButtonVariant.ERROR);
        return new HorizontalLayout(save, delete, new Button("Abbrechen", e -> onDone.run()));
    }

    private void save() {
        try {
            binder.writeBean(expense);
        } catch (ValidationException e) {
            return; // the binder already shows the error messages next to the fields
        }
        service.save(expense);
        Notification.show("Ausgabe gespeichert");
        onDone.run();
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
