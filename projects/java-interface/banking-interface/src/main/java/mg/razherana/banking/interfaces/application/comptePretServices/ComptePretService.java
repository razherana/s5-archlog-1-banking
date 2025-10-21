package mg.razherana.banking.interfaces.application.comptePretServices;

import mg.razherana.banking.interfaces.dto.comptePret.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for communicating with the banking-pret service.
 */
public interface ComptePretService {

  /**
   * Fetches all available loan types.
   * 
   * @return List of loan types
   */
  List<TypeComptePretDTO> getAllLoanTypes();

  /**
   * Fetches all loans for a specific user.
   * 
   * @param userId The ID of the user
   * @return List of loans for the user
   */
  List<ComptePretDTO> getLoansByUserId(Integer userId);

  /**
   * Gets a specific loan by ID.
   * 
   * @param loanId The loan ID
   * @return The loan or null if not found
   */
  ComptePretDTO getLoanById(Integer loanId);

  /**
   * Creates a new loan account and deposits the amount to the specified current
   * account.
   * 
   * @param request The loan creation request with current account selection
   * @return The created loan or null if creation failed
   */
  ComptePretDTO createLoan(CreateComptePretRequest request);

  /**
   * Makes a payment for a loan.
   * 
   * @param request The payment request
   * @return The payment record or null if payment failed
   */
  EcheanceDTO makePayment(MakePaymentRequest request);

  /**
   * Gets the payment status for a loan.
   * 
   * @param loanId The loan ID
   * @return The payment status or null if not found
   */
  PaymentStatusDTO getPaymentStatus(Integer loanId);

  /**
   * Gets the payment status for a loan at a specific date/time.
   * 
   * @param loanId The loan ID
   * @param actionDateTime The date/time to calculate status for
   * @return The payment status or null if not found
   */
  PaymentStatusDTO getPaymentStatus(Integer loanId, java.time.LocalDateTime actionDateTime);

  /**
   * Gets the payment history for a loan.
   * 
   * @param loanId The loan ID
   * @return List of payments for the loan
   */
  List<EcheanceDTO> getPaymentHistory(Integer loanId);

  /**
   * Gets the payment history for a loan at a specific date/time.
   * 
   * @param loanId The loan ID
   * @param actionDateTime The date/time to filter the payment history
   * @return List of payments for the loan
   */
  List<EcheanceDTO> getPaymentHistory(Integer loanId, LocalDateTime actionDateTime);
}