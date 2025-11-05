package mg.razherana.banking.interfaces.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for current account data from the banking-courant service.
 */
public class CompteCourantDTO {
  private Integer id;
  private BigDecimal taxe;
  private LocalDateTime createdAt;
  private Integer userId;
  private String userName;
  private String userEmail;
  private BigDecimal solde;

  // Default constructor
  public CompteCourantDTO() {
  }

  // Constructor with all fields
  public CompteCourantDTO(Integer id, BigDecimal taxe, LocalDateTime createdAt,
      Integer userId, String userName, String userEmail, BigDecimal solde) {
    this.id = id;
    this.taxe = taxe;
    this.createdAt = createdAt;
    this.userId = userId;
    this.userName = userName;
    this.userEmail = userEmail;
    this.solde = solde;
  }

  @Override
  public String toString() {
    return "CompteCourantDTO [id=" + id + ", taxe=" + taxe + ", createdAt=" + createdAt + ", userId=" + userId
        + ", userName=" + userName + ", userEmail=" + userEmail + ", solde=" + solde + "]";
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

  public BigDecimal getSolde() {
    return solde;
  }

  public void setSolde(BigDecimal solde) {
    this.solde = solde;
  }

  // Helper method to get formatted account number (if needed)
  public String getNumeroCompte() {
    return "ACCT-" + String.format("%08d", id != null ? id : 0);
  }

  // Helper method to get date ouverture (creation date)
  public LocalDateTime getDateOuverture() {
    return createdAt;
  }
}