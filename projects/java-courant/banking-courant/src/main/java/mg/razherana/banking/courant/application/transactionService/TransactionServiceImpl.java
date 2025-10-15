package mg.razherana.banking.courant.application.transactionService;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.courant.application.compteCourantService.CompteCourantService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.courant.entities.TransactionCourant.SpecialAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class TransactionServiceImpl implements TransactionService {
  private static final Logger LOG = Logger.getLogger(TransactionService.class.getName());

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @EJB
  private CompteCourantService compteCourantService;

  private void checkTaxesAndThrow(CompteCourant compte, LocalDateTime actionDateTime) {
    if (!compteCourantService.isTaxPaid(compte, actionDateTime)) {
      var amount = compteCourantService.getTaxToPay(compte, actionDateTime);

      LOG.warning("Compte " + compte.getId() + " has unpaid taxes, amount: " + amount);

      throw new IllegalArgumentException("Taxes must be paid before making a transaction, please pay the amount of "
          + amount + " MGA");
    }
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TransactionCourant depot(CompteCourant compte, BigDecimal montant, String description) {
    LOG.info("Processing depot of " + montant + " for compte " + compte.getId());

    if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Montant must be positive");
    }

    TransactionCourant transaction = new TransactionCourant();
    transaction.setSender(null); // System/external source
    transaction.setSpecialAction(SpecialAction.DEPOSIT.getDatabaseName());
    transaction.setReceiver(compte);
    transaction.setMontant(montant);
    transaction.setDate(LocalDateTime.now());

    entityManager.persist(transaction);
    entityManager.flush();
    LOG.info("Depot processed successfully");
    return transaction;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TransactionCourant retrait(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime) {
    LOG.info("Processing retrait of " + montant + " for compte " + compte.getId());

    if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Montant must be positive");
    }

    // Check if compte has payed taxes for the current month
    checkTaxesAndThrow(compte, actionDateTime);

    // Check if compte has sufficient balance
    BigDecimal currentSolde = compteCourantService.calculateSolde(compte);
    if (currentSolde.compareTo(montant) < 0) {
      throw new IllegalArgumentException("Solde insuffisant");
    }

    // For retrait, money goes to "system" (external destination)
    TransactionCourant transaction = new TransactionCourant();
    transaction.setSender(compte);
    transaction.setSpecialAction(SpecialAction.WITHDRAWAL.getDatabaseName());
    transaction.setReceiver(null); // System/external destination
    transaction.setMontant(montant);
    transaction.setDate(LocalDateTime.now());

    entityManager.persist(transaction);
    entityManager.flush();
    LOG.info("Retrait processed successfully");
    return transaction;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TransactionCourant payTax(CompteCourant compte, String description,
      LocalDateTime actionDateTime) {
    BigDecimal montant = compteCourantService.getTaxToPay(compte, actionDateTime);

    LOG.info("Processing tax payment of " + montant + " for compte " + compte.getId());

    if (montant.equals(BigDecimal.ZERO)) {
      LOG.info("No tax to pay for compte " + compte.getId());
      return null; // No tax to pay
    }

    if (montant == null || montant.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Montant must be positive");
    }

    // Check if compte has sufficient balance
    BigDecimal currentSolde = compteCourantService.calculateSolde(compte);
    if (currentSolde.compareTo(montant) < 0) {
      throw new IllegalArgumentException("Solde insuffisant");
    }

    // For retrait, money goes to "system" (external destination)
    TransactionCourant transaction = new TransactionCourant();
    transaction.setSender(compte);
    transaction.setSpecialAction(SpecialAction.TAXE.getDatabaseName());
    transaction.setReceiver(null); // System/external destination
    transaction.setMontant(montant);
    transaction.setDate(LocalDateTime.now());

    entityManager.persist(transaction);
    entityManager.flush();
    LOG.info("Tax payment processed successfully");
    return transaction;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void transfert(CompteCourant compteSource, CompteCourant compteDestination,
      BigDecimal montant, String description, LocalDateTime actionDateTime) {
    LOG.info("Processing transfert of " + montant + " from compte " + compteSource.getId()
        + " to compte " + compteDestination.getId());

    if (compteSource == null || compteDestination == null) {
      throw new IllegalArgumentException("Comptes cannot be null");
    }
    if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Montant must be positive");
    }

    // Check if source compte has payed taxes for the current month
    checkTaxesAndThrow(compteSource, actionDateTime);

    // Check if source compte has sufficient balance
    BigDecimal currentSolde = compteCourantService.calculateSolde(compteSource);
    if (currentSolde.compareTo(montant) < 0) {
      throw new IllegalArgumentException("Solde insuffisant");
    }

    // Create transfer transaction directly
    TransactionCourant transaction = new TransactionCourant();
    transaction.setSender(compteSource);
    transaction.setReceiver(compteDestination);
    transaction.setMontant(montant);
    transaction.setDate(LocalDateTime.now());

    entityManager.persist(transaction);
    entityManager.flush();
    LOG.info("Transfert processed successfully");
  }

  @Override
  public List<TransactionCourant> getTransactionsByCompte(CompteCourant compte) {
    LOG.info("Getting transactions for compte " + compte.getId());
    TypedQuery<TransactionCourant> query = entityManager.createQuery(
        "SELECT t FROM TransactionCourant t WHERE t.sender = :compte OR t.receiver = :compte ORDER BY t.date DESC",
        TransactionCourant.class);
    query.setParameter("compte", compte);

    return query.getResultList();
  }

  @Override
  public List<TransactionCourant> getAllTransactions() {
    LOG.info("Getting all transactions");
    TypedQuery<TransactionCourant> query = entityManager.createQuery(
        "SELECT t FROM TransactionCourant t ORDER BY t.date DESC",
        TransactionCourant.class);

    return query.getResultList();
  }

  @Override
  public TransactionCourant findById(Integer id) {
    LOG.info("Finding transaction by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Transaction ID cannot be null");
    }
    return entityManager.find(TransactionCourant.class, id);
  }
}
