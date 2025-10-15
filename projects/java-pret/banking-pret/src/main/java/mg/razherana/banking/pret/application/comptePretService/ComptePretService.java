package mg.razherana.banking.pret.application.comptePretService;

import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.TypeComptePret;
import mg.razherana.banking.pret.entities.Echeance;
import mg.razherana.banking.pret.entities.User;
import mg.razherana.banking.pret.dto.PaymentStatusDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing loan account (Compte Pret) operations.
 * 
 * <p>
 * This service provides business logic for loan account management including
 * creation, retrieval, payment processing, and amortization calculations.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public interface ComptePretService {

  /**
   * Find a user by ID using REST API call to java-interface.
   * 
   * @param userId the user ID
   * @return User object with the specified ID or null if not found
   * @throws IllegalArgumentException if userId is null
   */
  User findUser(Integer userId);

  /**
   * Creates a new loan account.
   * 
   * @param userId           the user ID
   * @param typeComptePretId the loan type ID
   * @param montant          the loan amount
   * @param dateDebut        the loan start date
   * @param dateFin          the loan end date
   * @return the created loan account
   */
  ComptePret createLoan(Integer userId, Integer typeComptePretId, BigDecimal montant,
      LocalDateTime dateDebut, LocalDateTime dateFin);

  /**
   * Finds a loan type by ID.
   * 
   * @param id the loan type ID
   * @return the loan type or null if not found
   */
  TypeComptePret findLoanTypeById(Integer id);

  /**
   * Gets all loan types.
   * 
   * @return list of all loan types
   */
  List<TypeComptePret> getAllLoanTypes();

  /**
   * Finds a loan account by ID.
   * 
   * @param id the loan account ID
   * @return the loan account or null if not found
   */
  ComptePret findById(Integer id);

  /**
   * Find all loans.
   * 
   * @return list of all loan accounts
   */
  List<ComptePret> findAllLoans();

  /**
   * Gets all loan accounts for a user.
   * 
   * @param userId the user ID
   * @return list of loan accounts for the user
   */
  List<ComptePret> getLoansByUserId(Integer userId);

  /**
   * Calculates the monthly payment for a loan using amortization formula.
   * Formula: M = [C × i] / [1 - (1 + i)^(-n)]
   * 
   * @param loan the loan account
   * @return the monthly payment amount
   */
  BigDecimal calculateMonthlyPayment(ComptePret loan);

  /**
   * Gets all payments for a loan account.
   * 
   * @param compteId the loan account ID
   * @return list of payments for the loan
   */
  List<Echeance> getPaymentHistory(Integer compteId);

  /**
   * Calculates total amount paid for a loan.
   * 
   * @param compteId the loan account ID
   * @return total amount paid
   */
  BigDecimal calculateTotalPaid(Integer compteId);

  /**
   * Calculates the expected amount to be paid by a specific date.
   * 
   * @param loan the loan account
   * @param actionDateTime the date to calculate expected payment
   * @return expected amount to be paid
   */
  BigDecimal calculateExpectedPaidByDate(ComptePret loan, LocalDateTime actionDateTime);

  /**
   * Gets payment status for a loan at a specific date.
   * 
   * @param compteId the loan account ID
   * @param actionDateTime the date to check status (null for current date)
   * @return payment status information
   */
  PaymentStatusDTO getPaymentStatus(Integer compteId, LocalDateTime actionDateTime);

  /**
   * Makes a payment for a loan.
   * 
   * @param compteId the loan account ID
   * @param amount the payment amount
   * @param actionDateTime the payment date (null for current date)
   * @return the created payment record
   */
  Echeance makePayment(Integer compteId, BigDecimal amount, LocalDateTime actionDateTime);
}