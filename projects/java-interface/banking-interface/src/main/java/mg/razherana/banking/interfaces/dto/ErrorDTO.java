package mg.razherana.banking.interfaces.dto;

/**
 * Data Transfer Object for error responses.
 */
public class ErrorDTO {

  private String message;
  private int status;
  private String error;
  private String path;

  // Default constructor
  public ErrorDTO() {
  }

  // Constructor with parameters
  public ErrorDTO(String message, int status, String error, String path) {
    this.message = message;
    this.status = status;
    this.error = error;
    this.path = path;
  }

  // Getters and setters
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}