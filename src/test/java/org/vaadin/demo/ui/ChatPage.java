package org.vaadin.demo.ui;

import com.vaadin.browserless.BrowserlessUIContext;

/** Where things are in the chat: the navbar button and the assistant window. */
class ChatPage {

    private final BrowserlessUIContext window;

    ChatPage(BrowserlessUIContext window) {
        this.window = window;
    }

    boolean chatButtonIsEnabled() {
        return window.findButton().withText("Chat").component().isEnabled();
    }

    void clickChatButton() {
        window.findButton().withText("Chat").click();
    }

    /** Drops a file on the upload of the chat window. */
    void attach(String fileName, String contentType, byte[] data) {
        window.findUpload().upload(fileName, contentType, data);
    }

    void send(String message) {
        window.findMessageInput().send(message);
    }

    boolean chatIsOpen() {
        return window.findMessageList().exists();
    }

    boolean showsMessage(String text) {
        return window.findMessageList().getMessages().stream()
                .anyMatch(message -> message.getText().contains(text));
    }

    /** The AI works on another thread; give it a moment to push its answer into the UI. */
    boolean showsMessageEventually(String text) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            window.roundTrip();
            if (showsMessage(text)) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }
}
