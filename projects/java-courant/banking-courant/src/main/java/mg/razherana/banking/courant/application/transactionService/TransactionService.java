package mg.razherana.banking.courant.application.transactionService;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public interface TransactionService {
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public TransactionCourant depot(CompteCourant compte, BigDecimal montant, String description);

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public TransactionCourant retrait(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime);

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public TransactionCourant payTax(CompteCourant compte, String description,
      LocalDateTime actionDateTime);

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void transfert(CompteCourant compteSource, CompteCourant compteDestination,
      BigDecimal montant, String description, LocalDateTime actionDateTime);

  public List<TransactionCourant> getTransactionsByCompte(CompteCourant compte);

  public List<TransactionCourant> getAllTransactions();

  public TransactionCourant findById(Integer id);
}
