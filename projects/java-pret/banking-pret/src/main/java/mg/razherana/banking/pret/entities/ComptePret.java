package mg.razherana.banking.pret.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ComptePret entity representing loan accounts in the banking system.
 * 
 * <p>
 * This entity stores basic loan account information.
 * Loan accounts are linked to users through userId references.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "compte_prets")
public class ComptePret {

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
   * Timestamp when the loan account was created.
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Default constructor for JPA.
   */
  public ComptePret() {
  }

  /**
   * Constructor with userId.
   * 
   * @param userId the ID of the user who owns this loan account
   */
  public ComptePret(Integer userId) {
    this.userId = userId;
    this.createdAt = LocalDateTime.now();
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
   * Gets the creation timestamp of the loan account.
   * 
   * @return the creation timestamp
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Sets the creation timestamp of the loan account.
   * 
   * @param createdAt the creation timestamp to set
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Pre-persist callback to set creation timestamp.
   */
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  /**
   * Returns a string representation of the loan account.
   * 
   * @return a string representation containing id, userId, and createdAt
   */
  @Override
  public String toString() {
    return "ComptePret{" +
        "id=" + id +
        ", userId=" + userId +
        ", createdAt=" + createdAt +
        '}';
  }
}