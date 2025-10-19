package mg.razherana.banking.interfaces.dto.requests;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transfer request data.
 */
public class TransferRequest {

  private Integer sourceAccountId;
  private Integer destinationAccountId;
  private BigDecimal amount;
  private String description;
  private LocalDateTime actionDateTime;

  // Default constructor
  public TransferRequest() {
  }

  // Constructor with required fields
  public TransferRequest(Integer sourceAccountId, Integer destinationAccountId, BigDecimal amount) {
    this.sourceAccountId = sourceAccountId;
    this.destinationAccountId = destinationAccountId;
    this.amount = amount;
  }

  // Getters and setters
  public Integer getSourceAccountId() {
    return sourceAccountId;
  }

  public void setSourceAccountId(Integer sourceAccountId) {
    this.sourceAccountId = sourceAccountId;
  }

  public Integer getDestinationAccountId() {
    return destinationAccountId;
  }

  public void setDestinationAccountId(Integer destinationAccountId) {
    this.destinationAccountId = destinationAccountId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getActionDateTime() {
    return actionDateTime;
  }

  public void setActionDateTime(LocalDateTime actionDateTime) {
    this.actionDateTime = actionDateTime;
  }

  @Override
  public String toString() {
    return "TransferRequest{" +
        "sourceAccountId=" + sourceAccountId +
        ", destinationAccountId=" + destinationAccountId +
        ", amount=" + amount +
        ", description='" + description + '\'' +
        ", actionDateTime=" + actionDateTime +
        '}';
  }
}