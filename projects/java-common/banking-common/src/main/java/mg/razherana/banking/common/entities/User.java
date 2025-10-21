package mg.razherana.banking.common.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity for the banking interface system.
 * 
 * <p>
 * This entity represents users in the banking system and serves as the central
 * user management for all banking services (courant, depot, pret).
 * </p>
 * 
 * <p>
 * The User entity is managed by the java-interface service and can be accessed
 * by other services through EJB remote interfaces or REST API calls.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  // Default constructor
  public User() {
    this.createdAt = LocalDateTime.now();
  }

  // Constructor with parameters
  public User(String name) {
    this();
    this.name = name;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}