package mg.razherana.banking.pret.dto;

import java.io.Serializable;

/**
 * Error Data Transfer Object for API error responses.
 * 
 * <p>
 * This DTO provides a consistent error response format across all API
 * endpoints.
 * </p>
 */
public class ErrorDTO implements Serializable {

  private String message;
  private int status;
  private String error;
  private String path;
  private long timestamp;

  public ErrorDTO() {
    this.timestamp = System.currentTimeMillis();
  }

  public ErrorDTO(String message, int status, String error, String path) {
    this.message = message;
    this.status = status;
    this.error = error;
    this.path = path;
    this.timestamp = System.currentTimeMillis();
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

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}