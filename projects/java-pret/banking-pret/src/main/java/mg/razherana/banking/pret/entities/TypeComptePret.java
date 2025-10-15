package mg.razherana.banking.pret.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * TypeComptePret entity representing loan types in the banking system.
 * 
 * <p>
 * This entity stores loan type information including name and interest rate.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "type_compte_prets")
public class TypeComptePret {

  /**
   * Unique identifier for the loan type.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Name of the loan type (e.g., "etudiant", "immobilier").
   */
  @Column(name = "nom", nullable = false, length = 100)
  private String nom;

  /**
   * Annual interest rate as a decimal (e.g., 0.0500 for 5%).
   */
  @Column(name = "interet", nullable = false, precision = 5, scale = 4)
  private BigDecimal interet;

  /**
   * Default constructor for JPA.
   */
  public TypeComptePret() {
  }

  /**
   * Constructor with loan type information.
   * 
   * @param nom     the name of the loan type
   * @param interet the annual interest rate
   */
  public TypeComptePret(String nom, BigDecimal interet) {
    this.nom = nom;
    this.interet = interet;
  }

  /**
   * Gets the unique identifier of the loan type.
   * 
   * @return the loan type ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the loan type.
   * 
   * @param id the loan type ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the name of the loan type.
   * 
   * @return the loan type name
   */
  public String getNom() {
    return nom;
  }

  /**
   * Sets the name of the loan type.
   * 
   * @param nom the loan type name to set
   */
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Gets the annual interest rate.
   * 
   * @return the annual interest rate
   */
  public BigDecimal getInteret() {
    return interet;
  }

  /**
   * Sets the annual interest rate.
   * 
   * @param interet the annual interest rate to set
   */
  public void setInteret(BigDecimal interet) {
    this.interet = interet;
  }

  /**
   * Returns a string representation of the loan type.
   * 
   * @return a string representation containing loan type details
   */
  @Override
  public String toString() {
    return "TypeComptePret{" +
        "id=" + id +
        ", nom='" + nom + '\'' +
        ", interet=" + interet +
        '}';
  }
}