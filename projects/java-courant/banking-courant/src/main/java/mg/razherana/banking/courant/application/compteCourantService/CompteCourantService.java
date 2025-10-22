package mg.razherana.banking.courant.application.compteCourantService;

import mg.razherana.banking.common.services.authorizationServices.AuthorizationService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing current account (Compte Courant) operations.
 * 
 * <p>
 * This service provides business logic for current account management including
 * creation, balance calculation, tax management, and account operations. The
 * service
 * implements the core banking business rule where account balances are
 * calculated
 * dynamically by summing all related transactions.
 * </p>
 * 
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li>Dynamic balance calculation (no stored balance field)</li>
 * <li>Monthly tax calculation and validation</li>
 * <li>Transaction-based account operations</li>
 * <li>Tax payment enforcement before withdrawals/transfers</li>
 * </ul>
 * 
 * <p>
 * This is a stateless EJB that can be injected into other components
 * using the {@code @EJB} annotation.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.application.transactionService.TransactionService
 * @see mg.razherana.banking.courant.api.CompteCourantResource
 */
public interface CompteCourantService extends AuthorizationService {
  /**
   * Find a user by ID using REST API call to java-interface.
   * 
   * @param userId the user ID
   * @return User object with the specified ID
   * @throws IllegalArgumentException if userId is null or user not found
   */
  public User findUser(Integer userId);

  public CompteCourant create(User user, BigDecimal taxe, LocalDateTime actionDateTime);

  public List<CompteCourant> getComptes();

  public CompteCourant findById(Integer id);

  public List<CompteCourant> getComptesByUser(User user);

  public List<CompteCourant> getComptesByUserId(Integer userId);

  /**
   * Calculate the balance (solde) of a compte courant by summing transactions
   * Balance = (sum of received amounts) - (sum of sent amounts)
   */
  public BigDecimal calculateSolde(CompteCourant compte);

  /**
   * Calculate the balance (solde) of a compte courant by summing transactions
   * Balance = (sum of received amounts) - (sum of sent amounts)
   */
  public BigDecimal calculateSolde(CompteCourant compte, LocalDateTime actionDateTime);
  /**
   * Calculate the total balance for all accounts of a user.
   * 
   * @param userId the user ID
   * @param actionDateTime optional date time for calculation (defaults to now)
   * @return total balance across all user's current accounts
   */
  public BigDecimal calculateTotalSoldeByUserId(Integer userId, LocalDateTime actionDateTime);

  public void updateTaxe(CompteCourant compte, BigDecimal nouvelleTaxe);

  public void delete(Integer id);

  // Taxes application logic

  public BigDecimal getTaxPaidTotal(CompteCourant compte);

  public BigDecimal getTaxPaidDate(CompteCourant compte, LocalDateTime actionDateTime);

  public boolean isTaxPaid(CompteCourant compte, LocalDateTime actionDateTime);

  // Taxes to pay for the month of the actionDateTime
  // If already paid, return 0
  // Sum up with old unpaid taxes if any in previous months
  // This is always superior or equal to 0
  public BigDecimal getTaxToPay(CompteCourant compte, LocalDateTime actionDateTime);
}
