package mg.razherana.banking.interfaces.dto.comptePret;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EcheanceDTO {
  @JsonProperty("id")
  private Integer id;

  @JsonProperty("compteId")
  private Integer compteId;

  @JsonProperty("montant")
  private BigDecimal montant;

  @JsonProperty("dateEcheance")
  private LocalDateTime dateEcheance;

  // Default constructor
  public EcheanceDTO() {
  }

  // Constructor with parameters
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

  // Helper methods
  public String getFormattedMontant() {
    if (montant != null) {
      return montant.toString() + " MGA";
    }
    return "0.00 MGA";
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