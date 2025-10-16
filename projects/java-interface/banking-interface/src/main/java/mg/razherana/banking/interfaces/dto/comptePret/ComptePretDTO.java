package mg.razherana.banking.interfaces.dto.comptePret;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ComptePretDTO {
  @JsonProperty("id")
  private Integer id;

  @JsonProperty("userId")
  private Integer userId;

  @JsonProperty("typeComptePretId")
  private Integer typeComptePretId;

  @JsonProperty("montant")
  private BigDecimal montant;

  @JsonProperty("dateDebut")
  private LocalDateTime dateDebut;

  @JsonProperty("dateFin")
  private LocalDateTime dateFin;

  @JsonProperty("monthlyPayment")
  private BigDecimal monthlyPayment;

  // Type details (from join or separate call)
  private String typeNom;
  private BigDecimal typeInteret;

  // Default constructor
  public ComptePretDTO() {
  }

  // Constructor with parameters
  public ComptePretDTO(Integer id, Integer userId, Integer typeComptePretId, BigDecimal montant,
      LocalDateTime dateDebut, LocalDateTime dateFin, BigDecimal monthlyPayment) {
    this.id = id;
    this.userId = userId;
    this.typeComptePretId = typeComptePretId;
    this.montant = montant;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.monthlyPayment = monthlyPayment;
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

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

  public BigDecimal getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(BigDecimal monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }

  public String getTypeNom() {
    return typeNom;
  }

  public void setTypeNom(String typeNom) {
    this.typeNom = typeNom;
  }

  public BigDecimal getTypeInteret() {
    return typeInteret;
  }

  public void setTypeInteret(BigDecimal typeInteret) {
    this.typeInteret = typeInteret;
  }

  // Helper methods
  public String getFormattedMontant() {
    if (montant != null) {
      return montant.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  public String getFormattedMonthlyPayment() {
    if (monthlyPayment != null) {
      return monthlyPayment.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  @Override
  public String toString() {
    return "ComptePretDTO{" +
        "id=" + id +
        ", userId=" + userId +
        ", typeComptePretId=" + typeComptePretId +
        ", montant=" + montant +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        ", monthlyPayment=" + monthlyPayment +
        '}';
  }
}