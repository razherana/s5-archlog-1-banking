package mg.razherana.banking.pret.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new loan account.
 */
public class CreateComptePretRequest {

  private Integer userId;
  private Integer typeComptePretId;
  private BigDecimal montant;
  private LocalDateTime dateDebut;
  private LocalDateTime dateFin;

  // Default constructor
  public CreateComptePretRequest() {
  }

  // Getters and setters
  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getTypeComptePretId() {
    return typeComptePretId;
  }

  public void setTypeComptePretId(Integer typeComptePretId) {
    this.typeComptePretId = typeComptePretId;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public LocalDateTime getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(LocalDateTime dateDebut) {
    this.dateDebut = dateDebut;
  }

  public LocalDateTime getDateFin() {
    return dateFin;
  }

  public void setDateFin(LocalDateTime dateFin) {
    this.dateFin = dateFin;
  }

  @Override
  public String toString() {
    return "CreateComptePretRequest{" +
        "userId=" + userId +
        ", typeComptePretId=" + typeComptePretId +
        ", montant=" + montant +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        '}';
  }
}