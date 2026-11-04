package org.vaadin.demo.ui;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.vaadin.demo.expense.ReceiptFile;

/**
 * The receipts the user attached in the chat during this browser session. The expense form offers
 * them in "Receipt from chat", so a receipt can be attached to the expense it belongs to.
 */
public final class ChatReceipts {

    private record Entry(String fileName, ReceiptFile file) {
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    private ChatReceipts() {
    }

    /** The receipts of this UI; created on first use. */
    public static ChatReceipts of(UI ui) {
        var receipts = ComponentUtil.getData(ui, ChatReceipts.class);
        if (receipts == null) {
            receipts = new ChatReceipts();
            ComponentUtil.setData(ui, ChatReceipts.class, receipts);
        }
        return receipts;
    }

    /** A file with the same name replaces the earlier one. */
    public void add(String fileName, String contentType, byte[] data) {
        entries.removeIf(entry -> entry.fileName().equals(fileName));
        entries.add(new Entry(fileName, new ReceiptFile(null, contentType, data)));
    }

    public List<String> fileNames() {
        return entries.stream().map(Entry::fileName).toList();
    }

    public Optional<ReceiptFile> find(String fileName) {
        return entries.stream().filter(entry -> entry.fileName().equals(fileName))
                .map(Entry::file).findFirst();
    }

    public void clear() {
        entries.clear();
    }
}
