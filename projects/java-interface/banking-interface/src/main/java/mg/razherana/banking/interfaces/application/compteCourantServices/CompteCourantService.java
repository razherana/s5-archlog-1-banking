package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for communicating with the banking-courant service.
 */
public interface CompteCourantService {
    
    /**
     * Fetches all current accounts for a specific user.
     * 
     * @param userAdmin The admin performing the action
     * @param userId The ID of the user
     * @return List of current accounts for the user
     */
    List<CompteCourant> getAccountsByUserId(UserAdmin userAdmin, Integer userId);
    
    /**
     * Creates a new current account for a user.
     * 
     * @param userAdmin The admin performing the action
     * @param userId The ID of the user
     * @param taxe The monthly tax amount (optional)
     * @param actionDateTime The creation date/time (optional, defaults to current time)
     * @return The created account or null if creation failed
     */
    CompteCourant createAccount(UserAdmin userAdmin, Integer userId, BigDecimal taxe, LocalDateTime actionDateTime);
    
    /**
     * Gets a specific account by ID.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @return The account or null if not found
     */
    CompteCourant getAccountById(UserAdmin userAdmin, Integer accountId);
    
    /**
     * Gets the minimum tax amount to be paid for an account.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @param actionDateTime The action date/time
     * @return The minimum tax amount to pay
     */
    BigDecimal getTaxToPay(UserAdmin userAdmin, Integer accountId, LocalDateTime actionDateTime);
    
    /**
     * Makes a deposit to an account.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @param montant The amount to deposit
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant makeDeposit(UserAdmin userAdmin, Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime);
    
    /**
     * Makes a withdrawal from an account.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @param montant The amount to withdraw
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant makeWithdrawal(UserAdmin userAdmin, Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime);
    
    /**
     * Pays tax for an account.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant payTax(UserAdmin userAdmin, Integer accountId, String description, LocalDateTime actionDateTime);
    
    /**
     * Gets all users in the system.
     * 
     * @param userAdmin The admin performing the action
     * @return List of all users
     */
    List<mg.razherana.banking.common.entities.User> getAllUsers(UserAdmin userAdmin);
    
    /**
     * Makes a transfer between two accounts.
     * 
     * @param userAdmin The admin performing the action
     * @param sourceAccountId The source account ID
     * @param destinationAccountId The destination account ID
     * @param amount The amount to transfer
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return True if successful, false if failed
     */
    boolean makeTransfer(UserAdmin userAdmin, Integer sourceAccountId, Integer destinationAccountId, BigDecimal amount, String description, LocalDateTime actionDateTime);

    /**
     * Helper method to get all accounts from the banking-courant service.
     * 
     * @param userAdmin The admin performing the action
     * @return List of all accounts
     */
    List<CompteCourant> getAllAccounts(UserAdmin userAdmin);

    /**
     * Gets transaction history for a specific account.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @return List of transactions for the account
     */
    List<TransactionCourant> getTransactionHistory(UserAdmin userAdmin, Integer accountId);

    /**
     * Gets account balance at a specific date.
     * 
     * @param userAdmin The admin performing the action
     * @param accountId The account ID
     * @param statusDate The date/time to calculate balance for
     * @return Account balance
     */
    BigDecimal getAccountBalance(UserAdmin userAdmin, Integer accountId, LocalDateTime statusDate);

    /**
     * Gets account balance of an user at a specific date.
     * 
     * @param userAdmin The admin performing the action
     * @param userId The user ID
     * @param actionDateTime The action date/time
     * @return Total balance of user
     */
    BigDecimal getAccountBalanceByUserId(UserAdmin userAdmin, Integer userId, LocalDateTime actionDateTime);
}