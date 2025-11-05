package mg.razherana.banking.interfaces.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for current account status information.
 * 
 * <p>
 * This DTO provides account status details at a specific date/time,
 * including balance, tax information, and transaction summary.
 * </p>
 * 
 * @author Banking Interface System
 * @version 1.0
 */
public class AccountStatusDTO {
  /** Account balance at the specified date */
  private BigDecimal balance;

  /** Tax amount paid */
  private BigDecimal taxPaid;

  /** Remaining tax amount to pay */
  private BigDecimal taxToPay;

  /** Total number of transactions */
  private Integer totalTransactions;

  /** Total amount of deposits */
  private BigDecimal totalDeposits;

  /** Total amount of withdrawals */
  private BigDecimal totalWithdrawals;

  /** Total amount of transfers sent */
  private BigDecimal totalTransfersSent;

  /** Total amount of transfers received */
  private BigDecimal totalTransfersReceived;

  /** Total amount of tax payments */
  private BigDecimal totalTaxPayments;

  /** Date/time for which the status was calculated */
  private LocalDateTime statusDate;

  /**
   * Default constructor.
   */
  public AccountStatusDTO() {
  }

  // Getters and setters
  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal getTaxPaid() {
    return taxPaid;
  }

  public void setTaxPaid(BigDecimal taxPaid) {
    this.taxPaid = taxPaid;
  }

  public BigDecimal getTaxToPay() {
    return taxToPay;
  }

  public void setTaxToPay(BigDecimal taxToPay) {
    this.taxToPay = taxToPay;
  }

  public Integer getTotalTransactions() {
    return totalTransactions;
  }

  public void setTotalTransactions(Integer totalTransactions) {
    this.totalTransactions = totalTransactions;
  }

  public BigDecimal getTotalDeposits() {
    return totalDeposits;
  }

  public void setTotalDeposits(BigDecimal totalDeposits) {
    this.totalDeposits = totalDeposits;
  }

  public BigDecimal getTotalWithdrawals() {
    return totalWithdrawals;
  }

  public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
    this.totalWithdrawals = totalWithdrawals;
  }

  public BigDecimal getTotalTransfersSent() {
    return totalTransfersSent;
  }

  public void setTotalTransfersSent(BigDecimal totalTransfersSent) {
    this.totalTransfersSent = totalTransfersSent;
  }

  public BigDecimal getTotalTransfersReceived() {
    return totalTransfersReceived;
  }

  public void setTotalTransfersReceived(BigDecimal totalTransfersReceived) {
    this.totalTransfersReceived = totalTransfersReceived;
  }

  public BigDecimal getTotalTaxPayments() {
    return totalTaxPayments;
  }

  public void setTotalTaxPayments(BigDecimal totalTaxPayments) {
    this.totalTaxPayments = totalTaxPayments;
  }

  public LocalDateTime getStatusDate() {
    return statusDate;
  }

  public void setStatusDate(LocalDateTime statusDate) {
    this.statusDate = statusDate;
  }

  /**
   * Get formatted balance for display.
   */
  public String getFormattedBalance() {
    return balance != null ? balance.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted tax paid for display.
   */
  public String getFormattedTaxPaid() {
    return taxPaid != null ? taxPaid.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted tax to pay for display.
   */
  public String getFormattedTaxToPay() {
    return taxToPay != null ? taxToPay.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted total deposits for display.
   */
  public String getFormattedTotalDeposits() {
    return totalDeposits != null ? totalDeposits.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted total withdrawals for display.
   */
  public String getFormattedTotalWithdrawals() {
    return totalWithdrawals != null ? totalWithdrawals.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted total transfers sent for display.
   */
  public String getFormattedTotalTransfersSent() {
    return totalTransfersSent != null ? totalTransfersSent.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted total transfers received for display.
   */
  public String getFormattedTotalTransfersReceived() {
    return totalTransfersReceived != null ? totalTransfersReceived.toString() + " MGA" : "0.00 MGA";
  }

  /**
   * Get formatted total tax payments for display.
   */
  public String getFormattedTotalTaxPayments() {
    return totalTaxPayments != null ? totalTaxPayments.toString() + " MGA" : "0.00 MGA";
  }
}