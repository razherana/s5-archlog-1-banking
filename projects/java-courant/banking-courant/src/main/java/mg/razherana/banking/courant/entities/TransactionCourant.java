package mg.razherana.banking.courant.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing financial transactions in the current account
 * system.
 * 
 * <p>
 * This entity implements a double-entry-like transaction system where:
 * </p>
 * <ul>
 * <li><strong>Deposits:</strong> sender = null, receiver = account</li>
 * <li><strong>Withdrawals:</strong> sender = account, receiver = null</li>
 * <li><strong>Transfers:</strong> sender = source account, receiver =
 * destination account</li>
 * <li><strong>Tax payments:</strong> sender = account, receiver = null,
 * specialAction = "taxe"</li>
 * </ul>
 * 
 * <p>
 * Account balances are calculated by summing all transactions where the account
 * appears as receiver (+) minus all transactions where it appears as sender
 * (-).
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.application.transactionService.TransactionService
 * @see mg.razherana.banking.courant.dto.TransactionCourantDTO
 */
@Entity
@Table(name = "transaction_courants")
public class TransactionCourant {
  /**
   * Enumeration defining special transaction types for the banking system.
   * 
   * <p>
   * This enum helps categorize transactions beyond the basic sender/receiver
   * pattern:
   * </p>
   * <ul>
   * <li><strong>DEPOSIT:</strong> External money coming into an account</li>
   * <li><strong>WITHDRAWAL:</strong> Money leaving an account to external
   * destination</li>
   * <li><strong>TAXE:</strong> Tax payment transactions</li>
   * </ul>
   */
  /**
   * Enumeration defining special transaction types for the banking system.
   * 
   * <p>
   * This enum helps categorize transactions beyond the basic sender/receiver
   * pattern:
   * </p>
   * <ul>
   * <li><strong>DEPOSIT:</strong> External money coming into an account</li>
   * <li><strong>WITHDRAWAL:</strong> Money leaving an account to external
   * destination</li>
   * <li><strong>TAXE:</strong> Tax payment transactions</li>
   * </ul>
   */
  public static enum SpecialAction {
    /** Represents deposit transactions from external sources */
    DEPOSIT("deposit"),
    /** Represents withdrawal transactions to external destinations */
    WITHDRAWAL("withdrawal"),
    /** Represents tax payment transactions */
    TAXE("taxe");

    /** The database representation of this special action */
    private String databaseName;

    /**
     * Creates a new SpecialAction with the given database name.
     * 
     * @param databaseName the string value stored in the database
     */
    SpecialAction(String databaseName) {
      this.databaseName = databaseName;
    }

    /**
     * Gets the database representation of this special action.
     * 
     * @return the database name string
     */
    public String getDatabaseName() {
      return databaseName;
    }
  }

  /**
   * Unique identifier for the transaction.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Special action type for this transaction.
   * Used to categorize transactions beyond the sender/receiver pattern.
   * Can be null for regular transfers between accounts.
   * 
   * @see SpecialAction
   */
  @Column(name = "special_action", nullable = true)
  private String specialAction;

  /**
   * The account sending money in this transaction.
   * Can be null for deposit transactions (external source).
   * Lazy fetched for performance optimization.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = true)
  private CompteCourant sender;

  /**
   * The account receiving money in this transaction.
   * Can be null for withdrawal transactions (external destination).
   * Lazy fetched for performance optimization.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = true)
  private CompteCourant receiver;

  /**
   * The monetary amount of this transaction.
   * Always positive - direction is determined by sender/receiver.
   * Stored with precision 15 and scale 2 for high-precision monetary values.
   */
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal montant;

  /**
   * The timestamp when this transaction occurred.
   * Defaults to current time when the transaction is created.
   */
  @Column(name = "date", nullable = false)
  private LocalDateTime date = LocalDateTime.now();

  /**
   * Gets the unique identifier of the transaction.
   * 
   * @return the transaction ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the transaction.
   * 
   * @param id the transaction ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the account that sent money in this transaction.
   * 
   * @return the sender account, or null for deposit transactions
   */
  public CompteCourant getSender() {
    return sender;
  }

  /**
   * Sets the account that sent money in this transaction.
   * 
   * @param sender the sender account to set, or null for deposits
   */
  public void setSender(CompteCourant sender) {
    this.sender = sender;
  }

  /**
   * Gets the account that received money in this transaction.
   * 
   * @return the receiver account, or null for withdrawal transactions
   */
  public CompteCourant getReceiver() {
    return receiver;
  }

  /**
   * Sets the account that received money in this transaction.
   * 
   * @param receiver the receiver account to set, or null for withdrawals
   */
  public void setReceiver(CompteCourant receiver) {
    this.receiver = receiver;
  }

  /**
   * Gets the monetary amount of this transaction.
   * 
   * @return the transaction amount as BigDecimal
   */
  public BigDecimal getMontant() {
    return montant;
  }

  /**
   * Sets the monetary amount of this transaction.
   * 
   * @param montant the transaction amount to set
   * @throws IllegalArgumentException if montant is null or negative
   */
  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  /**
   * Gets the timestamp when this transaction occurred.
   * 
   * @return the transaction date and time
   */
  public LocalDateTime getDate() {
    return date;
  }

  /**
   * Sets the timestamp when this transaction occurred.
   * 
   * @param date the transaction date and time to set
   */
  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  /**
   * Gets the special action type of this transaction as a string.
   * 
   * @return the special action string, or null if not a special transaction
   * @see SpecialAction
   */
  public String getSpecialAction() {
    return specialAction;
  }

  /**
   * Sets the special action type of this transaction.
   * 
   * @param specialAction the special action string to set
   * @see SpecialAction
   */
  public void setSpecialAction(String specialAction) {
    this.specialAction = specialAction;
  }

  /**
   * Gets the special action type of this transaction as an enum.
   * 
   * @return the SpecialAction enum value, or null if not a special transaction
   * @see SpecialAction
   */
  public SpecialAction getSpecialActionEnum() {
    if (specialAction == null) {
      return null;
    }

    for (SpecialAction action : SpecialAction.values())
      if (action.getDatabaseName().equals(specialAction))
        return action;

    return null;
  }

  /**
   * Returns a string representation of the transaction.
   * Excludes sender/receiver details to prevent circular references.
   * 
   * @return a string representation containing id, amount, and date
   */
  @Override
  public String toString() {
    return "TransactionCourant{" +
        "id=" + id +
        ", montant=" + montant +
        ", date=" + date +
        '}';
  }
}
