package mg.razherana.banking.interfaces.application.comptePretServices;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.interfaces.dto.comptePret.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for communicating with the banking-pret service.
 */
public interface ComptePretService {

  /**
   * Fetches all available loan types.
   * 
   * @param userAdmin The admin performing the action
   * @return List of loan types
   */
  List<TypeComptePretDTO> getAllLoanTypes(UserAdmin userAdmin);

  /**
   * Fetches all loans for a specific user.
   * 
   * @param userAdmin The admin performing the action
   * @param userId The ID of the user
   * @return List of loans for the user
   */
  List<ComptePretDTO> getLoansByUserId(UserAdmin userAdmin, Integer userId);

  /**
   * Gets a specific loan by ID.
   * 
   * @param userAdmin The admin performing the action
   * @param loanId The loan ID
   * @return The loan or null if not found
   */
  ComptePretDTO getLoanById(UserAdmin userAdmin, Integer loanId);

  /**
   * Creates a new loan account and deposits the amount to the specified current
   * account.
   * 
   * @param userAdmin The admin performing the action
   * @param request The loan creation request with current account selection
   * @return The created loan or null if creation failed
   */
  ComptePretDTO createLoan(UserAdmin userAdmin, CreateComptePretRequest request);

  /**
   * Makes a payment for a loan.
   * 
   * @param userAdmin The admin performing the action
   * @param request The payment request
   * @return The payment record or null if payment failed
   */
  EcheanceDTO makePayment(UserAdmin userAdmin, MakePaymentRequest request);

  /**
   * Gets the payment status for a loan.
   * 
   * @param userAdmin The admin performing the action
   * @param loanId The loan ID
   * @return The payment status or null if not found
   */
  PaymentStatusDTO getPaymentStatus(UserAdmin userAdmin, Integer loanId);

  /**
   * Gets the payment status for a loan at a specific date/time.
   * 
   * @param userAdmin The admin performing the action
   * @param loanId The loan ID
   * @param actionDateTime The date/time to calculate status for
   * @return The payment status or null if not found
   */
  PaymentStatusDTO getPaymentStatus(UserAdmin userAdmin, Integer loanId, java.time.LocalDateTime actionDateTime);

  /**
   * Gets the payment history for a loan.
   * 
   * @param userAdmin The admin performing the action
   * @param loanId The loan ID
   * @return List of payments for the loan
   */
  List<EcheanceDTO> getPaymentHistory(UserAdmin userAdmin, Integer loanId);

  /**
   * Gets the payment history for a loan at a specific date/time.
   * 
   * @param userAdmin The admin performing the action
   * @param loanId The loan ID
   * @param actionDateTime The date/time to filter the payment history
   * @return List of payments for the loan
   */
  List<EcheanceDTO> getPaymentHistory(UserAdmin userAdmin, Integer loanId, LocalDateTime actionDateTime);

  /**
   * Gets the loan balance for a user at a specific date/time.
   * 
   * @param userAdmin The admin performing the action
   * @param userId The user ID
   * @param actionDateTime The date/time to calculate balance for
   * @return Loan balance of user
   */
  BigDecimal getLoanBalanceByUserId(UserAdmin userAdmin, Integer userId, LocalDateTime actionDateTime);
}