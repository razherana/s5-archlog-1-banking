package mg.razherana.banking.pret.entities;

import java.io.Serializable;

/**
 * Simple User entity for representing user data retrieved from java-interface
 * service.
 * This is a minimal representation used for loan account operations.
 * 
 * <p>
 * The full user management is handled by the java-interface service.
 * This entity is used locally only for loan account relationships.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public class User implements Serializable {

  private Integer id;
  private String name;

  /**
   * Default constructor.
   */
  public User() {
  }

  /**
   * Constructor with basic fields.
   */
  public User(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

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

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}