package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.interfaces.dto.AccountStatusDTO;
import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
import mg.razherana.banking.interfaces.dto.TransactionCourantDTO;
import mg.razherana.banking.interfaces.dto.UserDTO;

import java.util.List;

/**
 * Service interface for communicating with the banking-courant service.
 */
public interface CompteCourantService {
    
    /**
     * Fetches all current accounts for a specific user.
     * 
     * @param userId The ID of the user
     * @return List of current accounts for the user
     */
    List<CompteCourantDTO> getAccountsByUserId(Integer userId);
    
    /**
     * Creates a new current account for a user.
     * 
     * @param userId The ID of the user
     * @param taxe The monthly tax amount (optional)
     * @param actionDateTime The creation date/time (optional, defaults to current time)
     * @return The created account or null if creation failed
     */
    CompteCourantDTO createAccount(Integer userId, Double taxe, String actionDateTime);
    
    /**
     * Gets a specific account by ID.
     * 
     * @param accountId The account ID
     * @return The account or null if not found
     */
    CompteCourantDTO getAccountById(Integer accountId);
    
    /**
     * Gets the minimum tax amount to be paid for an account.
     * 
     * @param accountId The account ID
     * @return The minimum tax amount to pay
     */
    Double getTaxToPay(Integer accountId);
    
    /**
     * Makes a deposit to an account.
     * 
     * @param accountId The account ID
     * @param montant The amount to deposit
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return null if successful, error message if failed
     */
    String makeDeposit(Integer accountId, Double montant, String description, String actionDateTime);
    
    /**
     * Makes a withdrawal from an account.
     * 
     * @param accountId The account ID
     * @param montant The amount to withdraw
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return null if successful, error message if failed
     */
    String makeWithdrawal(Integer accountId, Double montant, String description, String actionDateTime);
    
    /**
     * Pays tax for an account.
     * 
     * @param accountId The account ID
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return null if successful, error message if failed
     */
    String payTax(Integer accountId, String description, String actionDateTime);
    
    /**
     * Gets all users in the system.
     * 
     * @return List of all users
     */
    List<UserDTO> getAllUsers();
    
    /**
     * Makes a transfer between two accounts.
     * 
     * @param sourceAccountId The source account ID
     * @param destinationAccountId The destination account ID
     * @param amount The amount to transfer
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return null if successful, error message if failed
     */
    String makeTransfer(Integer sourceAccountId, Integer destinationAccountId, Double amount, String description, String actionDateTime);

    /**
     * Helper method to get all accounts from the banking-courant service.
     */
    List<CompteCourantDTO> getAllAccounts();

    /**
     * Gets transaction history for a specific account.
     * 
     * @param accountId The account ID
     * @return List of transactions for the account
     */
    List<TransactionCourantDTO> getTransactionHistory(Integer accountId);

    /**
     * Gets account status (balance, tax info, transaction summary) at a specific date.
     * 
     * @param accountId The account ID
     * @param statusDate The date/time to calculate status for
     * @return Account status information
     */
    AccountStatusDTO getAccountStatus(Integer accountId, java.time.LocalDateTime statusDate);
}