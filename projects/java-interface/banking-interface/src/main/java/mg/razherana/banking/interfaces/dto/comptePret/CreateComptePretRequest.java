package mg.razherana.banking.interfaces.dto.comptePret;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateComptePretRequest {
  private Integer userId;
  private Integer typeComptePretId;
  private BigDecimal montant;
  private LocalDateTime dateDebut;
  private LocalDateTime dateFin;
  private Integer compteCourantId; // The current account to deposit the loan amount

  // Default constructor
  public CreateComptePretRequest() {
  }

  // Constructor with parameters
  public CreateComptePretRequest(Integer userId, Integer typeComptePretId, BigDecimal montant,
      LocalDateTime dateDebut, LocalDateTime dateFin, Integer compteCourantId) {
    this.userId = userId;
    this.typeComptePretId = typeComptePretId;
    this.montant = montant;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.compteCourantId = compteCourantId;
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

  public Integer getCompteCourantId() {
    return compteCourantId;
  }

  public void setCompteCourantId(Integer compteCourantId) {
    this.compteCourantId = compteCourantId;
  }

  @Override
  public String toString() {
    return "CreateComptePretRequest{" +
        "userId=" + userId +
        ", typeComptePretId=" + typeComptePretId +
        ", montant=" + montant +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        ", compteCourantId=" + compteCourantId +
        '}';
  }
}