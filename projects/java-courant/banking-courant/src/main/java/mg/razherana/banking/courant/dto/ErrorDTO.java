package mg.razherana.banking.courant.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for error responses in the banking API.
 * 
 * <p>Provides standardized error information for API clients including
 * error message, HTTP status code, error type, timestamp, and request path.
 * Used consistently across all API endpoints for error handling.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public class ErrorDTO {
    /** Human-readable error message */
    private String message;
    
    /** HTTP status code */
    private int status;
    
    /** Error type or category */
    private String error;
    
    /** Timestamp when the error occurred */
    private LocalDateTime timestamp;
    
    /** API path where the error occurred */
    private String path;

    /**
     * Default constructor that sets the timestamp to current time.
     */
    public ErrorDTO() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with basic error information.
     * 
     * @param message the error message
     * @param status the HTTP status code
     * @param error the error type
     */
    public ErrorDTO(String message, int status, String error) {
        this();
        this.message = message;
        this.status = status;
        this.error = error;
    }

    public ErrorDTO(String message, int status, String error, String path) {
        this(message, status, error);
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
