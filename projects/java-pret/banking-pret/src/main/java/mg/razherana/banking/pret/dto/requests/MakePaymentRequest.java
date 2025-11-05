package mg.razherana.banking.pret.dto.requests;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for making a loan payment.
 */
public class MakePaymentRequest implements Serializable {

  private Integer compteId;
  private BigDecimal montant;
  private LocalDateTime actionDateTime;

  // Default constructor
  public MakePaymentRequest() {
  }

  // Getters and setters
  public Integer getCompteId() {
    return compteId;
  }

  public void setCompteId(Integer compteId) {
    this.compteId = compteId;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public LocalDateTime getActionDateTime() {
    return actionDateTime;
  }

  public void setActionDateTime(LocalDateTime actionDateTime) {
    this.actionDateTime = actionDateTime;
  }

  @Override
  public String toString() {
    return "MakePaymentRequest{" +
        "compteId=" + compteId +
        ", montant=" + montant +
        ", actionDateTime=" + actionDateTime +
        '}';
  }
}