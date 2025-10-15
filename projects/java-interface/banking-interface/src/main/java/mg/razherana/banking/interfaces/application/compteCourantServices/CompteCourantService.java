package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
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
}