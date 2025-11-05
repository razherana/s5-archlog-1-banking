package mg.razherana.banking.interfaces.dto.comptePret;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MakePaymentRequest {
  private Integer compteId;
  private BigDecimal montant;
  private LocalDateTime actionDateTime;

  // Default constructor
  public MakePaymentRequest() {
  }

  // Constructor with parameters
  public MakePaymentRequest(Integer compteId, BigDecimal montant, LocalDateTime actionDateTime) {
    this.compteId = compteId;
    this.montant = montant;
    this.actionDateTime = actionDateTime;
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