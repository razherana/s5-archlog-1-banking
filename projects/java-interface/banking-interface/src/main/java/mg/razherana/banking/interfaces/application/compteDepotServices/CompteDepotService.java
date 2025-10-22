package mg.razherana.banking.interfaces.application.compteDepotServices;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
import mg.razherana.banking.interfaces.dto.TypeCompteDepotDTO;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for communicating with the banking-depot service (.NET).
 * Provides operations for deposit account management and withdrawal processing.
 */
public interface CompteDepotService {

  /**
   * Fetches all deposit accounts for a specific user.
   * 
   * @param userAdmin The admin performing the action
   * @param userId The ID of the user
   * @return List of deposit accounts for the user
   */
  List<CompteDepotDTO> getAccountsByUserId(UserAdmin userAdmin, Integer userId);

  /**
   * Gets a specific deposit account by ID.
   * 
   * @param userAdmin The admin performing the action
   * @param accountId The account ID
   * @return The account or null if not found
   */
  CompteDepotDTO getAccountById(UserAdmin userAdmin, Integer accountId);

  /**
   * Creates a new deposit account for a user.
   * 
   * @param userAdmin The admin performing the action
   * @param typeCompteDepotId The type of deposit account
   * @param userId            The ID of the user
   * @param dateEcheance      The maturity date (ISO format string)
   * @param montant           The initial deposit amount
   * @param actionDateTime    The creation date/time (optional, defaults to
   *                          current time)
   * @return The created account or null if creation failed
   */
  CompteDepotDTO createAccount(UserAdmin userAdmin, Integer typeCompteDepotId, Integer userId, String dateEcheance,
      BigDecimal montant, String actionDateTime);

  /**
   * Withdraws from a deposit account at maturity.
   * 
   * @param userAdmin The admin performing the action
   * @param accountId      The account ID
   * @param targetAccountId The target current account ID to deposit withdrawn funds (optional)
   * @param actionDateTime The withdrawal date/time (optional, defaults to current
   *                       time)
   * @return Success message with withdrawal details or error message
   */
  String withdrawFromAccount(UserAdmin userAdmin, Integer accountId, Integer targetAccountId, String actionDateTime);

  /**
   * Gets all available deposit account types with their interest rates.
   * 
   * @param userAdmin The admin performing the action
   * @return List of all deposit account types
   */
  List<TypeCompteDepotDTO> getAllDepositTypes(UserAdmin userAdmin);

  /**
   * Gets a specific deposit account type by ID.
   * 
   * @param userAdmin The admin performing the action
   * @param typeId The type ID
   * @return The deposit account type or null if not found
   */
  TypeCompteDepotDTO getDepositTypeById(UserAdmin userAdmin, Integer typeId);

  /**
   * Fetches all deposit accounts (admin function).
   * 
   * @param userAdmin The admin performing the action
   * @return List of all deposit accounts
   */
  List<CompteDepotDTO> getAllAccounts(UserAdmin userAdmin);
}