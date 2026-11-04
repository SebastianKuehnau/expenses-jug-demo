package org.vaadin.demo.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import java.util.function.Consumer;

/** "Scan receipt": drop a receipt; the file goes to {@code onUploaded}, which lets the AI read it. */
public class ReceiptUpload extends Composite<VerticalLayout> {

    /** The file as the browser sent it. */
    public record UploadedFile(String name, String mimeType, byte[] data) {
    }

    private final Upload upload;
    private final ProgressBar progress = new ProgressBar();

    public ReceiptUpload(boolean aiAvailable, Consumer<UploadedFile> onUploaded) {
        // inMemory: the handler gets the whole file as byte[] (on the UI thread).
        this.upload = new Upload(UploadHandler.inMemory((metadata, data) ->
                onUploaded.accept(new UploadedFile(metadata.fileName(), metadata.contentType(), data))));
        upload.setAcceptedFileTypes("image/*", "application/pdf");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Scan receipt"));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        if (!aiAvailable) {
            upload.setEnabled(false);
            upload.getElement().setAttribute("title", "No OPENAI_API_KEY configured");
        }
        VerticalLayout layout = getContent();
        layout.setPadding(false);
        layout.add(upload, progress);
    }

    /** While the AI reads the receipt: show the progress bar and block further uploads. */
    public void setScanning(boolean scanning) {
        if (!scanning) {
            upload.clearFileList();
        }
        upload.setEnabled(!scanning);
        progress.setVisible(scanning);
    }
}
