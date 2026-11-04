package org.vaadin.demo.ui.chat;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.messages.MessageListItemVariant;
import java.time.Instant;
import java.util.List;

/** The conversation: Vaadin's MessageList, with the user's messages on the right. */
class ChatLog extends MessageList {

    private static final String USER = "You";
    private static final String ASSISTANT = "Assistant";

    ChatLog() {
        setMarkdown(true); // assistant answers contain lists, bold text, ...
        setSizeFull();
    }

    void addUser(String text, List<String> fileNames) {
        var message = new MessageListItem(text, Instant.now(), USER);
        message.addThemeVariants(MessageListItemVariant.SELF);
        fileNames.forEach(name -> message.addAttachment(
                new MessageListItem.Attachment(name, "", null)));
        addItem(message);
    }

    void addAssistant(String markdown) {
        addItem(new MessageListItem(markdown, Instant.now(), ASSISTANT));
    }

    void addError(String message) {
        addItem(new MessageListItem("⚠️ " + message, Instant.now(), ASSISTANT));
    }

    /** A message whose text is replaced while the AI works. Remove it when the turn ends. */
    MessageListItem addStatus(String text) {
        var status = new MessageListItem(text, ASSISTANT);
        addItem(status);
        return status;
    }

    void remove(MessageListItem message) {
        setItems(getItems().stream().filter(item -> item != message).toList());
    }

    void clear() {
        setItems(List.of());
    }
}
