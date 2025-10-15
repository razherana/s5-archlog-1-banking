package mg.razherana.banking.pret.dto;

import mg.razherana.banking.pret.entities.ComptePret;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ComptePret entity.
 * 
 * <p>
 * This DTO is used for transferring loan account data through REST API
 * endpoints.
 * </p>
 */
public class ComptePretDTO {

  private Integer id;
  private Integer userId;
  private LocalDateTime createdAt;

  // Default constructor
  public ComptePretDTO() {
  }

  // Constructor from ComptePret entity
  public ComptePretDTO(ComptePret compte) {
    this.id = compte.getId();
    this.userId = compte.getUserId();
    this.createdAt = compte.getCreatedAt();
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "ComptePretDTO{" +
        "id=" + id +
        ", userId=" + userId +
        ", createdAt=" + createdAt +
        '}';
  }
}