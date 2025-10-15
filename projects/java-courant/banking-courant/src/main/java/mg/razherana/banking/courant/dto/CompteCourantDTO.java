package mg.razherana.banking.courant.dto;

import mg.razherana.banking.courant.entities.CompteCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for current account (Compte Courant) information.
 * 
 * <p>
 * This DTO provides a complete view of current account information including
 * calculated balance and associated user details. Used for API responses to
 * avoid exposing entity internals and include computed values.
 * </p>
 * 
 * <p>
 * The balance (solde) is calculated dynamically by summing all transactions
 * and is not stored in the database.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.api.CompteCourantResource
 * @see mg.razherana.banking.courant.application.compteCourantService.CompteCourantService
 */
public class CompteCourantDTO {
  /** Unique identifier of the current account */
  private Integer id;

  /** Monthly tax amount for this account */
  private BigDecimal taxe;

  /** Timestamp when this account was created */
  private LocalDateTime createdAt;

  /** ID of the user who owns this account */
  private Integer userId;

  /** Name of the user who owns this account */
  private String userName;

  /** Email of the user who owns this account */
  private String userEmail;

  /** Calculated current balance of the account */
  private BigDecimal solde; // Calculated field

  /**
   * Default constructor.
   */
  public CompteCourantDTO() {
  }

  /**
   * Constructor that creates a DTO from a CompteCourant entity and calculated
   * balance.
   * 
   * @param compte the CompteCourant entity to convert
   * @param solde  the calculated balance for this account
   */
  public CompteCourantDTO(CompteCourant compte, BigDecimal solde) {
    this.id = compte.getId();
    this.taxe = compte.getTaxe();
    this.createdAt = compte.getCreatedAt();
    this.solde = solde;
    if (compte.getUser() != null) {
      this.userId = compte.getUser().getId();
      this.userName = compte.getUser().getName();
      this.userEmail = compte.getUser().getEmail();
    }
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public BigDecimal getTaxe() {
    return taxe;
  }

  public void setTaxe(BigDecimal taxe) {
    this.taxe = taxe;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public BigDecimal getSolde() {
    return solde;
  }

  public void setSolde(BigDecimal solde) {
    this.solde = solde;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }
}
