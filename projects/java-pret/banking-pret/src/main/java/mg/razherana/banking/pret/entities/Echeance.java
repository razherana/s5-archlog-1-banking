package mg.razherana.banking.pret.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Echeance entity representing loan payments in the banking system.
 * 
 * <p>
 * This entity stores individual payment records for loan accounts.
 * Each payment made creates one echeance record.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "echeances")
public class Echeance implements Serializable {

  /**
   * Unique identifier for the payment.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Reference to the loan account.
   */
  @Column(name = "compte_id", nullable = false)
  private Integer compteId;

  /**
   * Payment amount.
   */
  @Column(name = "montant", nullable = false, precision = 15, scale = 2)
  private BigDecimal montant;

  /**
   * Payment date (when the payment was made).
   */
  @Column(name = "date_echeance", nullable = false)
  private LocalDateTime dateEcheance;

  /**
   * Default constructor for JPA.
   */
  public Echeance() {
  }

  /**
   * Constructor with payment information.
   * 
   * @param compteId     the loan account ID
   * @param montant      the payment amount
   * @param dateEcheance the payment date
   */
  public Echeance(Integer compteId, BigDecimal montant, LocalDateTime dateEcheance) {
    this.compteId = compteId;
    this.montant = montant;
    this.dateEcheance = dateEcheance;
  }

  /**
   * Gets the unique identifier of the payment.
   * 
   * @return the payment ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the payment.
   * 
   * @param id the payment ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the loan account ID.
   * 
   * @return the loan account ID
   */
  public Integer getCompteId() {
    return compteId;
  }

  /**
   * Sets the loan account ID.
   * 
   * @param compteId the loan account ID to set
   */
  public void setCompteId(Integer compteId) {
    this.compteId = compteId;
  }

  /**
   * Gets the payment amount.
   * 
   * @return the payment amount
   */
  public BigDecimal getMontant() {
    return montant;
  }

  /**
   * Sets the payment amount.
   * 
   * @param montant the payment amount to set
   */
  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  /**
   * Gets the payment date.
   * 
   * @return the payment date
   */
  public LocalDateTime getDateEcheance() {
    return dateEcheance;
  }

  /**
   * Sets the payment date.
   * 
   * @param dateEcheance the payment date to set
   */
  public void setDateEcheance(LocalDateTime dateEcheance) {
    this.dateEcheance = dateEcheance;
  }

  /**
   * Returns a string representation of the payment.
   * 
   * @return a string representation containing payment details
   */
  @Override
  public String toString() {
    return "Echeance{" +
        "id=" + id +
        ", compteId=" + compteId +
        ", montant=" + montant +
        ", dateEcheance=" + dateEcheance +
        '}';
  }
}