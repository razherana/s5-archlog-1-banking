package mg.razherana.banking.courant.dto.requests;

import java.math.BigDecimal;

/**
 * Request DTO for updating the monthly tax amount of a current account.
 * 
 * <p>Used to modify the monthly tax rate that is charged to an account.
 * The new tax amount will be used for calculating future tax obligations.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.api.CompteCourantResource#updateTaxe(Integer, UpdateTaxeRequest)
 * @see mg.razherana.banking.courant.application.CompteCourantService#updateTaxe(Integer, BigDecimal)
 */
public class UpdateTaxeRequest {
    /** New monthly tax amount (must be non-negative) */
    private BigDecimal taxe;

    /**
     * Default constructor.
     */
    public UpdateTaxeRequest() {}

    /**
     * Constructor with tax amount.
     * 
     * @param taxe the new monthly tax amount
     */
    public UpdateTaxeRequest(BigDecimal taxe) {
        this.taxe = taxe;
    }

    /**
     * Gets the new monthly tax amount.
     * 
     * @return the monthly tax amount
     */
    public BigDecimal getTaxe() {
        return taxe;
    }

    public void setTaxe(BigDecimal taxe) {
        this.taxe = taxe;
    }
}