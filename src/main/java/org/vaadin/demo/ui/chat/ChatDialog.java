package org.vaadin.demo.ui.chat;

import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.Attachment;
import com.vaadin.draiv.ai.presenter.TextPresenterService.CancelSignal;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnEvent;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnResult;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.ai.chat.messages.Message;
import org.vaadin.demo.ai.AppContext;
import org.vaadin.demo.ai.TurnStatus;

/**
 * The assistant window: a dialog that is not modal, so the user can keep using the app behind it.
 * It belongs to the MainLayout, so it stays open (with its history) while navigating.
 */
public class ChatDialog extends Dialog {

    private static final String GREETING = "Hallo! Sag mir, was ich in der Expense-App für dich tun soll.";

    private final TextPresenterService presenter;
    private final ChatLog log = new ChatLog();
    private final ChatAttachments attachments = new ChatAttachments();
    private final MessageInput input = new MessageInput(e -> startTurn(e.getValue()));
    private final Button stop = new Button("Stopp", VaadinIcon.STOP.create(), e -> cancelTurn());
    private final Button clear = new Button(VaadinIcon.ERASER.create(), e -> clearHistory());
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            runnable -> Thread.ofPlatform().daemon().name("chat").unstarted(runnable));

    private List<Message> history = new ArrayList<>();
    private CancelSignal currentTurn;

    public ChatDialog(TextPresenterService presenter) {
        this.presenter = presenter;
        configureWindow();
        add(createContent());
        log.addAssistant(GREETING);
    }

    public void toggle() {
        if (isOpened()) {
            close();
        } else {
            open();
        }
    }

    public void shutdown() {
        close();
        executor.shutdownNow();
    }

    private void configureWindow() {
        setModal(false);
        setDraggable(true);
        setResizable(true);
        setWidth("420px");
        setHeight("560px");
        // Start in the bottom right corner (top/left take any CSS length).
        setTop("calc(100vh - 600px)");
        setLeft("calc(100vw - 460px)");
        setHeaderTitle("Assistent");
        clear.setTooltipText("Verlauf löschen");
        clear.setAriaLabel("Verlauf löschen");
        var close = new Button(VaadinIcon.CLOSE.create(), e -> close());
        close.setAriaLabel("Schließen");
        for (Button button : List.of(clear, close)) {
            button.addThemeVariants(ButtonVariant.TERTIARY);
            getHeader().add(button);
        }
    }

    private VerticalLayout createContent() {
        stop.setVisible(false);
        var content = new VerticalLayout(log, stop, attachments.component(), input);
        content.setSizeFull();
        content.setPadding(false);
        return content;
    }

    private void startTurn(String text) {
        List<Attachment> files = attachments.takeAll();
        log.addUser(text, files.stream().map(Attachment::filename).toList());
        MessageListItem thinking = log.addStatus("Denke nach…");
        setTurnRunning(true);

        // Read on the UI thread: the background thread below has no UI of its own.
        UI ui = UI.getCurrent();
        String principal = DemoPrincipal.forUi(ui);
        List<Message> prior = AppContext.withAppContext(history);
        CancelSignal cancel = new CancelSignal();
        currentTurn = cancel;
        executor.submit(() -> runTurn(ui, principal, prior, text, files, thinking, cancel));
    }

    /**
     * Runs on a background thread, because the AI needs several seconds. It changes the UI by
     * itself (navigates, fills fields). Those changes reach the browser because the AI uses
     * {@code UI.access} and the app has {@code @Push}.
     */
    private void runTurn(UI ui, String principal, List<Message> prior, String text,
            List<Attachment> files, MessageListItem thinking, CancelSignal cancel) {
        try {
            TurnResult result = presenter.respond(principal, prior, text, files,
                    event -> showStatus(ui, thinking, event), null, null, cancel);
            access(ui, () -> finishTurn(thinking, result));
        } catch (RuntimeException e) {
            access(ui, () -> failTurn(thinking, e));
        }
    }

    private void showStatus(UI ui, MessageListItem thinking, TurnEvent event) {
        TurnStatus.line(event).ifPresent(line -> access(ui, () -> thinking.setText(line)));
    }

    private void finishTurn(MessageListItem thinking, TurnResult result) {
        log.remove(thinking);
        setTurnRunning(false);
        history = new ArrayList<>(result.conversationItems());
        if (result.text() != null && !result.text().isBlank()) {
            log.addAssistant(result.text());
        }
    }

    private void failTurn(MessageListItem thinking, RuntimeException e) {
        log.remove(thinking);
        setTurnRunning(false);
        log.addError(e.getMessage() == null ? "Der Assistent ist fehlgeschlagen." : e.getMessage());
    }

    private void cancelTurn() {
        if (currentTurn != null) {
            currentTurn.cancel();
        }
    }

    private void clearHistory() {
        history = new ArrayList<>();
        attachments.clear();
        log.clear();
        log.addAssistant(GREETING);
    }

    private void setTurnRunning(boolean running) {
        input.setEnabled(!running);
        attachments.component().setEnabled(!running);
        clear.setEnabled(!running);
        stop.setVisible(running);
    }

    private void access(UI ui, Runnable change) {
        try {
            ui.access(change::run);
        } catch (UIDetachedException e) {
            // The user closed the tab during the turn; nothing left to update.
        }
    }
}
