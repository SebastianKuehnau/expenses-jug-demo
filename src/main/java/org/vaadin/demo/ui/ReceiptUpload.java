package org.vaadin.demo.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.vaadin.demo.receipt.ReceiptData;
import org.vaadin.demo.receipt.ReceiptExtractionService;

/** "Beleg scannen": drop a receipt, the AI reads it, the result goes to {@code onExtracted}. */
public class ReceiptUpload extends Composite<VerticalLayout> {

    private final ReceiptExtractionService extraction;
    private final Consumer<ReceiptData> onExtracted;
    private final Upload upload;
    private final ProgressBar progress = new ProgressBar();

    public ReceiptUpload(ReceiptExtractionService extraction, Consumer<ReceiptData> onExtracted) {
        this.extraction = extraction;
        this.onExtracted = onExtracted;
        // inMemory: the handler gets the whole file as byte[] (on the UI thread).
        this.upload = new Upload(UploadHandler.inMemory((metadata, data) ->
                scan(data, metadata.contentType())));
        upload.setAcceptedFileTypes("image/*", "application/pdf");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Beleg scannen"));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        enableIfAiIsConfigured();
        VerticalLayout layout = getContent();
        layout.setPadding(false);
        layout.add(upload, progress);
    }

    private void enableIfAiIsConfigured() {
        if (!extraction.isAvailable()) {
            upload.setEnabled(false);
            upload.getElement().setAttribute("title", "Kein OPENAI_API_KEY gesetzt");
        }
    }

    private void scan(byte[] data, String mimeType) {
        setScanning(true);
        UI ui = UI.getCurrent();
        // The AI call takes seconds: run it in the background, then come back with UI.access.
        CompletableFuture.supplyAsync(() -> extraction.extract(data, mimeType))
                .whenComplete((receipt, error) -> ui.access(() -> afterScan(receipt, error)));
    }

    private void afterScan(ReceiptData receipt, Throwable error) {
        setScanning(false);
        upload.clearFileList();
        if (error != null) {
            Notification.show("Der Beleg konnte nicht gelesen werden.");
        } else {
            onExtracted.accept(receipt);
        }
    }

    private void setScanning(boolean scanning) {
        upload.setEnabled(!scanning);
        progress.setVisible(scanning);
    }
}
