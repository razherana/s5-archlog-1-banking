package mg.razherana.banking.courant.application.transactionService;

import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
  /**
   * Deposit into an account with a specified currency (devise).
   *
   * @param compte the destination account
   * @param montant the amount to deposit
   * @param description optional description
   * @param actionDateTime optional action date/time
   * @param devise the currency code for this transaction (e.g., "MGA", "USD")
   * @return the created TransactionCourant
   */
  public TransactionCourant depot(CompteCourant compte, BigDecimal montant, String description, LocalDateTime actionDateTime, String devise);

  /**
   * Withdrawal from an account with a specified currency (devise).
   */
  public TransactionCourant retrait(CompteCourant compte, BigDecimal montant, String description,
      LocalDateTime actionDateTime, String devise);

  /**
   * Pay tax for an account with a specified currency (devise).
   */
  public TransactionCourant payTax(CompteCourant compte, String description,
      LocalDateTime actionDateTime, String devise);

  /**
   * Transfer between accounts with a specified currency (devise).
   */
  public void transfert(CompteCourant compteSource, CompteCourant compteDestination,
      BigDecimal montant, String description, LocalDateTime actionDateTime, String devise);

  public TransactionCourant validerVirement(int virementId, LocalDateTime date);

  public List<TransactionCourant> getTransactionsByCompte(CompteCourant compte);

  public List<TransactionCourant> getAllTransactions();

  public TransactionCourant findById(Integer id);
}
