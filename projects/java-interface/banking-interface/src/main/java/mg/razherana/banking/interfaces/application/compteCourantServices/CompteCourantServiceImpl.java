package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.interfaces.application.remoteServices.EJBLookupService;
import mg.razherana.banking.common.entities.User;
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
 * Service implementation for communicating with the banking-courant service using remote EJBs.
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
  public List<CompteCourant> getAccountsByUserId(Integer userId) {
    try {
      User user = userService.findUserById(userId);
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
  public CompteCourant createAccount(Integer userId, BigDecimal taxe, LocalDateTime actionDateTime) {
    try {
      User user = userService.findUserById(userId);
      var userCourant = new mg.razherana.banking.courant.entities.User();
      userCourant.setId(user.getId());
      userCourant.setName(user.getName());
      
      return compteCourantRemoteService.create(userCourant, taxe, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error creating account for user " + userId, e);
      return null;
    }
  }

  @Override
  public CompteCourant getAccountById(Integer accountId) {
    try {
      return compteCourantRemoteService.findById(accountId);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting account " + accountId, e);
      return null;
    }
  }

  @Override
  public BigDecimal getTaxToPay(Integer accountId, LocalDateTime actionDateTime) {
    try {
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
  public TransactionCourant makeDeposit(Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime) {
    try {
      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }
      
      return transactionRemoteService.depot(compte, montant, description, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making deposit to account " + accountId, e);
      return null;
    }
  }

  @Override
  public TransactionCourant makeWithdrawal(Integer accountId, BigDecimal montant, String description, LocalDateTime actionDateTime) {
    try {
      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }
      
      return transactionRemoteService.retrait(compte, montant, description, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making withdrawal from account " + accountId, e);
      return null;
    }
  }

  @Override
  public TransactionCourant payTax(Integer accountId, String description, LocalDateTime actionDateTime) {
    try {
      CompteCourant compte = compteCourantRemoteService.findById(accountId);
      if (compte == null) {
        LOG.warning("Account not found: " + accountId);
        return null;
      }
      
      return transactionRemoteService.payTax(compte, description, actionDateTime);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error paying tax for account " + accountId, e);
      return null;
    }
  }

  @Override
  public List<User> getAllUsers() {
    try {
      return userService.getAllUsers();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all users", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<CompteCourant> getAllAccounts() {
    try {
      return compteCourantRemoteService.getComptes();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all accounts", e);
      return new ArrayList<>();
    }
  }

  @Override
  public boolean makeTransfer(Integer sourceAccountId, Integer destinationAccountId, BigDecimal amount, String description, LocalDateTime actionDateTime) {
    try {
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
  public List<TransactionCourant> getTransactionHistory(Integer accountId) {
    try {
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
  public BigDecimal getAccountBalance(Integer accountId, LocalDateTime statusDate) {
    try {
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
  public BigDecimal getAccountBalanceByUserId(Integer userId, LocalDateTime actionDateTime) {
    return compteCourantRemoteService.calculateTotalSoldeByUserId(userId, actionDateTime);
  }
}
