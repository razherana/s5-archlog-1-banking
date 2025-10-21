package mg.razherana.banking.interfaces.dto.requests;

/**
 * Request DTO for user registration.
 */
public class RegisterRequest {

  private String name;

  // Default constructor
  public RegisterRequest() {
  }

  // Constructor with parameters
  public RegisterRequest(String name) {
    this.name = name;
  }

  // Getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}