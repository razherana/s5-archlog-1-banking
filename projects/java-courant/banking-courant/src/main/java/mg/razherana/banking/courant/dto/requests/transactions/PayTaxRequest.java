package mg.razherana.banking.courant.dto.requests.transactions;

import java.time.LocalDateTime;

/**
 * Request DTO for tax payment transactions.
 * 
 * <p>Represents a tax payment operation where accumulated monthly taxes
 * are paid from an account. The tax amount is calculated automatically
 * based on the account's creation date and monthly tax rate.</p>
 * 
 * <p>Tax payments are required before performing withdrawals or transfers
 * on accounts that have unpaid taxes.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.api.TransactionResource#payTax(PayTaxRequest)
 * @see mg.razherana.banking.courant.application.TransactionService#payTax(Integer, LocalDateTime)
 */
public class PayTaxRequest {
    /** ID of the account paying taxes */
    private Integer compteId;
    
    /** Optional description for the tax payment */
    private String description;
    
    /** Date/time for tax calculation (defaults to current time if not provided) */
    private LocalDateTime actionDateTime;

    /**
     * Gets the ID of the account paying taxes.
     * 
     * @return the account ID
     */
    public Integer getCompteId() {
        return compteId;
    }

    /**
     * Sets the ID of the account paying taxes.
     * 
     * @param compteId the account ID
     */
    public void setCompteId(Integer compteId) {
        this.compteId = compteId;
    }

    /**
     * Gets the optional description for the tax payment.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActionDateTime() {
        return actionDateTime;
    }

    public void setActionDateTime(LocalDateTime actionDateTime) {
        this.actionDateTime = actionDateTime;
    }
}