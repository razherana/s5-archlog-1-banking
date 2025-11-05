package mg.razherana.banking.interfaces.dto.comptePret;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentStatusDTO {
  @JsonProperty("totalPaid")
  private BigDecimal totalPaid;

  @JsonProperty("totalExpected")
  private BigDecimal totalExpected;

  @JsonProperty("amountDue")
  private BigDecimal amountDue;

  @JsonProperty("monthlyPayment")
  private BigDecimal monthlyPayment;

  @JsonProperty("fullyPaid")
  private boolean fullyPaid;

  // Default constructor
  public PaymentStatusDTO() {
  }

  // Constructor with parameters
  public PaymentStatusDTO(BigDecimal totalPaid, BigDecimal totalExpected,
      BigDecimal amountDue, BigDecimal monthlyPayment, boolean fullyPaid) {
    this.totalPaid = totalPaid;
    this.totalExpected = totalExpected;
    this.amountDue = amountDue;
    this.monthlyPayment = monthlyPayment;
    this.fullyPaid = fullyPaid;
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

  public BigDecimal getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(BigDecimal monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }

  // Helper methods
  public String getFormattedTotalPaid() {
    if (totalPaid != null) {
      return totalPaid.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  public String getFormattedTotalExpected() {
    if (totalExpected != null) {
      return totalExpected.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  public String getFormattedAmountDue() {
    if (amountDue != null) {
      return amountDue.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  public String getFormattedMonthlyPayment() {
    if (monthlyPayment != null) {
      return monthlyPayment.toString() + " MGA";
    }
    return "0.00 MGA";
  }

  // Helper method to get completion percentage
  public double getCompletionPercentage() {
    if (totalExpected != null && totalExpected.compareTo(BigDecimal.ZERO) > 0 && totalPaid != null) {
      return totalPaid.divide(totalExpected, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
    }
    return 0.0;
  }

  // Helper method to check if there's an amount due
  public boolean hasAmountDue() {
    return amountDue != null && amountDue.compareTo(BigDecimal.ZERO) > 0;
  }

  @Override
  public String toString() {
    return "PaymentStatusDTO{" +
        "totalPaid=" + totalPaid +
        ", totalExpected=" + totalExpected +
        ", amountDue=" + amountDue +
        ", monthlyPayment=" + monthlyPayment +
        '}';
  }

  public boolean isFullyPaid() {
    return fullyPaid;
  }

  public void setFullyPaid(boolean fullyPaid) {
    this.fullyPaid = fullyPaid;
  }
}