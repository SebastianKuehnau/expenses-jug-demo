package org.vaadin.demo.ui.chat;

import com.vaadin.draiv.ai.presenter.TextPresenterService.Attachment;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Drag-and-drop upload for the chat. Collects the files until the next message is sent. */
class ChatAttachments {

    private final List<Attachment> pending = new ArrayList<>();
    private final Upload upload;

    ChatAttachments() {
        // inMemory: the handler gets the whole file as byte[]; fine for receipts (max 20 MB).
        upload = new Upload(UploadHandler.inMemory((metadata, data) -> pending.add(new Attachment(
                metadata.fileName(), metadata.contentType(),
                Base64.getEncoder().encodeToString(data)))));
        upload.setAcceptedFileTypes("image/*", "application/pdf");
        upload.setMaxFiles(5);
        upload.setMaxFileSize(20 * 1024 * 1024);
        upload.setWidthFull();
        upload.setDropLabel(new Span("Belege hier ablegen (Bild oder PDF)"));
        upload.setDropLabelIcon(new Icon(VaadinIcon.CLOUD_UPLOAD_O));
        // Removing a file chip in the upload also removes the attachment.
        upload.addFileRemovedListener(e -> pending.removeIf(a -> a.filename().equals(e.getFileName())));
    }

    Upload component() {
        return upload;
    }

    /** The files collected so far; the upload starts empty again. */
    List<Attachment> takeAll() {
        List<Attachment> files = List.copyOf(pending);
        pending.clear();
        upload.clearFileList();
        return files;
    }

    void clear() {
        takeAll();
    }
}
