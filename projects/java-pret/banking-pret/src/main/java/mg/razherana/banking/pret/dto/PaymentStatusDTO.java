package mg.razherana.banking.pret.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for loan payment status information.
 */
public class PaymentStatusDTO implements Serializable {

  private BigDecimal totalPaid;
  private BigDecimal totalExpected;
  private BigDecimal amountDue;
  private boolean isFullyPaid;
  private BigDecimal monthlyPayment;

  // Default constructor
  public PaymentStatusDTO() {
  }

  // Constructor
  public PaymentStatusDTO(BigDecimal totalPaid, BigDecimal totalExpected,
      BigDecimal amountDue, boolean isFullyPaid, BigDecimal monthlyPayment) {
    this.totalPaid = totalPaid;
    this.totalExpected = totalExpected;
    this.amountDue = amountDue;
    this.isFullyPaid = isFullyPaid;
    this.monthlyPayment = monthlyPayment;
  }

  // Getters and setters
  public BigDecimal getTotalPaid() {
    return totalPaid;
  }

  public void setTotalPaid(BigDecimal totalPaid) {
    this.totalPaid = totalPaid;
  }

  public BigDecimal getTotalExpected() {
    return totalExpected;
  }

  public void setTotalExpected(BigDecimal totalExpected) {
    this.totalExpected = totalExpected;
  }

  public BigDecimal getAmountDue() {
    return amountDue;
  }

  public void setAmountDue(BigDecimal amountDue) {
    this.amountDue = amountDue;
  }

  public boolean isFullyPaid() {
    return isFullyPaid;
  }

  public void setFullyPaid(boolean fullyPaid) {
    isFullyPaid = fullyPaid;
  }

  public BigDecimal getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(BigDecimal monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }

  @Override
  public String toString() {
    return "PaymentStatusDTO{" +
        "totalPaid=" + totalPaid +
        ", totalExpected=" + totalExpected +
        ", amountDue=" + amountDue +
        ", isFullyPaid=" + isFullyPaid +
        ", monthlyPayment=" + monthlyPayment +
        '}';
  }
}