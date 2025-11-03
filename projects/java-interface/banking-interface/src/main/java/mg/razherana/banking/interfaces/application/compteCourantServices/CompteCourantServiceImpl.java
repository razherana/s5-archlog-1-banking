package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import mg.razherana.banking.interfaces.application.changeServices.ChangeService;
import mg.razherana.banking.interfaces.application.remoteServices.EJBLookupService;
import mg.razherana.banking.interfaces.tests.JNDITreeLister;
import mg.razherana.banking.courant.application.compteCourantService.CompteCourantRemoteService;
import mg.razherana.banking.courant.application.transactionService.TransactionRemoteService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service implementation for communicating with the banking-courant service
 * using remote EJBs.
 */
@Stateless
public class CompteCourantServiceImpl implements CompteCourantService {

  private static final Logger LOG = Logger.getLogger(CompteCourantServiceImpl.class.getName());

  @EJB
  private UserService userService;

  @EJB
  private ChangeService changeService;

  private EJBLookupService remoteCourant;
  private CompteCourantRemoteService compteCourantRemoteService = null;
  private TransactionRemoteService transactionRemoteService = null;

  public CompteCourantServiceImpl() {
    try {
      JNDITreeLister.list();
      this.remoteCourant = new EJBLookupService("host.docker.internal:8081");

      this.compteCourantRemoteService = remoteCourant.lookupStatefulBean(
          "global/CompteCourantRemoteServiceImpl!mg.razherana.banking.courant.application.compteCourantService.CompteCourantRemoteService",
          CompteCourantRemoteService.class);
      this.transactionRemoteService = remoteCourant.lookupStatefulBean(
          "global/TransactionRemoteServiceImpl!mg.razherana.banking.courant.application.transactionService.TransactionRemoteService",
          TransactionRemoteService.class);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to initialize remote services", e);
      throw new RuntimeException("Failed to initialize remote services", e);
    }
  }

  @Override
  public List<CompteCourant> getAccountsByUserId(UserAdmin userAdmin, Integer userId) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      User user = userService.findUserById(userAdmin, userId);
      var userCourant = new mg.razherana.banking.courant.entities.User();
      userCourant.setId(user.getId());
      userCourant.setName(user.getName());

      return compteCourantRemoteService.getComptesByUser(userCourant);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting accounts for user " + userId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public CompteCourant createAccount(UserAdmin userAdmin, Integer userId, BigDecimal taxe,
      LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create current accounts");
        throw new IllegalStateException(
            "Unauthorized access: User does not have permission to create current accounts");
      }

      User user = userService.findUserById(userAdmin, userId);
      var userCourant = new mg.razherana.banking.courant.entities.User();
      userCourant.setId(user.getId());
      userCourant.setName(user.getName());

      return compteCourantRemoteService.create(userCourant, taxe, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error creating account for user " + userId, e);
      throw e;
    }
  }

  @Override
  public CompteCourant getAccountById(UserAdmin userAdmin, Integer accountId) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      return compteCourantRemoteService.findById(accountId);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting account " + accountId, e);
      throw e;
    }
  }

  @Override
  public BigDecimal getTaxToPay(UserAdmin userAdmin, Integer accountId, LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")
          || !compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        return BigDecimal.ZERO;
      }
      return compteCourantRemoteService.getTaxToPay(compte, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting tax to pay for account " + accountId, e);
      return BigDecimal.ZERO;
    }
  }

  @Override
  public TransactionCourant makeDeposit(
      UserAdmin userAdmin,
      Integer accountId,
      BigDecimal montant,
      String description,
      LocalDateTime actionDateTime,
      String currency) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      // Default to MGA if currency is null
      if (currency == null) {
        currency = "MGA";
      }

      // Apply currency conversion if not MGA
      BigDecimal convertedAmount = montant;
      if (!"MGA".equals(currency)) {
        var change = changeService.getChange(currency, actionDateTime);
        convertedAmount = montant.multiply(change);
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.depot(compte, convertedAmount, description, actionDateTime, currency);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making deposit to account " + accountId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant makeWithdrawal(UserAdmin userAdmin, Integer accountId, BigDecimal montant,
      String description,
      LocalDateTime actionDateTime,
      String currency) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      // Default to MGA if currency is null
      if (currency == null) {
        currency = "MGA";
      }

      // Apply currency conversion if not MGA
      BigDecimal convertedAmount = montant;
      if (!"MGA".equals(currency)) {
        var change = changeService.getChange(currency, actionDateTime);
        convertedAmount = montant.multiply(change);
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.retrait(compte, convertedAmount, description, actionDateTime, currency);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making withdrawal from account " + accountId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant payTax(UserAdmin userAdmin, Integer accountId, String description,
      LocalDateTime actionDateTime,
      String currency) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      // Default to MGA if currency is null
      if (currency == null) {
        currency = "MGA";
      }

      // Note: Tax amount is calculated by the remote service based on account
      // configuration
      // Currency conversion would be applied to the calculated tax amount if needed
      // For now, we just pass the currency information in the description
      if (!"MGA".equals(currency)) {
        description = description + " (Currency: " + currency + ")";
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.payTax(compte, description, actionDateTime, currency);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error paying tax for account " + accountId, e);
      throw e;
    }
  }

  @Override
  public List<User> getAllUsers(UserAdmin userAdmin) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "users")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read users");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read users");
      }

      return userService.getAllUsers(userAdmin);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all users", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<CompteCourant> getAllAccounts(UserAdmin userAdmin) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      return compteCourantRemoteService.getComptes();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all accounts", e);
      throw e;
    }
  }

  @Override
  public boolean makeTransfer(UserAdmin userAdmin, Integer sourceAccountId, Integer destinationAccountId,
      BigDecimal amount,
      String description, LocalDateTime actionDateTime,
      String currency) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      // Default to MGA if currency is null
      if (currency == null) {
        currency = "MGA";
      }

      // Apply currency conversion if not MGA
      BigDecimal convertedAmount = amount;
      if (!"MGA".equals(currency)) {
        var change = changeService.getChange(currency, actionDateTime);
        convertedAmount = amount.multiply(change);
      }

      CompteCourant compteSource = compteCourantRemoteService.findById(sourceAccountId);
      CompteCourant compteDestination = compteCourantRemoteService.findById(destinationAccountId);

      if (compteSource == null) {
        LOG.warning("Source account not found: " + sourceAccountId);
        return false;
      }

      if (compteDestination == null) {
        LOG.warning("Destination account not found: " + destinationAccountId);
        return false;
      }

      transactionRemoteService.transfert(compteSource, compteDestination, convertedAmount, description, actionDateTime,
          currency);
      return true;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making transfer from " + sourceAccountId + " to " + destinationAccountId, e);
      throw e;
    }
  }

  @Override
  public List<TransactionCourant> getTransactionHistory(UserAdmin userAdmin, Integer accountId) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read transactions");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return new ArrayList<>();
      }

      return transactionRemoteService.getTransactionsByCompte(compte);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting transaction history for account " + accountId, e);
      throw e;
    }
  }

  @Override
  public BigDecimal getAccountBalance(UserAdmin userAdmin, Integer accountId, LocalDateTime statusDate) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return BigDecimal.ZERO;
      }

      return compteCourantRemoteService.calculateSolde(compte, statusDate);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error calculating balance for account " + accountId, e);
      throw e;
    }
  }

  @Override
  public BigDecimal getAccountBalanceByUserId(UserAdmin userAdmin, Integer userId, LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "READ", "compte_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current accounts");
        throw new IllegalStateException("Unauthorized access: User does not have permission to read current accounts");
      }

      return compteCourantRemoteService.calculateTotalSoldeByUserId(userId, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error calculating total balance for user " + userId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant validateTransaction(UserAdmin userAdmin, Integer transactionId,
      LocalDateTime validationDate) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "VALIDATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to validate transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to validate transactions");
      }

      return transactionRemoteService.validerVirement(transactionId, validationDate);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error validating transaction " + transactionId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant updateTransaction(UserAdmin userAdmin, Integer idTransaction, BigDecimal montant,
      String change) {
    if (!compteCourantRemoteService.hasAuthorization(userAdmin, "UPDATE", "transaction_courants")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to update transactions");
      throw new IllegalStateException("Unauthorized access: User does not have permission to update transactions");
    }

    // Get montant based on change
    var transaction = transactionRemoteService.findById(idTransaction);

    if (transaction == null)
      throw new IllegalArgumentException("La transaction n'existe pas");

    // Get OG montant or updated
    BigDecimal newMontant = montant;

    if (newMontant == null)
      newMontant = transaction.getMontant();

    // Apply conversion between changes
    if (change != null) {
      var transactionDate = transaction.getDate();

      var ogChange = changeService.getChange(transaction.getChange(), transactionDate);
      var newChange = changeService.getChange(change, transactionDate);

      LOG.info("Original Change: " + ogChange + ", New Change: " + newChange);
      LOG.info("Original Montant: " + newMontant);


      newMontant = newMontant.divide(ogChange).multiply(newChange);

      LOG.info("Converted Montant: " + newMontant);
    }

    return transactionRemoteService.updateTransaction(idTransaction, newMontant, change);
  }
}
