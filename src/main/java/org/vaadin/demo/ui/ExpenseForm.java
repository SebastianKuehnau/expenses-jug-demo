package org.vaadin.demo.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.PageRegion;
import com.vaadin.flow.component.ai.common.Rect;
import com.vaadin.flow.component.ai.common.SourceExtract;
import com.vaadin.flow.component.ai.form.FieldValueChangeEvent;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.vaadin.demo.compliance.ComplianceResult;
import org.vaadin.demo.compliance.ComplianceService;
import org.vaadin.demo.expense.Expense;
import org.vaadin.demo.expense.ExpenseCategory;
import org.vaadin.demo.expense.ExpenseService;
import org.vaadin.demo.expense.ReceiptFile;
import org.vaadin.demo.receipt.ReceiptFormAi;

/** Edit form for one expense. Tells its parent via {@code onDone} when the user is finished. */
public class ExpenseForm extends HorizontalLayout {

    private final DatePicker date = new DatePicker("Date");
    private final TextField merchant = new TextField("Merchant");
    private final ComboBox<ExpenseCategory> category = new ComboBox<>("Category");
    private final BigDecimalField amount = new BigDecimalField("Amount");
    private final TextField currency = new TextField("Currency");
    private final TextArea description = new TextArea("Description");
    private final FormLayout fields = new FormLayout();
    private final Button save = new Button("Save", e -> checkAndSave());
    private final Button delete = new Button("Delete", e -> confirmDelete());
    private final ComboBox<String> chatReceipt = new ComboBox<>("Receipt from chat");
    private final ReceiptPreview preview = new ReceiptPreview();
    private final ComplianceWarning warning = new ComplianceWarning(this::saveAnyway);

    /**
     * The Binder connects the fields to the bean. BeanValidationBinder reads the
     * validation annotations on Expense, so we don't repeat the rules here.
     */
    private final BeanValidationBinder<Expense> binder = new BeanValidationBinder<>(Expense.class);

    private final ReceiptUpload receiptUpload;
    private final FormAIController filler;
    private final AIOrchestrator orchestrator;

    private final ExpenseService service;
    private final ComplianceService compliance;
    private final Runnable onDone;
    private Expense expense;
    /** The file uploaded while this form is open; saved together with the expense. */
    private ReceiptFile uploadedReceipt;

    public ExpenseForm(ExpenseService service, ComplianceService compliance,
            ReceiptFormAi receiptAi, Runnable onDone) {
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
        receiptUpload = new ReceiptUpload(receiptAi.isAvailable(), this::receiptUploaded);
        var fieldLayout = createFields();
        filler = createFiller();
        orchestrator = receiptAi.createOrchestrator(filler, event -> scanFinished());
        var formColumn = new VerticalLayout(receiptUpload, chatReceipt, fieldLayout, createButtons());
        formColumn.setPadding(false);
        formColumn.setWidth("24rem");
        // The uploaded receipt appears to the right of the fields, so it can be checked against them.
        add(formColumn, preview);
        chatReceipt.addValueChangeListener(e -> attachFromChat(e.getValue()));
        showSourceOnFocus(date);
        showSourceOnFocus(merchant);
        showSourceOnFocus(category);
        showSourceOnFocus(amount);
        showSourceOnFocus(currency);
        showSourceOnFocus(description);
    }

    /** The AI works in the background: come back to the UI thread before touching components. */
    private void scanFinished() {
        getUI().ifPresent(ui -> ui.access(() -> receiptUpload.setScanning(false)));
    }

    /** Click into a field and the receipt shows where the AI read that value (images only). */
    private <C extends Component & Focusable<C> & HasValue<?, ?>> void showSourceOnFocus(C field) {
        field.addFocusListener(e -> preview.highlight(placesOnReceipt(field)));
    }

    private List<Rect> placesOnReceipt(HasValue<?, ?> field) {
        return filler.getFieldSource(field).stream()
                .flatMap(source -> source.extracts().stream())
                .map(SourceExtract::location)
                .filter(PageRegion.class::isInstance)
                .map(location -> ((PageRegion) location).rect())
                .toList();
    }

    /** Shows the given expense; a new, unsaved one means "create". */
    public void edit(Expense expense) {
        this.expense = expense;
        binder.readBean(expense);
        warning.hide();
        uploadedReceipt = null;
        preview.highlight(List.of());
        offerChatReceipts();
        showSavedReceipt(expense);
        delete.setVisible(expense.getId() != null);
    }

    private void showSavedReceipt(Expense expense) {
        preview.clear();
        if (expense.getId() != null) {
            service.findReceipt(expense.getId())
                    .ifPresent(file -> preview.show(file.getData(), file.getContentType()));
        }
    }

    /** Receipts attached in the chat can be picked here; the field only shows up if there are any. */
    private void offerChatReceipts() {
        chatReceipt.clear();
        chatReceipt.setItems(ChatReceipts.of(UI.getCurrent()).fileNames());
        chatReceipt.setVisible(!ChatReceipts.of(UI.getCurrent()).fileNames().isEmpty());
    }

    private void attachFromChat(String fileName) {
        if (fileName == null) {
            return;
        }
        ChatReceipts.of(UI.getCurrent()).find(fileName).ifPresent(file -> {
            uploadedReceipt = file;
            preview.show(file.getData(), file.getContentType());
        });
    }

    /** The AI reads the receipt and fills the fields; the form shows where each value came from. */
    private void receiptUploaded(ReceiptUpload.UploadedFile file) {
        uploadedReceipt = new ReceiptFile(null, file.mimeType(), file.data());
        preview.show(file.data(), file.mimeType());
        receiptUpload.setScanning(true);
        orchestrator.prompt("Fill in the form from the attached receipt.",
                List.of(new AIAttachment(file.name(), file.mimeType(), file.data())));
    }

    /**
     * Vaadin's AI Form Filler. It marks every field the AI wrote with an "AI" badge. With source
     * tracking on, the badge's popover also shows what the AI read on the receipt for that value.
     */
    private FormAIController createFiller() {
        var controller = new FormAIController(fields, binder);
        controller.setSourceTrackingEnabled(true);
        controller.setFieldMarkerPopoverContentProvider(this::describeSource);
        return controller;
    }

    private Component describeSource(FieldValueChangeEvent change) {
        return change.getFieldSource().<Component>map(source -> {
            var content = new Div();
            if (source.confidence() != null) {
                content.add(new Paragraph("Confidence: " + source.confidence().name().toLowerCase()));
            }
            source.extracts().forEach(extract -> content.add(new Paragraph("On the receipt: “" + extract.text() + "”")));
            return content;
        }).orElse(null);
    }

    private FormLayout createFields() {
        fields.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        fields.add(date, merchant, category, amount, currency, description);
        return fields;
    }

    private HorizontalLayout createButtons() {
        save.addThemeVariants(ButtonVariant.PRIMARY);
        delete.addThemeVariants(ButtonVariant.ERROR);
        return new HorizontalLayout(save, delete, new Button("Cancel", e -> onDone.run()));
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
            Notification.show("The compliance check failed. Please try again.");
        } else if (result.compliant()) {
            saveExpense(false, null);
        } else {
            warning.show(result.feedback());
        }
    }

    /** "Save anyway": no new check, but we remember that the warning was ignored. */
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
        service.save(expense, uploadedReceipt);
        Notification.show("Expense saved");
        onDone.run();
    }

    private void setChecking(boolean checking) {
        fields.setEnabled(!checking);
        save.setEnabled(!checking);
        save.setText(checking ? "Checking…" : "Save");
    }

    private void confirmDelete() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Delete expense?");
        dialog.setText("This expense will be deleted permanently.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.addConfirmListener(e -> {
            service.delete(expense);
            Notification.show("Expense deleted");
            onDone.run();
        });
        dialog.open();
    }
}
