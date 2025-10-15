package mg.razherana.banking.courant.dto.requests.transactions;

import java.math.BigDecimal;

/**
 * Request DTO for deposit transactions.
 * 
 * <p>
 * Represents a deposit operation where external money is added to an account.
 * In the transaction model, deposits have no sender (external source) and the
 * specified account as receiver.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.api.TransactionResource#depot(DepotRequest)
 * @see mg.razherana.banking.courant.application.transactionService.TransactionService#depot(Integer,
 *      BigDecimal)
 */
public class DepotRequest {
  /** ID of the account to receive the deposit */
  private Integer compteId;

  /** Amount to deposit (must be positive) */
  private BigDecimal montant;

  /** Optional description for the deposit */
  private String description;

  /**
   * Gets the ID of the account to receive the deposit.
   * 
   * @return the account ID
   */
  public Integer getCompteId() {
    return compteId;
  }

  /**
   * Sets the ID of the account to receive the deposit.
   * 
   * @param compteId the account ID
   */
  public void setCompteId(Integer compteId) {
    this.compteId = compteId;
  }

  /**
   * Gets the amount to deposit.
   * 
   * @return the deposit amount
   */
  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
