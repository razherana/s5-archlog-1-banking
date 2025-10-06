package mg.razherana.banking.courant.dto;

import java.time.LocalDateTime;

public class ErrorDTO {
    private String message;
    private int status;
    private String error;
    private LocalDateTime timestamp;
    private String path;

    public ErrorDTO() {
        this.timestamp = LocalDateTime.now();
    }

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
