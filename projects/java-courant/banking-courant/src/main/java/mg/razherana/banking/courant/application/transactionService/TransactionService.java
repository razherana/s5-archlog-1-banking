package mg.razherana.banking.courant.application.transactionService;

import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
  public TransactionCourant depot(CompteCourant compte, BigDecimal montant, String description, LocalDateTime actionDateTime);

  public TransactionCourant retrait(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime);

  public TransactionCourant payTax(CompteCourant compte, String description,
      LocalDateTime actionDateTime);

  public void transfert(CompteCourant compteSource, CompteCourant compteDestination,
      BigDecimal montant, String description, LocalDateTime actionDateTime);

  public List<TransactionCourant> getTransactionsByCompte(CompteCourant compte);

  public List<TransactionCourant> getAllTransactions();

  public TransactionCourant findById(Integer id);
}
