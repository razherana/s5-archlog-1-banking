package mg.razherana.banking.interfaces.application.compteCourantServices;

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
     * @param userId The ID of the user
     * @return List of current accounts for the user
     */
    List<CompteCourant> getAccountsByUserId(Integer userId);
    
    /**
     * Creates a new current account for a user.
     * 
     * @param userId The ID of the user
     * @param taxe The monthly tax amount (optional)
     * @param actionDateTime The creation date/time (optional, defaults to current time)
     * @return The created account or null if creation failed
     */
    CompteCourant createAccount(Integer userId, BigDecimal taxe, LocalDateTime actionDateTime);
    
    /**
     * Gets a specific account by ID.
     * 
     * @param accountId The account ID
     * @return The account or null if not found
     */
    CompteCourant getAccountById(Integer accountId);
    
    /**
     * Gets the minimum tax amount to be paid for an account.
     * 
     * @param accountId The account ID
     * @return The minimum tax amount to pay
     */
    BigDecimal getTaxToPay(Integer accountId, LocalDateTime actionDateTime);
    
    /**
     * Makes a deposit to an account.
     * 
     * @param accountId The account ID
     * @param montant The amount to deposit
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant makeDeposit(Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime);
    
    /**
     * Makes a withdrawal from an account.
     * 
     * @param accountId The account ID
     * @param montant The amount to withdraw
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant makeWithdrawal(Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime);
    
    /**
     * Pays tax for an account.
     * 
     * @param accountId The account ID
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return The created transaction or null if failed
     */
    TransactionCourant payTax(Integer accountId, String description, LocalDateTime actionDateTime);
    
    /**
     * Gets all users in the system.
     * 
     * @return List of all users
     */
    List<mg.razherana.banking.interfaces.entities.User> getAllUsers();
    
    /**
     * Makes a transfer between two accounts.
     * 
     * @param sourceAccountId The source account ID
     * @param destinationAccountId The destination account ID
     * @param amount The amount to transfer
     * @param description The transaction description
     * @param actionDateTime The action date/time (optional, defaults to current time)
     * @return True if successful, false if failed
     */
    boolean makeTransfer(Integer sourceAccountId, Integer destinationAccountId, BigDecimal amount, String description, LocalDateTime actionDateTime);

    /**
     * Helper method to get all accounts from the banking-courant service.
     */
    List<CompteCourant> getAllAccounts();

    /**
     * Gets transaction history for a specific account.
     * 
     * @param accountId The account ID
     * @return List of transactions for the account
     */
    List<TransactionCourant> getTransactionHistory(Integer accountId);

    /**
     * Gets account balance at a specific date.
     * 
     * @param accountId The account ID
     * @param statusDate The date/time to calculate balance for
     * @return Account balance
     */
    BigDecimal getAccountBalance(Integer accountId, LocalDateTime statusDate);

    /**
     * Gets account balance of an user at a specific date.
     * 
     * @param userId
     * @param actionDateTime
     * @return Total balance of user
     */
    BigDecimal getAccountBalanceByUserId(Integer userId, LocalDateTime actionDateTime);
}