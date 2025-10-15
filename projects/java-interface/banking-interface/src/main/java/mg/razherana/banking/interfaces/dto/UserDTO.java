package mg.razherana.banking.interfaces.dto;

import mg.razherana.banking.interfaces.entities.User;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * 
 * <p>
 * This DTO is used for transferring user data through REST API endpoints
 * without exposing sensitive information like passwords.
 * </p>
 */
public class UserDTO {

  private Integer id;
  private String name;
  private String email;
  private LocalDateTime createdAt;

  // Default constructor
  public UserDTO() {
  }

  // Constructor from User entity
  public UserDTO(User user) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
    this.createdAt = user.getCreatedAt();
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}