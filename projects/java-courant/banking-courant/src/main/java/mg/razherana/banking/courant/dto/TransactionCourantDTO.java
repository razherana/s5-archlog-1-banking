package mg.razherana.banking.courant.dto;

import mg.razherana.banking.courant.entities.TransactionCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for transaction information.
 * 
 * <p>
 * This DTO provides transaction details for API responses, including account
 * IDs
 * instead of full account objects to avoid circular references and reduce
 * payload size.
 * </p>
 * 
 * <p>
 * The sender and receiver IDs indicate the direction of money flow:
 * </p>
 * <ul>
 * <li><strong>Deposits:</strong> senderId = null, receiverId = account ID</li>
 * <li><strong>Withdrawals:</strong> senderId = account ID, receiverId =
 * null</li>
 * <li><strong>Transfers:</strong> senderId = source account ID, receiverId =
 * destination account ID</li>
 * </ul>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.api.TransactionResource
 * @see mg.razherana.banking.courant.application.transactionService.TransactionService
 */
public class TransactionCourantDTO {
  /** Unique identifier of the transaction */
  private Integer id;

  /** ID of the account sending money (null for deposits) */
  private Integer senderId;

  /** ID of the account receiving money (null for withdrawals) */
  private Integer receiverId;

  /** Monetary amount of the transaction */
  private BigDecimal montant;

  /** Timestamp when the transaction occurred */
  private LocalDateTime date;

  /** Special action type (e.g., "taxe" for tax payments) */
  private String specialAction;

  /**
   * Default constructor.
   */
  public TransactionCourantDTO() {
  }

  /**
   * Constructor that creates a DTO from a TransactionCourant entity.
   * 
   * @param transaction the TransactionCourant entity to convert
   */
  public TransactionCourantDTO(TransactionCourant transaction) {
    this.id = transaction.getId();
    this.senderId = transaction.getSender() != null ? transaction.getSender().getId() : null;
    this.receiverId = transaction.getReceiver() != null ? transaction.getReceiver().getId() : null;
    this.montant = transaction.getMontant();
    this.date = transaction.getDate();
    this.specialAction = transaction.getSpecialAction();
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getSenderId() {
    return senderId;
  }

  public void setSenderId(Integer senderId) {
    this.senderId = senderId;
  }

  public Integer getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(Integer receiverId) {
    this.receiverId = receiverId;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getSpecialAction() {
    return specialAction;
  }

  public void setSpecialAction(String specialAction) {
    this.specialAction = specialAction;
  }
}
