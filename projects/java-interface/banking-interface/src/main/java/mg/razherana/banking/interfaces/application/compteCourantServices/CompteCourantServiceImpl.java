package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.interfaces.application.remoteServices.EJBLookupService;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
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

  private EJBLookupService remoteCourant;
  private CompteCourantRemoteService compteCourantRemoteService = null;
  private TransactionRemoteService transactionRemoteService = null;

  public CompteCourantServiceImpl() {
    try {
      JNDITreeLister.list();
      this.remoteCourant = new EJBLookupService();

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
      LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.depot(compte, montant, description, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making deposit to account " + accountId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant makeWithdrawal(UserAdmin userAdmin, Integer accountId, BigDecimal montant,
      String description,
      LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.retrait(compte, montant, description, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making withdrawal from account " + accountId, e);
      throw e;
    }
  }

  @Override
  public TransactionCourant payTax(UserAdmin userAdmin, Integer accountId, String description,
      LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
      }

      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }

      return transactionRemoteService.payTax(compte, description, actionDateTime);
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
      return new ArrayList<>();
    }
  }

  @Override
  public boolean makeTransfer(UserAdmin userAdmin, Integer sourceAccountId, Integer destinationAccountId,
      BigDecimal amount,
      String description, LocalDateTime actionDateTime) {
    try {
      if (!compteCourantRemoteService.hasAuthorization(userAdmin, "CREATE", "transaction_courants")) {
        LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create transactions");
        throw new IllegalStateException("Unauthorized access: User does not have permission to create transactions");
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

      transactionRemoteService.transfert(compteSource, compteDestination, amount, description, actionDateTime);
      return true;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making transfer from " + sourceAccountId + " to " + destinationAccountId, e);
      return false;
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
      return new ArrayList<>();
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
      return BigDecimal.ZERO;
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
      return BigDecimal.ZERO;
    }
  }
}
