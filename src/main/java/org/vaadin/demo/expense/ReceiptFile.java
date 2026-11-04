package org.vaadin.demo.expense;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * The uploaded receipt (image or PDF) of one expense. It is a separate entity, so listing
 * expenses in the Grid never loads the file contents.
 */
@Entity
public class ReceiptFile {

    @Id
    @GeneratedValue
    private Long id;

    private Long expenseId;

    private String contentType;

    @Lob
    private byte[] data;

    protected ReceiptFile() {
    }

    public ReceiptFile(Long expenseId, String contentType, byte[] data) {
        this.expenseId = expenseId;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getExpenseId() { return expenseId; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
}
