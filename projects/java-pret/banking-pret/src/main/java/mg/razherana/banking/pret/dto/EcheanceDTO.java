package mg.razherana.banking.pret.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for echeance (payment record) data.
 */
public class EcheanceDTO {

  private Integer id;
  private Integer compteId;
  private BigDecimal montant;
  private LocalDateTime dateEcheance;

  // Default constructor
  public EcheanceDTO() {
  }

  // Constructor from Echeance entity
  public EcheanceDTO(Integer id, Integer compteId, BigDecimal montant, LocalDateTime dateEcheance) {
    this.id = id;
    this.compteId = compteId;
    this.montant = montant;
    this.dateEcheance = dateEcheance;
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

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

  public LocalDateTime getDateEcheance() {
    return dateEcheance;
  }

  public void setDateEcheance(LocalDateTime dateEcheance) {
    this.dateEcheance = dateEcheance;
  }

  @Override
  public String toString() {
    return "EcheanceDTO{" +
        "id=" + id +
        ", compteId=" + compteId +
        ", montant=" + montant +
        ", dateEcheance=" + dateEcheance +
        '}';
  }
}