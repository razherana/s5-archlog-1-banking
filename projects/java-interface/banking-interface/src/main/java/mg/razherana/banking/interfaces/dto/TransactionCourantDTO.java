package mg.razherana.banking.interfaces.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for current account transaction information.
 * 
 * <p>
 * This DTO provides transaction details for API responses, including account
 * IDs instead of full account objects to avoid circular references and reduce
 * payload size.
 * </p>
 * 
 * <p>
 * The sender and receiver IDs indicate the direction of money flow:
 * </p>
 * <ul>
 * <li><strong>Deposits:</strong> senderId = null, receiverId = account ID</li>
 * <li><strong>Withdrawals:</strong> senderId = account ID, receiverId = null</li>
 * <li><strong>Transfers:</strong> senderId = source account ID, receiverId = destination account ID</li>
 * </ul>
 * 
 * @author Banking Interface System
 * @version 1.0
 */
public class TransactionCourantDTO {
  
  /**
   * Enumeration defining special transaction types for the banking system interface.
   * 
   * <p>
   * This enum helps categorize transactions beyond the basic sender/receiver
   * pattern and mirrors the backend TransactionCourant.SpecialAction enum:
   * </p>
   * <ul>
   * <li><strong>DEPOSIT:</strong> External money coming into an account</li>
   * <li><strong>WITHDRAWAL:</strong> Money leaving an account to external destination</li>
   * <li><strong>TAXE:</strong> Tax payment transactions</li>
   * </ul>
   */
  public static enum SpecialAction {
    /** Represents deposit transactions from external sources */
    DEPOSIT("deposit", "Dépôt"),
    /** Represents withdrawal transactions to external destinations */
    WITHDRAWAL("withdrawal", "Retrait"),
    /** Represents tax payment transactions */
    TAXE("taxe", "Paiement Taxe");

    /** The database representation of this special action */
    private final String databaseName;
    /** The display name for this special action */
    private final String displayName;

    /**
     * Creates a new SpecialAction with the given database name and display name.
     * 
     * @param databaseName the string value stored in the database
     * @param displayName the human-readable display name
     */
    SpecialAction(String databaseName, String displayName) {
      this.databaseName = databaseName;
      this.displayName = displayName;
    }

    /**
     * Gets the database representation of this special action.
     * 
     * @return the database name string
     */
    public String getDatabaseName() {
      return databaseName;
    }

    /**
     * Gets the display name of this special action.
     * 
     * @return the display name string
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Gets the SpecialAction enum from a database name.
     * 
     * @param databaseName the database name to lookup
     * @return the matching SpecialAction or null if not found
     */
    public static SpecialAction fromDatabaseName(String databaseName) {
      if (databaseName == null) {
        return null;
      }
      for (SpecialAction action : values()) {
        if (action.getDatabaseName().equals(databaseName)) {
          return action;
        }
      }
      return null;
    }
  }
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

  /**
   * Get formatted amount for display.
   */
  public String getFormattedMontant() {
    return montant != null ? montant.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted date for display.
   */
  public String getFormattedDate() {
    return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
  }

  /**
   * Get transaction type based on sender and receiver IDs and special action.
   */
  public String getTransactionType() {
    // Check for special actions first
    SpecialAction action = SpecialAction.fromDatabaseName(specialAction);
    if (action != null) {
      return action.getDisplayName();
    }
    
    // Determine type based on sender/receiver pattern
    if (senderId == null && receiverId != null) {
      return "Dépôt";
    } else if (senderId != null && receiverId == null) {
      return "Retrait";
    } else if (senderId != null && receiverId != null) {
      return "Transfert";
    }
    
    return "Autre";
  }

  /**
   * Gets the special action type of this transaction as an enum.
   * 
   * @return the SpecialAction enum value, or null if not a special transaction
   */
  public SpecialAction getSpecialActionEnum() {
    return SpecialAction.fromDatabaseName(specialAction);
  }

  /**
   * Determines if this transaction is positive (incoming money) for the given account.
   * 
   * @param accountId the account ID to check for
   * @return true if the transaction increases the account balance
   */
  public boolean isPositiveForAccount(Integer accountId) {
    if (accountId == null) {
      return false;
    }
    
    // Tax payments are always negative
    if (SpecialAction.TAXE.getDatabaseName().equals(specialAction)) {
      return false;
    }
    
    // Deposits (external source to account) are positive
    if (senderId == null && accountId.equals(receiverId)) {
      return true;
    }
    
    // Transfers received are positive
    if (senderId != null && accountId.equals(receiverId)) {
      return true;
    }
    
    // All other cases (withdrawals, transfers sent) are negative
    return false;
  }

  /**
   * Determines if this transaction is negative (outgoing money) for the given account.
   * 
   * @param accountId the account ID to check for
   * @return true if the transaction decreases the account balance
   */
  public boolean isNegativeForAccount(Integer accountId) {
    return !isPositiveForAccount(accountId) && isInvolvedInTransaction(accountId);
  }

  /**
   * Determines if the given account is involved in this transaction.
   * 
   * @param accountId the account ID to check for
   * @return true if the account is either sender or receiver
   */
  public boolean isInvolvedInTransaction(Integer accountId) {
    if (accountId == null) {
      return false;
    }
    return accountId.equals(senderId) || accountId.equals(receiverId);
  }
}