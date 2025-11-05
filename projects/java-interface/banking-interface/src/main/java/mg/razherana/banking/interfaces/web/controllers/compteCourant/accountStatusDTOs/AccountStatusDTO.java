package mg.razherana.banking.interfaces.web.controllers.compteCourant.accountStatusDTOs;

import java.math.BigDecimal;

public class AccountStatusDTO {
  private BigDecimal balance;
  private String formattedBalance;
  private String formattedTaxPaid;
  private String formattedTaxToPay;
  private boolean taxToPayPositive;
  private int totalTransactions;
  private String formattedTotalDeposits;
  private String formattedTotalWithdrawals;
  private String formattedTotalTransfersSent;
  private String formattedTotalTransfersReceived;
  private String formattedTotalTaxPayments;

  public AccountStatusDTO(BigDecimal balance, BigDecimal taxPaid, BigDecimal taxToPay,
      int totalTransactions, BigDecimal totalDeposits, BigDecimal totalWithdrawals,
      BigDecimal totalTransfersSent, BigDecimal totalTransfersReceived) {
    this.balance = balance;
    this.taxToPayPositive = taxToPay.compareTo(BigDecimal.ZERO) > 0;
    this.formattedBalance = String.format("%,.2f MGA", balance);
    this.formattedTaxPaid = String.format("%,.2f MGA", taxPaid);
    this.formattedTaxToPay = String.format("%,.2f MGA", taxToPay);
    this.totalTransactions = totalTransactions;
    this.formattedTotalDeposits = String.format("%,.2f MGA", totalDeposits);
    this.formattedTotalWithdrawals = String.format("%,.2f MGA", totalWithdrawals);
    this.formattedTotalTransfersSent = String.format("%,.2f MGA", totalTransfersSent);
    this.formattedTotalTransfersReceived = String.format("%,.2f MGA", totalTransfersReceived);
    this.formattedTotalTaxPayments = String.format("%,.2f MGA", taxPaid);
  }

  public boolean getIsPositive() {
    return balance.compareTo(BigDecimal.ZERO) >= 0;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public String getFormattedBalance() {
    return formattedBalance;
  }

  public String getFormattedTaxPaid() {
    return formattedTaxPaid;
  }

  public boolean getTaxToPayPositive() {
    return taxToPayPositive;
  }

  public String getFormattedTaxToPay() {
    return formattedTaxToPay;
  }

  public int getTotalTransactions() {
    return totalTransactions;
  }

  public String getFormattedTotalDeposits() {
    return formattedTotalDeposits;
  }

  public String getFormattedTotalWithdrawals() {
    return formattedTotalWithdrawals;
  }

  public String getFormattedTotalTransfersSent() {
    return formattedTotalTransfersSent;
  }

  public String getFormattedTotalTransfersReceived() {
    return formattedTotalTransfersReceived;
  }

  public String getFormattedTotalTaxPayments() {
    return formattedTotalTaxPayments;
  }
}