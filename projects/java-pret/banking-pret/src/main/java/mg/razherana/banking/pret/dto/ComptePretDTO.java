package mg.razherana.banking.pret.dto;

import mg.razherana.banking.pret.entities.ComptePret;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ComptePret entity.
 * 
 * <p>
 * This DTO is used for transferring loan account data through REST API
 * endpoints.
 * </p>
 */
public class ComptePretDTO implements Serializable {

  private Integer id;
  private Integer userId;
  private Integer typeComptePretId;
  private BigDecimal montant;
  private LocalDateTime dateDebut;
  private LocalDateTime dateFin;
  private BigDecimal monthlyPayment;

  // Default constructor
  public ComptePretDTO() {
  }

  // Constructor from ComptePret entity
  public ComptePretDTO(ComptePret compte) {
    this.id = compte.getId();
    this.userId = compte.getUserId();
    this.typeComptePretId = compte.getTypeComptePretId();
    this.montant = compte.getMontant();
    this.dateDebut = compte.getDateDebut();
    this.dateFin = compte.getDateFin();
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

  @Override
  public String toString() {
    return "ComptePretDTO{" +
        "id=" + id +
        ", userId=" + userId +
        ", typeComptePretId=" + typeComptePretId +
        ", montant=" + montant +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        '}';
  }

  public BigDecimal getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(BigDecimal monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }
}