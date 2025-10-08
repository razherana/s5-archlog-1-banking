package mg.razherana.banking.courant.dto;

/**
 * Data Transfer Object for simple message responses in the banking API.
 * 
 * <p>Used for success responses and simple confirmations where only a
 * text message needs to be returned to the client.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public class MessageDTO {
    /** The message content */
    private String message;

    /**
     * Default constructor.
     */
    public MessageDTO() {}

    /**
     * Constructor with message.
     * 
     * @param message the message to include in the response
     */
    public MessageDTO(String message) {
        this.message = message;
    }

    /**
     * Gets the message content.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content.
     * 
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
