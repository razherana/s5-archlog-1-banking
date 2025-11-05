package mg.razherana.banking.courant.entities;

import java.io.Serializable;

import jakarta.persistence.*;

/**
 * User entity representing bank customers in the banking system.
 * 
 * <p>
 * This entity stores basic user information including personal details
 * and authentication credentials. Users can have multiple current accounts
 * associated with them.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.dto.requests.users.CreateUserRequest
 * @see mg.razherana.banking.courant.dto.requests.users.UpdateUserRequest
 */
@Entity
@Table(name = "users")
public class User implements Serializable {
  /**
   * Unique identifier for the user.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Full name of the user.
   * Cannot be null.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Email address of the user.
   * Must be unique and cannot be null.
   */
  @Column(nullable = false)
  private String email;

  /**
   * User's password for authentication.
   * Cannot be null.
   */
  @Column(nullable = false)
  private String password;

  /**
   * Gets the unique identifier of the user.
   * 
   * @return the user ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the user.
   * 
   * @param id the user ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the full name of the user.
   * 
   * @return the user's full name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the full name of the user.
   * 
   * @param name the user's full name to set
   * @throws IllegalArgumentException if name is null or empty
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the email address of the user.
   * 
   * @return the user's email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address of the user.
   * 
   * @param email the user's email address to set
   * @throws IllegalArgumentException if email is null or invalid format
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the user's password.
   * 
   * @return the user's password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the user's password.
   * 
   * @param password the user's password to set
   * @throws IllegalArgumentException if password is null or too weak
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns a string representation of the user.
   * The password is excluded for security reasons.
   * 
   * @return a string representation containing id, name, and email
   */
  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
