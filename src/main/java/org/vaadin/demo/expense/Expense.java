package org.vaadin.demo.expense;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One expense receipt. The Bean Validation annotations are used twice: by JPA on save
 * and by the Vaadin form (BeanValidationBinder), so the rules live in one place.
 */
@Entity
public class Expense {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Required")
    @PastOrPresent(message = "The date must not be in the future")
    private LocalDate date;

    @NotBlank(message = "Required")
    private String merchant;

    @NotBlank(message = "Required")
    @Size(max = 500, message = "Maximal 500 Zeichen")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Required")
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @NotNull(message = "Required")
    @DecimalMin(value = "0", inclusive = false, message = "The amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Required")
    private String currency = "EUR";

    /** true = the compliance warning was ignored ("Save anyway"). */
    private boolean complianceOverridden;

    private String complianceNote;

    public Long getId() { return id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isComplianceOverridden() { return complianceOverridden; }
    public void setComplianceOverridden(boolean complianceOverridden) { this.complianceOverridden = complianceOverridden; }

    public String getComplianceNote() { return complianceNote; }
    public void setComplianceNote(String complianceNote) { this.complianceNote = complianceNote; }
}
