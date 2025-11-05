package mg.razherana.banking.pret.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ComptePret entity representing loan accounts in the banking system.
 * 
 * <p>
 * This entity stores loan account information including amount, type, and
 * duration.
 * Loan accounts are linked to users and loan types.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "compte_prets")
public class ComptePret implements Serializable {

  /**
   * Unique identifier for the loan account.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Reference to the user who owns this loan account.
   * Links to the user management service.
   */
  @Column(name = "user_id", nullable = false)
  private Integer userId;

  /**
   * Reference to the loan type.
   */
  @Column(name = "type_compte_pret_id", nullable = false)
  private Integer typeComptePretId;

  /**
   * Loan amount (principal).
   */
  @Column(name = "montant", nullable = false, precision = 15, scale = 2)
  private BigDecimal montant;

  /**
   * Loan start date.
   */
  @Column(name = "date_debut", nullable = false)
  private LocalDateTime dateDebut;

  /**
   * Loan end date.
   */
  @Column(name = "date_fin", nullable = false)
  private LocalDateTime dateFin;

  /**
   * Default constructor for JPA.
   */
  public ComptePret() {
  }

  /**
   * Constructor with basic loan information.
   * 
   * @param userId           the ID of the user who owns this loan account
   * @param typeComptePretId the loan type ID
   * @param montant          the loan amount
   * @param dateDebut        the loan start date
   * @param dateFin          the loan end date
   */
  public ComptePret(Integer userId, Integer typeComptePretId, BigDecimal montant,
      LocalDateTime dateDebut, LocalDateTime dateFin) {
    this.userId = userId;
    this.typeComptePretId = typeComptePretId;
    this.montant = montant;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
  }

  /**
   * Gets the unique identifier of the loan account.
   * 
   * @return the loan account ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the loan account.
   * 
   * @param id the loan account ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the user ID associated with this loan account.
   * 
   * @return the user ID
   */
  public Integer getUserId() {
    return userId;
  }

  /**
   * Sets the user ID for this loan account.
   * 
   * @param userId the user ID to set
   */
  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  /**
   * Gets the loan type ID.
   * 
   * @return the loan type ID
   */
  public Integer getTypeComptePretId() {
    return typeComptePretId;
  }

  /**
   * Sets the loan type ID.
   * 
   * @param typeComptePretId the loan type ID to set
   */
  public void setTypeComptePretId(Integer typeComptePretId) {
    this.typeComptePretId = typeComptePretId;
  }

  /**
   * Gets the loan amount.
   * 
   * @return the loan amount
   */
  public BigDecimal getMontant() {
    return montant;
  }

  /**
   * Sets the loan amount.
   * 
   * @param montant the loan amount to set
   */
  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  /**
   * Gets the loan start date.
   * 
   * @return the loan start date
   */
  public LocalDateTime getDateDebut() {
    return dateDebut;
  }

  /**
   * Sets the loan start date.
   * 
   * @param dateDebut the loan start date to set
   */
  public void setDateDebut(LocalDateTime dateDebut) {
    this.dateDebut = dateDebut;
  }

  /**
   * Gets the loan end date.
   * 
   * @return the loan end date
   */
  public LocalDateTime getDateFin() {
    return dateFin;
  }

  /**
   * Sets the loan end date.
   * 
   * @param dateFin the loan end date to set
   */
  public void setDateFin(LocalDateTime dateFin) {
    this.dateFin = dateFin;
  }

  /**
   * Returns a string representation of the loan account.
   * 
   * @return a string representation containing loan details
   */
  @Override
  public String toString() {
    return "ComptePret{" +
        "id=" + id +
        ", userId=" + userId +
        ", typeComptePretId=" + typeComptePretId +
        ", montant=" + montant +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        '}';
  }
}