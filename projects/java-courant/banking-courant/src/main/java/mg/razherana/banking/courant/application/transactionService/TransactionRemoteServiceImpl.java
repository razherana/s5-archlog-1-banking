package mg.razherana.banking.courant.application.transactionService;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Remote implementation of the TransactionService that delegates to the local
 * service.
 * 
 * <p>
 * This stateless EJB provides remote access to transaction operations by
 * delegating all calls to the local TransactionServiceImpl. This allows
 * other applications to access the transaction services via EJB remote calls.
 * </p>
 * 
 * <p>
 * The implementation uses dependency injection to access the local service
 * and simply forwards all method calls, making the local functionality
 * available remotely without duplicating business logic.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see TransactionRemoteService
 * @see TransactionService
 * @see TransactionServiceImpl
 */
@Stateless
public class TransactionRemoteServiceImpl implements TransactionRemoteService {

  @EJB
  private TransactionService transactionService;

  @Override
  public TransactionCourant depot(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime, String devise) {
    return transactionService.depot(compte, montant, description, actionDateTime, devise);
  }

  @Override
  public TransactionCourant retrait(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime, String devise) {
    return transactionService.retrait(compte, montant, description, actionDateTime, devise);
  }

  @Override
  public TransactionCourant payTax(CompteCourant compte, String description, LocalDateTime actionDateTime, String devise) {
    return transactionService.payTax(compte, description, actionDateTime, devise);
  }

  @Override
  public void transfert(CompteCourant compteSource, CompteCourant compteDestination, BigDecimal montant,
      String description, LocalDateTime actionDateTime, String devise) {
    transactionService.transfert(compteSource, compteDestination, montant, description, actionDateTime, devise);
  }

  @Override
  public List<TransactionCourant> getTransactionsByCompte(CompteCourant compte) {
    return transactionService.getTransactionsByCompte(compte);
  }

  @Override
  public List<TransactionCourant> getAllTransactions() {
    return transactionService.getAllTransactions();
  }

  @Override
  public TransactionCourant findById(Integer id) {
    return transactionService.findById(id);
  }

  @Override
  public TransactionCourant validerVirement(int virementId, LocalDateTime date) {
    return transactionService.validerVirement(virementId, date);
  }

  @Override
  public TransactionCourant updateTransaction(Integer idTransaction, BigDecimal montant, String change) {
    return transactionService.updateTransaction(idTransaction, montant, change);
  }
}